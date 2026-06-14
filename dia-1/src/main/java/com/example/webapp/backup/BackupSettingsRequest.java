package com.example.webapp.backup;

public class BackupSettingsRequest {

    private boolean enabled;
    private BackupFrequency frequency;
    private String backupDirectory;

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
}
