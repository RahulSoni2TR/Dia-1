package com.example.webapp.backup;

public class BackupImportRequest {

    private boolean latest = true;
    private String backupFile;

    public boolean isLatest() {
        return latest;
    }

    public void setLatest(boolean latest) {
        this.latest = latest;
    }

    public String getBackupFile() {
        return backupFile;
    }

    public void setBackupFile(String backupFile) {
        this.backupFile = backupFile;
    }
}
