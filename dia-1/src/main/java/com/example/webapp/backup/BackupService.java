package com.example.webapp.backup;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class BackupService {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    private final Path defaultBackupDirectory;
    private final Path settingsFile;
    private final Path uploadsDirectory;
    private final Object lock = new Object();

    public BackupService(
            DataSource dataSource,
            @Value("${app.backup.dir:}") String configuredBackupDir,
            @Value("${save.uploads.path:}") String uploadsDirectoryPath) {
        this.dataSource = dataSource;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        Path rootDir;
        if (configuredBackupDir != null && !configuredBackupDir.isBlank()) {
            rootDir = Paths.get(configuredBackupDir);
        } else {
            rootDir = Paths.get(System.getProperty("user.home"), "JewelleryStoreManager", "backups");
        }

        this.defaultBackupDirectory = rootDir;
        this.settingsFile = rootDir.resolve("backup-settings.json");

        if (uploadsDirectoryPath != null && !uploadsDirectoryPath.isBlank()) {
            this.uploadsDirectory = Paths.get(uploadsDirectoryPath);
        } else {
            this.uploadsDirectory = Paths.get(System.getProperty("user.home"), ".productmanager", "data", "uploads");
        }
    }

    private Path getBackupDirectory(BackupSettings settings) {
        if (settings != null && settings.getBackupDirectory() != null && !settings.getBackupDirectory().isBlank()) {
            return Paths.get(settings.getBackupDirectory());
        }
        return defaultBackupDirectory;
    }

    public Map<String, Object> getSettingsView() {
        synchronized (lock) {
            BackupSettings settings = loadSettings();
            return buildSettingsResponse(settings, false, "Backup settings loaded.");
        }
    }

    public Map<String, Object> updateSettings(BackupSettingsRequest request) {
        synchronized (lock) {
            BackupSettings settings = loadSettings();
            settings.setEnabled(request.isEnabled());
            settings.setFrequency(request.getFrequency() == null ? BackupFrequency.WEEKLY : request.getFrequency());
            if (request.getBackupDirectory() != null && !request.getBackupDirectory().isBlank()) {
                settings.setBackupDirectory(request.getBackupDirectory());
            }
            saveSettings(settings);
            return buildSettingsResponse(settings, true, "Backup settings updated successfully.");
        }
    }

    public Map<String, Object> runBackupNow(String trigger) {
        synchronized (lock) {
            BackupSettings settings = loadSettings();
            BackupRunResult result = executeBackup(settings, trigger == null ? "manual" : trigger);
            return buildRunResponse(settings, result, result.isSuccess()
                    ? "Backup completed successfully."
                    : "Backup failed.");
        }
    }

    public Map<String, Object> runDueBackupIfNeeded() {
        synchronized (lock) {
            BackupSettings settings = loadSettings();
            if (!settings.isEnabled()) {
                return buildSettingsResponse(settings, false, "Automatic backup is disabled.");
            }

            if (!isDue(settings, LocalDateTime.now())) {
                return buildSettingsResponse(settings, false, "Backup is not due yet.");
            }

            BackupRunResult result = executeBackup(settings, "automatic");
            return buildRunResponse(settings, result, result.isSuccess()
                    ? "Scheduled backup completed."
                    : "Scheduled backup failed.");
        }
    }

    public Map<String, Object> listBackups() {
        synchronized (lock) {
            BackupSettings settings = loadSettings();
            Path activeDir = getBackupDirectory(settings);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("backupDirectory", activeDir.toString());
            response.put("backups", getBackupFiles(settings));
            response.put("message", "Backup files loaded.");
            return response;
        }
    }

    public Map<String, Object> importBackup(BackupImportRequest request) {
        synchronized (lock) {
            Map<String, Object> response = new LinkedHashMap<>();
            try {
                Path importFile = resolveImportFile(request);
                BackupSettings settings = loadSettings();
                BackupRunResult safetyBackup = executeBackup(settings, "before-import");

                String filename = importFile.getFileName().toString();
                if (filename.endsWith(".zip")) {
                    Path tempDir = Files.createTempDirectory("pm-restore-");
                    try {
                        unzip(importFile, tempDir);

                        Path sqlFile = tempDir.resolve("database.sql");
                        if (Files.exists(sqlFile)) {
                            executeSqlFile(sqlFile);
                        } else {
                            try (Stream<Path> walk = Files.list(tempDir)) {
                                Path fallbackSql = walk
                                        .filter(p -> p.getFileName().toString().endsWith(".sql"))
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalArgumentException("Backup ZIP does not contain a database SQL file."));
                                executeSqlFile(fallbackSql);
                            }
                        }

                        Path unzippedUploads = tempDir.resolve("uploads");
                        if (Files.exists(unzippedUploads) && Files.isDirectory(unzippedUploads)) {
                            copyDirectory(unzippedUploads, uploadsDirectory);
                        }
                    } finally {
                        deleteDirectory(tempDir);
                    }
                } else {
                    executeSqlFile(importFile);
                }

                response.put("success", true);
                response.put("importedFile", importFile.toString());
                response.put("preImportBackupFile", safetyBackup.getBackupFile());
                response.put("message", "Backup imported successfully.");
            } catch (Exception ex) {
                ex.printStackTrace();
                response.put("success", false);
                response.put("error", ex.getMessage());
                response.put("message", "Backup import failed.");
            }
            BackupSettings settings = loadSettings();
            response.put("backups", getBackupFiles(settings));
            return response;
        }
    }

    private BackupRunResult executeBackup(BackupSettings settings, String trigger) {
        try {
            Path activeDir = getBackupDirectory(settings);
            ensureDirectory(activeDir);
            String suffix = (trigger != null && !trigger.equals("manual") && !trigger.equals("automatic")) ? "-" + trigger : "";
            String fileName = "db-backup-" + FILE_TIMESTAMP.format(LocalDateTime.now()) + suffix + ".zip";
            Path backupFile = activeDir.resolve(fileName);

            Path tempSqlFile = Files.createTempFile("db-dump-", ".sql");
            try {
                writeBackupFile(tempSqlFile);

                try (OutputStream os = Files.newOutputStream(backupFile);
                     ZipOutputStream zos = new ZipOutputStream(os)) {

                    // 1. Add database.sql
                    ZipEntry sqlEntry = new ZipEntry("database.sql");
                    zos.putNextEntry(sqlEntry);
                    Files.copy(tempSqlFile, zos);
                    zos.closeEntry();

                    // 2. Add uploads folder recursively
                    if (Files.exists(uploadsDirectory) && Files.isDirectory(uploadsDirectory)) {
                        try (Stream<Path> walk = Files.walk(uploadsDirectory)) {
                            List<Path> filesToZip = walk.filter(p -> !Files.isDirectory(p)).toList();
                            for (Path p : filesToZip) {
                                String relativePath = uploadsDirectory.relativize(p).toString().replace("\\", "/");
                                ZipEntry mediaEntry = new ZipEntry("uploads/" + relativePath);
                                zos.putNextEntry(mediaEntry);
                                Files.copy(p, zos);
                                zos.closeEntry();
                            }
                        }
                    }
                }
            } finally {
                Files.deleteIfExists(tempSqlFile);
            }

            settings.setLastBackupAt(LocalDateTime.now());
            settings.setLastBackupFile(backupFile.toString());
            saveSettings(settings);
            return new BackupRunResult(true, trigger, backupFile.toString(), null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new BackupRunResult(false, trigger, null, ex.getMessage());
        }
    }

    private Map<String, Object> buildSettingsResponse(BackupSettings settings, boolean updated, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("enabled", settings.isEnabled());
        response.put("frequency", settings.getFrequency().name());
        response.put("backupDirectory", settings.getBackupDirectory());
        response.put("lastBackupAt", settings.getLastBackupAt());
        response.put("lastBackupFile", settings.getLastBackupFile());
        response.put("nextDueAt", getNextDueAt(settings));
        response.put("dueNow", isDue(settings, LocalDateTime.now()));
        response.put("updated", updated);
        response.put("message", message);
        return response;
    }

    private Map<String, Object> buildRunResponse(BackupSettings settings, BackupRunResult result, String message) {
        Map<String, Object> response = buildSettingsResponse(settings, false, message);
        response.put("success", result.isSuccess());
        response.put("trigger", result.getTrigger());
        response.put("backupFile", result.getBackupFile());
        response.put("error", result.getError());
        return response;
    }

    private LocalDateTime getNextDueAt(BackupSettings settings) {
        if (settings.getLastBackupAt() == null) {
            return LocalDateTime.now();
        }
        return switch (settings.getFrequency()) {
            case DAILY -> settings.getLastBackupAt().plusDays(1);
            case WEEKLY -> settings.getLastBackupAt().plusWeeks(1);
            case MONTHLY -> settings.getLastBackupAt().plusMonths(1);
        };
    }

    private boolean isDue(BackupSettings settings, LocalDateTime now) {
        if (settings.getLastBackupAt() == null) {
            return true;
        }
        return !getNextDueAt(settings).isAfter(now);
    }

    private BackupSettings loadSettings() {
        try {
            ensureDirectory(defaultBackupDirectory);
            if (Files.exists(settingsFile)) {
                BackupSettings settings = objectMapper.readValue(settingsFile.toFile(), BackupSettings.class);
                if (settings.getFrequency() == null) {
                    settings.setFrequency(BackupFrequency.WEEKLY);
                }
                if (settings.getBackupDirectory() == null || settings.getBackupDirectory().isBlank()) {
                    settings.setBackupDirectory(defaultBackupDirectory.toString());
                }
                return settings;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        BackupSettings defaults = new BackupSettings();
        defaults.setEnabled(true);
        defaults.setFrequency(BackupFrequency.WEEKLY);
        defaults.setBackupDirectory(defaultBackupDirectory.toString());
        saveSettings(defaults);
        return defaults;
    }

    private void saveSettings(BackupSettings settings) {
        try {
            ensureDirectory(defaultBackupDirectory);
            if (settings.getBackupDirectory() == null || settings.getBackupDirectory().isBlank()) {
                settings.setBackupDirectory(defaultBackupDirectory.toString());
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile.toFile(), settings);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save backup settings", ex);
        }
    }

    private void ensureDirectory(Path path) throws IOException {
        Files.createDirectories(path);
    }

    private List<Map<String, Object>> getBackupFiles(BackupSettings settings) {
        try {
            Path activeDir = getBackupDirectory(settings);
            ensureDirectory(activeDir);
            try (Stream<Path> files = Files.list(activeDir)) {
                return files
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().startsWith("db-backup-"))
                        .filter(path -> path.getFileName().toString().endsWith(".zip") || path.getFileName().toString().endsWith(".sql"))
                        .sorted(Comparator.comparing(this::getLastModified).reversed())
                        .map(this::buildBackupFileView)
                        .toList();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read backup files", ex);
        }
    }

    private Map<String, Object> buildBackupFileView(Path path) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("name", path.getFileName().toString());
        view.put("path", path.toString());
        view.put("sizeBytes", getSize(path));
        view.put("lastModified", getLastModified(path));
        return view;
    }

    private Instant getLastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException ex) {
            return Instant.EPOCH;
        }
    }

    private long getSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException ex) {
            return 0L;
        }
    }

    private Path resolveImportFile(BackupImportRequest request) throws IOException {
        BackupSettings settings = loadSettings();
        Path activeDir = getBackupDirectory(settings);
        ensureDirectory(activeDir);
        if (request == null || request.isLatest()) {
            return getBackupFiles(settings).stream()
                    .findFirst()
                    .map(file -> activeDir.resolve(String.valueOf(file.get("name"))))
                    .orElseThrow(() -> new IllegalArgumentException("No backup files are available to import."));
        }

        String requestedFile = request.getBackupFile();
        if (requestedFile == null || requestedFile.isBlank()) {
            throw new IllegalArgumentException("Select a backup file to import.");
        }

        Path resolved = activeDir.resolve(Paths.get(requestedFile).getFileName()).normalize();
        Path normalizedDirectory = activeDir.toAbsolutePath().normalize();
        Path normalizedFile = resolved.toAbsolutePath().normalize();
        if (!normalizedFile.startsWith(normalizedDirectory)) {
            throw new IllegalArgumentException("Backup file must be inside the configured backup folder.");
        }
        if (!Files.isRegularFile(normalizedFile)) {
            throw new IllegalArgumentException("Selected backup file was not found.");
        }
        String name = normalizedFile.getFileName().toString();
        if (!name.startsWith("db-backup-")
                || (!name.endsWith(".sql") && !name.endsWith(".zip"))) {
            throw new IllegalArgumentException("Selected file is not a valid database backup.");
        }
        return normalizedFile;
    }

    private void executeSqlFile(Path importFile) throws IOException, SQLException {
        String sql = Files.readString(importFile, StandardCharsets.UTF_8);
        List<String> statements = splitSqlStatements(stripSqlComments(sql));
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sqlStatement : statements) {
                if (!sqlStatement.isBlank()) {
                    statement.execute(sqlStatement);
                }
            }
        }
    }

    private String stripSqlComments(String sql) {
        StringBuilder cleaned = new StringBuilder();
        for (String line : sql.split("\\R")) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("--")) {
                cleaned.append(line).append('\n');
            }
        }
        return cleaned.toString();
    }

    private List<String> splitSqlStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inBacktick = false;

        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            current.append(ch);

            if (ch == '\\' && i + 1 < sql.length() && (inSingleQuote || inDoubleQuote)) {
                current.append(sql.charAt(++i));
                continue;
            }

            if (ch == '\'' && !inDoubleQuote && !inBacktick) {
                if (inSingleQuote && i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                    current.append(sql.charAt(++i));
                } else {
                    inSingleQuote = !inSingleQuote;
                }
            } else if (ch == '"' && !inSingleQuote && !inBacktick) {
                inDoubleQuote = !inDoubleQuote;
            } else if (ch == '`' && !inSingleQuote && !inDoubleQuote) {
                inBacktick = !inBacktick;
            } else if (ch == ';' && !inSingleQuote && !inDoubleQuote && !inBacktick) {
                statements.add(current.substring(0, current.length() - 1).trim());
                current.setLength(0);
            }
        }

        if (!current.toString().isBlank()) {
            statements.add(current.toString().trim());
        }
        return statements;
    }

    private void writeBackupFile(Path backupFile) throws SQLException, IOException {
        try (Connection connection = dataSource.getConnection();
             BufferedWriter writer = Files.newBufferedWriter(backupFile, StandardCharsets.UTF_8)) {

            String databaseName = connection.getCatalog();
            if (databaseName == null || databaseName.isBlank()) {
                databaseName = "local";
            }

            writer.write("-- Jewellery Store Manager backup");
            writer.newLine();
            writer.write("-- Generated at " + LocalDateTime.now());
            writer.newLine();
            writer.write("CREATE DATABASE IF NOT EXISTS `" + databaseName + "`;");
            writer.newLine();
            writer.write("USE `" + databaseName + "`;");
            writer.newLine();
            writer.write("SET FOREIGN_KEY_CHECKS=0;");
            writer.newLine();
            writer.newLine();

            for (String tableName : getTableNames(connection, databaseName)) {
                writeTableBackup(connection, tableName, writer);
            }

            writer.write("SET FOREIGN_KEY_CHECKS=1;");
            writer.newLine();
        }
    }

    private List<String> getTableNames(Connection connection, String databaseName) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet tables = metaData.getTables(databaseName, null, "%", new String[] { "TABLE" })) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                if (tableName != null) {
                    tableNames.add(tableName);
                }
            }
        }
        return tableNames;
    }

    private void writeTableBackup(Connection connection, String tableName, BufferedWriter writer)
            throws SQLException, IOException {
        writer.write("-- Table: `" + tableName + "`");
        writer.newLine();
        writer.write("DROP TABLE IF EXISTS `" + tableName + "`;");
        writer.newLine();
        writer.write(getCreateTableStatement(connection, tableName) + ";");
        writer.newLine();

        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT * FROM `" + tableName + "`")) {

            ResultSetMetaData metaData = rows.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rows.next()) {
                StringBuilder insert = new StringBuilder("INSERT INTO `")
                        .append(tableName)
                        .append("` (");

                for (int i = 1; i <= columnCount; i++) {
                    insert.append('`').append(metaData.getColumnName(i)).append('`');
                    if (i < columnCount) {
                        insert.append(", ");
                    }
                }

                insert.append(") VALUES (");

                for (int i = 1; i <= columnCount; i++) {
                    insert.append(toSqlLiteral(rows.getObject(i), metaData.getColumnName(i), metaData.getColumnTypeName(i)));
                    if (i < columnCount) {
                        insert.append(", ");
                    }
                }

                insert.append(");");
                writer.write(insert.toString());
                writer.newLine();
            }
        }

        writer.newLine();
    }

    private String getCreateTableStatement(Connection connection, String tableName) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SHOW CREATE TABLE `" + tableName + "`")) {
            if (rs.next()) {
                return rs.getString("Create Table");
            }
        }
        throw new SQLException("Could not fetch CREATE TABLE for " + tableName);
    }

    private String toSqlLiteral(Object value, String columnName, String columnTypeName) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number || value instanceof BigDecimal) {
            return Objects.toString(value);
        }
        if (value instanceof Boolean bool) {
            return bool ? "1" : "0";
        }
        if (value instanceof byte[] bytes) {
            if ("JSON".equalsIgnoreCase(columnTypeName)
                    || "custom_fields".equalsIgnoreCase(columnName)
                    || "product_snapshot".equalsIgnoreCase(columnName)
                    || "estimate_snapshot".equalsIgnoreCase(columnName)
                    || isJsonBytes(bytes)) {
                return "'" + escapeSql(new String(bytes, StandardCharsets.UTF_8)) + "'";
            }
            StringBuilder hex = new StringBuilder("0x");
            for (byte b : bytes) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        }
        if (value instanceof Timestamp timestamp) {
            return "'" + escapeSql(timestamp.toLocalDateTime().toString().replace('T', ' ')) + "'";
        }
        return "'" + escapeSql(String.valueOf(value)) + "'";
    }

    private boolean isJsonBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return false;
        }
        // Find first non-whitespace byte
        int start = 0;
        while (start < bytes.length && bytes[start] <= 32) {
            start++;
        }
        if (start >= bytes.length) return false;

        int end = bytes.length - 1;
        while (end >= 0 && bytes[end] <= 32) {
            end--;
        }
        if (end <= start) return false;

        char first = (char) bytes[start];
        char last = (char) bytes[end];
        return (first == '{' && last == '}') || (first == '[' && last == ']');
    }

    private String escapeSql(String value) {
        return value.replace("\\", "\\\\").replace("'", "''");
    }

    public String browseBackupDirectory() {
        try {
            String script = "$shell = New-Object -ComObject Shell.Application; " +
                            "$folder = $shell.BrowseForFolder(0, 'Select Database Backup Folder', 64, 17); " +
                            "if ($folder) { $folder.Self.Path }";
            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-ExecutionPolicy", "Bypass",
                "-Command",
                script
            );
            Process process = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            process.waitFor();
            String selectedPath = sb.toString().trim();
            if (!selectedPath.isEmpty()) {
                return selectedPath;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class BackupRunResult {
        private final boolean success;
        private final String trigger;
        private final String backupFile;
        private final String error;

        BackupRunResult(boolean success, String trigger, String backupFile, String error) {
            this.success = success;
            this.trigger = trigger;
            this.backupFile = backupFile;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTrigger() {
            return trigger;
        }

        public String getBackupFile() {
            return backupFile;
        }
        public String getError() {
            return error;
        }
    }

    private void unzip(Path zipFilePath, Path destDirectory) throws IOException {
        Files.createDirectories(destDirectory);
        try (InputStream is = Files.newInputStream(zipFilePath);
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path resolvedPath = destDirectory.resolve(entry.getName()).normalize();
                if (!resolvedPath.startsWith(destDirectory)) {
                    throw new IOException("ZIP entry escapes destination directory: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    Files.createDirectories(resolvedPath.getParent());
                    Files.copy(zis, resolvedPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> walk = Files.walk(source)) {
            List<Path> sources = walk.toList();
            for (Path src : sources) {
                Path dest = target.resolve(source.relativize(src));
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) return;
        try (Stream<Path> walk = Files.walk(directory)) {
            List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
    }
}

