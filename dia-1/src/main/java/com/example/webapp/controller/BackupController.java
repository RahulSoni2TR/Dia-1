package com.example.webapp.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestParam;
import com.example.webapp.backup.BackupImportRequest;
import com.example.webapp.backup.BackupService;
import com.example.webapp.backup.BackupSettingsRequest;
import com.example.webapp.service.ProductService;

@RestController
@RequestMapping("/api/backups")
public class BackupController {

    private final BackupService backupService;
    private final ProductService productService;

    public BackupController(BackupService backupService, ProductService productService) {
        this.backupService = backupService;
        this.productService = productService;
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(backupService.getSettingsView());
    }

    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> updateConfig(@RequestBody BackupSettingsRequest request) {
        return ResponseEntity.ok(backupService.updateSettings(request));
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runBackupNow() {
        return ResponseEntity.ok(backupService.runBackupNow("manual"));
    }

    @PostMapping("/run-due")
    public ResponseEntity<Map<String, Object>> runDueBackup() {
        return ResponseEntity.ok(backupService.runDueBackupIfNeeded());
    }

    @GetMapping("/files")
    public ResponseEntity<Map<String, Object>> listBackups() {
        return ResponseEntity.ok(backupService.listBackups());
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importBackup(@RequestBody BackupImportRequest request) {
        return ResponseEntity.ok(backupService.importBackup(request));
    }

    @PostMapping("/browse-directory")
    public ResponseEntity<Map<String, Object>> browseDirectory() {
        String path = backupService.browseBackupDirectory();
        if (path != null) {
            return ResponseEntity.ok(Map.of("success", true, "path", path));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "No folder selected"));
        }
    }

    @PostMapping("/regenerate-qrs")
    public ResponseEntity<Map<String, Object>> regenerateQrs(@RequestParam(value = "customBaseUrl", required = false) String customBaseUrl) {
        try {
            int count = productService.regenerateAllQRCodes(customBaseUrl);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", count,
                "message", "QR codes regenerated successfully."
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "error", e.getMessage(),
                "message", "QR code regeneration failed."
            ));
        }
    }
}
