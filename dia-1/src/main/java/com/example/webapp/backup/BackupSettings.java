package com.example.webapp.backup;

import java.time.LocalDateTime;

public class BackupSettings {

    private boolean enabled = true;
    private BackupFrequency frequency = BackupFrequency.WEEKLY;
    private String backupDirectory;
    private LocalDateTime lastBackupAt;
    private String lastBackupFile;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public BackupFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(BackupFrequency frequency) {
        this.frequency = frequency;
    }

    public String getBackupDirectory() {
        return backupDirectory;
    }

    public void setBackupDirectory(String backupDirectory) {
        this.backupDirectory = backupDirectory;
    }

    public LocalDateTime getLastBackupAt() {
        return lastBackupAt;
    }

    public void setLastBackupAt(LocalDateTime lastBackupAt) {
        this.lastBackupAt = lastBackupAt;
    }

    public String getLastBackupFile() {
        return lastBackupFile;
    }

    public void setLastBackupFile(String lastBackupFile) {
        this.lastBackupFile = lastBackupFile;
    }
}
