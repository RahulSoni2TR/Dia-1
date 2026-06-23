package com.example.webapp.service;

import com.example.webapp.backup.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class BackupServiceTest {

    @TempDir
    Path tempBackupDir;

    @TempDir
    Path tempUploadsDir;

    private DataSource mockDataSource;
    private Connection mockConnection;
    private DatabaseMetaData mockMetaData;
    private Statement mockStatement;
    private ResultSet mockTablesResultSet;
    private ResultSet mockCreateTableResultSet;
    private ResultSet mockRowsResultSet;
    private ResultSetMetaData mockRowsMetaData;

    private BackupService backupService;

    @BeforeEach
    public void setUp() throws Exception {
        mockDataSource = Mockito.mock(DataSource.class);
        mockConnection = Mockito.mock(Connection.class);
        mockMetaData = Mockito.mock(DatabaseMetaData.class);
        mockStatement = Mockito.mock(Statement.class);
        mockTablesResultSet = Mockito.mock(ResultSet.class);
        mockCreateTableResultSet = Mockito.mock(ResultSet.class);
        mockRowsResultSet = Mockito.mock(ResultSet.class);
        mockRowsMetaData = Mockito.mock(ResultSetMetaData.class);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.getCatalog()).thenReturn("testdb");
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        // Mock tables metadata
        when(mockMetaData.getTables(anyString(), any(), anyString(), any())).thenReturn(mockTablesResultSet);
        when(mockTablesResultSet.next()).thenReturn(true, false); // One table, then end
        when(mockTablesResultSet.getString("TABLE_NAME")).thenReturn("products");

        // Mock SHOW CREATE TABLE
        when(mockStatement.executeQuery(startsWith("SHOW CREATE TABLE"))).thenReturn(mockCreateTableResultSet);
        when(mockCreateTableResultSet.next()).thenReturn(true);
        when(mockCreateTableResultSet.getString("Create Table")).thenReturn("CREATE TABLE products (id INT, name VARCHAR(255))");

        // Mock SELECT * FROM products
        when(mockStatement.executeQuery(contains("SELECT * FROM"))).thenReturn(mockRowsResultSet);
        when(mockRowsResultSet.getMetaData()).thenReturn(mockRowsMetaData);
        when(mockRowsMetaData.getColumnCount()).thenReturn(2);
        when(mockRowsMetaData.getColumnName(1)).thenReturn("id");
        when(mockRowsMetaData.getColumnName(2)).thenReturn("name");
        when(mockRowsResultSet.next()).thenReturn(true, false); // One row, then end
        when(mockRowsResultSet.getObject(1)).thenReturn(1);
        when(mockRowsResultSet.getObject(2)).thenReturn("Gold Ring");

        // Create the backup service instance
        backupService = new BackupService(
                mockDataSource,
                tempBackupDir.toString(),
                tempUploadsDir.toString()
        );
    }

    @Test
    public void testExecuteBackupCreatesZipWithDatabaseSqlAndUploads() throws Exception {
        // Create some files in the uploads directory
        Path uploadsFile1 = tempUploadsDir.resolve("image1.jpg");
        Files.writeString(uploadsFile1, "fake-image-data-1");
        
        Path subDir = tempUploadsDir.resolve("qr_codes");
        Files.createDirectories(subDir);
        Path uploadsFile2 = subDir.resolve("qr1.png");
        Files.writeString(uploadsFile2, "fake-qr-data");

        // Trigger manual backup
        Map<String, Object> result = backupService.runBackupNow("manual");
        
        assertTrue((Boolean) result.get("success"));
        String backupFilePath = (String) result.get("backupFile");
        assertNotNull(backupFilePath);
        
        Path zipPath = Path.of(backupFilePath);
        assertTrue(Files.exists(zipPath));
        assertTrue(zipPath.getFileName().toString().endsWith(".zip"));

        // Verify the zip contents
        boolean foundSql = false;
        boolean foundImage = false;
        boolean foundQr = false;

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.equals("database.sql")) {
                    foundSql = true;
                    String sqlContent = new String(zis.readAllBytes());
                    assertTrue(sqlContent.contains("Gold Ring"));
                    assertTrue(sqlContent.contains("CREATE TABLE products"));
                } else if (name.equals("uploads/image1.jpg")) {
                    foundImage = true;
                    String content = new String(zis.readAllBytes());
                    assertEquals("fake-image-data-1", content);
                } else if (name.equals("uploads/qr_codes/qr1.png")) {
                    foundQr = true;
                    String content = new String(zis.readAllBytes());
                    assertEquals("fake-qr-data", content);
                }
            }
        }

        assertTrue(foundSql, "database.sql should be present in ZIP");
        assertTrue(foundImage, "image1.jpg should be present under uploads/ inside ZIP");
        assertTrue(foundQr, "qr1.png should be present under uploads/qr_codes/ inside ZIP");
    }

    @Test
    public void testImportZipBackupRestoresSqlAndUploads() throws Exception {
        // Prepare a backup ZIP manually
        Path zipPath = tempBackupDir.resolve("db-backup-test.zip");
        Path tempSql = Files.createTempFile("temp-sql-", ".sql");
        Files.writeString(tempSql, "CREATE TABLE products (id INT); INSERT INTO products VALUES(1);");
        
        try (java.io.OutputStream os = Files.newOutputStream(zipPath);
             java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(os)) {
            
            // Add database.sql
            ZipEntry sqlEntry = new ZipEntry("database.sql");
            zos.putNextEntry(sqlEntry);
            Files.copy(tempSql, zos);
            zos.closeEntry();

            // Add upload file
            ZipEntry imageEntry = new ZipEntry("uploads/restored_image.jpg");
            zos.putNextEntry(imageEntry);
            zos.write("restored-image-data".getBytes());
            zos.closeEntry();
        }
        Files.delete(tempSql);

        // Setup mock connection for statement execution when importing
        Statement mockImportStatement = Mockito.mock(Statement.class);
        when(mockConnection.createStatement()).thenReturn(mockImportStatement);

        // Run import
        BackupImportRequest request = new BackupImportRequest();
        request.setBackupFile(zipPath.getFileName().toString());
        request.setLatest(false);

        Map<String, Object> result = backupService.importBackup(request);
        
        assertTrue((Boolean) result.get("success"), "Import should succeed");
        
        // Verify SQL execution calls
        Mockito.verify(mockImportStatement).execute("CREATE TABLE products (id INT)");
        Mockito.verify(mockImportStatement).execute("INSERT INTO products VALUES(1)");

        // Verify uploads restore
        Path restoredImagePath = tempUploadsDir.resolve("restored_image.jpg");
        assertTrue(Files.exists(restoredImagePath));
        assertEquals("restored-image-data", Files.readString(restoredImagePath));
    }

    @Test
    public void testImportLegacySqlBackupRestoresDatabaseOnly() throws Exception {
        // Create raw SQL backup
        Path sqlPath = tempBackupDir.resolve("db-backup-legacy.sql");
        Files.writeString(sqlPath, "CREATE TABLE legacy_table (id INT);");

        Statement mockImportStatement = Mockito.mock(Statement.class);
        when(mockConnection.createStatement()).thenReturn(mockImportStatement);

        BackupImportRequest request = new BackupImportRequest();
        request.setBackupFile(sqlPath.getFileName().toString());
        request.setLatest(false);

        Map<String, Object> result = backupService.importBackup(request);
        
        assertTrue((Boolean) result.get("success"), "Import should succeed");
        Mockito.verify(mockImportStatement).execute("CREATE TABLE legacy_table (id INT)");
    }
}
