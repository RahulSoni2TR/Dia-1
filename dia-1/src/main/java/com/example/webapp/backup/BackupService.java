package com.example.webapp.backup;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final Path backupDirectory;
    private final Path settingsFile;
    private final Object lock = new Object();

    public BackupService(
            DataSource dataSource,
            @Value("${app.backup.dir:}") String configuredBackupDir) {
        this.dataSource = dataSource;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        Path rootDir;
        if (configuredBackupDir != null && !configuredBackupDir.isBlank()) {
            rootDir = Paths.get(configuredBackupDir);
        } else {
            rootDir = Paths.get(System.getProperty("user.home"), "JewelleryStoreManager", "backups");
        }

        this.backupDirectory = rootDir;
        this.settingsFile = rootDir.resolve("backup-settings.json");
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
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("backupDirectory", backupDirectory.toString());
            response.put("backups", getBackupFiles());
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
                executeSqlFile(importFile);

                response.put("success", true);
                response.put("importedFile", importFile.toString());
                response.put("preImportBackupFile", safetyBackup.getBackupFile());
                response.put("message", "Backup imported successfully.");
            } catch (Exception ex) {
                response.put("success", false);
                response.put("error", ex.getMessage());
                response.put("message", "Backup import failed.");
            }
            response.put("backups", getBackupFiles());
            return response;
        }
    }

    private BackupRunResult executeBackup(BackupSettings settings, String trigger) {
        try {
            ensureDirectory();
            String fileName = "db-backup-" + FILE_TIMESTAMP.format(LocalDateTime.now()) + ".sql";
            Path backupFile = backupDirectory.resolve(fileName);
            writeBackupFile(backupFile);
            settings.setLastBackupAt(LocalDateTime.now());
            settings.setLastBackupFile(backupFile.toString());
            saveSettings(settings);
            return new BackupRunResult(true, trigger, backupFile.toString(), null);
        } catch (Exception ex) {
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
            ensureDirectory();
            if (Files.exists(settingsFile)) {
                BackupSettings settings = objectMapper.readValue(settingsFile.toFile(), BackupSettings.class);
                if (settings.getFrequency() == null) {
                    settings.setFrequency(BackupFrequency.WEEKLY);
                }
                settings.setBackupDirectory(backupDirectory.toString());
                return settings;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        BackupSettings defaults = new BackupSettings();
        defaults.setEnabled(true);
        defaults.setFrequency(BackupFrequency.WEEKLY);
        defaults.setBackupDirectory(backupDirectory.toString());
        saveSettings(defaults);
        return defaults;
    }

    private void saveSettings(BackupSettings settings) {
        try {
            ensureDirectory();
            settings.setBackupDirectory(backupDirectory.toString());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile.toFile(), settings);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save backup settings", ex);
        }
    }

    private void ensureDirectory() throws IOException {
        Files.createDirectories(backupDirectory);
    }

    private List<Map<String, Object>> getBackupFiles() {
        try {
            ensureDirectory();
            try (Stream<Path> files = Files.list(backupDirectory)) {
                return files
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().startsWith("db-backup-"))
                        .filter(path -> path.getFileName().toString().endsWith(".sql"))
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
        ensureDirectory();
        if (request == null || request.isLatest()) {
            return getBackupFiles().stream()
                    .findFirst()
                    .map(file -> backupDirectory.resolve(String.valueOf(file.get("name"))))
                    .orElseThrow(() -> new IllegalArgumentException("No backup files are available to import."));
        }

        String requestedFile = request.getBackupFile();
        if (requestedFile == null || requestedFile.isBlank()) {
            throw new IllegalArgumentException("Select a backup file to import.");
        }

        Path resolved = backupDirectory.resolve(Paths.get(requestedFile).getFileName()).normalize();
        Path normalizedDirectory = backupDirectory.toAbsolutePath().normalize();
        Path normalizedFile = resolved.toAbsolutePath().normalize();
        if (!normalizedFile.startsWith(normalizedDirectory)) {
            throw new IllegalArgumentException("Backup file must be inside the configured backup folder.");
        }
        if (!Files.isRegularFile(normalizedFile)) {
            throw new IllegalArgumentException("Selected backup file was not found.");
        }
        if (!normalizedFile.getFileName().toString().startsWith("db-backup-")
                || !normalizedFile.getFileName().toString().endsWith(".sql")) {
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
                    insert.append(toSqlLiteral(rows.getObject(i)));
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

    private String toSqlLiteral(Object value) {
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

    private String escapeSql(String value) {
        return value.replace("\\", "\\\\").replace("'", "''");
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
}
