package com.example.webapp.controller;

import com.example.webapp.service.LicenseService;
import com.example.webapp.service.LicenseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/license")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        LicenseStatus status = licenseService.getLicenseStatus();
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.name());
        response.put("machineId", licenseService.getMachineId());
        response.put("daysRemaining", licenseService.getDaysRemaining());
        response.put("expiryDate", licenseService.getExpiryDate());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/activate")
    public ResponseEntity<Map<String, Object>> activate(@RequestBody(required = false) Map<String, String> payload,
                                                        @RequestParam(value = "licenseKey", required = false) String licenseKeyParam) {
        String licenseKey = null;
        if (payload != null && payload.containsKey("licenseKey")) {
            licenseKey = payload.get("licenseKey");
        } else {
            licenseKey = licenseKeyParam;
        }

        Map<String, Object> response = new HashMap<>();
        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "License key cannot be empty.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean success = licenseService.activateLicense(licenseKey);
        if (success) {
            response.put("success", true);
            response.put("message", "License activated successfully! Please restart the application.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid license key for this machine or the key has expired.");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
