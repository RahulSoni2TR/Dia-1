package com.example.webapp.backup;

public class BackupSettingsRequest {

    private boolean enabled;
    private BackupFrequency frequency;

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
}
