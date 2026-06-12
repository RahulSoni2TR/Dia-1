package com.example.webapp.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Enumeration;

@Service
public class LicenseService {

    // Hardcoded RSA 2048-bit Public Key in Base64
    private static final String PUBLIC_KEY_BASE64 = 
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApJbDiGBy3yLmz+ert9XkYb2IldwwvZ4hBCbaHGwKv+nFBk83/" +
        "Hm9Qu50oODm6HnGzutyUKY7XHndL3T/JhWDDVTY0X2/bJxZyv4ANjJ9kFiEukNbmFaOdX1YbIXlSXxRZhqCp3i8dycHHn" +
        "NCmXywnQ5rO/2auu1mcnRDNpEDNcRsgieURGdIqPi6rrlif0g3t+I9sG19o3pgjCmJ/UW0mCi7livcLJ9wbfbiI0hyfSx" +
        "RLUD1YKPxCb/XzFYJDG0Nptb6ipsn9SU8X8bBjxxZR/bjxHxIYN5lyRMtODeAceUWjbtMGbOtcCQnxAufInRjVMSZcAa3" +
        "lnEz05hYLo2/hwIDAQAB";

    private static final String TRIAL_SECRET_SALT = "Dia1ProductManagerTrialSecretKeySalt_2026";
    private static final long TRIAL_DURATION_MS = 10L * 24 * 60 * 60 * 1000L; // 10 days

    private final File primaryTrialFile;
    private final File backupTrialFile;
    private final File licenseFile;

    public LicenseService() {
        this(
            new File(System.getProperty("user.home"), ".productmanager"),
            new File(System.getProperty("java.io.tmpdir"))
        );
    }

    LicenseService(File baseDir, File backupDir) {
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        this.primaryTrialFile = new File(baseDir, "trial.dat");
        this.licenseFile = new File(baseDir, "license.lic");
        this.backupTrialFile = new File(backupDir, "trial_pm.dat");
    }

    /**
     * Gets the unique hardware-based Machine ID using hashed MAC address.
     */
    public String getMachineId() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp() || ni.getHardwareAddress() == null) {
                    continue;
                }
                byte[] mac = ni.getHardwareAddress();
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(mac);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 8; i++) {
                    sb.append(String.format("%02X", hash[i]));
                    if (i % 2 == 1 && i < 7) {
                        sb.append("-");
                    }
                }
                return sb.toString();
            }
        } catch (Exception e) {
            // Fallback
        }
        return "OFFLINE-DESKTOP-ID";
    }

    /**
     * Checks the current license and trial status.
     */
    public LicenseStatus getLicenseStatus() {
        // 1. Check if license.lic is valid
        if (isLicenseValid()) {
            return LicenseStatus.LICENSED;
        }

        // 2. Check trial status
        TrialData trial = getAndSyncTrialData();
        if (trial.tampered) {
            return LicenseStatus.TAMPERED;
        }

        long now = System.currentTimeMillis();
        // Clock rollback check
        if (now < trial.lastSeen) {
            trial.tampered = true;
            saveTrialData(trial);
            return LicenseStatus.TAMPERED;
        }

        // Expiry check
        if (now - trial.firstLaunch > TRIAL_DURATION_MS) {
            return LicenseStatus.EXPIRED;
        }

        // Update last seen
        trial.lastSeen = Math.max(trial.lastSeen, now);
        saveTrialData(trial);

        return LicenseStatus.TRIAL;
    }

    /**
     * Returns remaining trial days.
     */
    public int getDaysRemaining() {
        TrialData trial = getAndSyncTrialData();
        if (trial.tampered) return 0;
        long remainingMs = TRIAL_DURATION_MS - (System.currentTimeMillis() - trial.firstLaunch);
        if (remainingMs <= 0) return 0;
        return (int) Math.ceil((double) remainingMs / (24 * 60 * 60 * 1000L));
    }

    /**
     * Activates the license key.
     */
    public boolean activateLicense(String licenseKey) {
        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            return false;
        }
        if (verifyLicenseKey(licenseKey)) {
            try {
                FileWriter writer = new FileWriter(licenseFile);
                writer.write(licenseKey.trim());
                writer.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Gets the license expiry date if licensed.
     */
    public String getExpiryDate() {
        if (!licenseFile.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(licenseFile))) {
            String key = reader.readLine();
            if (key == null) return null;
            String[] parts = key.split("\\.");
            if (parts.length != 2) return null;
            String data = new String(decodeBase64(parts[0]), StandardCharsets.UTF_8);
            String[] dataParts = data.split(";");
            if (dataParts.length >= 2) {
                return dataParts[1];
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    // Helper classes and methods

    private static class TrialData {
        long firstLaunch;
        long lastSeen;
        boolean tampered;

        TrialData(long firstLaunch, long lastSeen, boolean tampered) {
            this.firstLaunch = firstLaunch;
            this.lastSeen = lastSeen;
            this.tampered = tampered;
        }
    }

    private TrialData getAndSyncTrialData() {
        TrialData primary = readTrialFile(primaryTrialFile);
        TrialData backup = readTrialFile(backupTrialFile);

        if (primary == null && backup == null) {
            // First launch
            long now = System.currentTimeMillis();
            TrialData newTrial = new TrialData(now, now, false);
            saveTrialData(newTrial);
            return newTrial;
        }

        if (primary != null && backup != null) {
            if (primary.tampered || backup.tampered) {
                // If either is marked tampered, overall is tampered
                primary.tampered = true;
                backup.tampered = true;
                saveTrialData(primary);
                return primary;
            }
            // Sync if needed, taking the latest lastSeen
            long maxLastSeen = Math.max(primary.lastSeen, backup.lastSeen);
            long minFirstLaunch = Math.min(primary.firstLaunch, backup.firstLaunch);
            TrialData synced = new TrialData(minFirstLaunch, maxLastSeen, false);
            if (primary.lastSeen != maxLastSeen || primary.firstLaunch != minFirstLaunch) {
                saveTrialData(synced);
            }
            return synced;
        }

        // One is missing/corrupt, restore it from the other
        TrialData valid = (primary != null) ? primary : backup;
        saveTrialData(valid);
        return valid;
    }

    private TrialData readTrialFile(File file) {
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String firstLaunchStr = br.readLine();
            String lastSeenStr = br.readLine();
            String tamperedStr = br.readLine();
            String checksum = br.readLine();

            if (firstLaunchStr == null || lastSeenStr == null || tamperedStr == null || checksum == null) {
                return null;
            }

            long firstLaunch = Long.parseLong(firstLaunchStr.trim());
            long lastSeen = Long.parseLong(lastSeenStr.trim());
            boolean tampered = Boolean.parseBoolean(tamperedStr.trim());

            String expectedChecksum = computeChecksum(firstLaunch, lastSeen, tampered);
            if (!expectedChecksum.equals(checksum.trim())) {
                return new TrialData(firstLaunch, lastSeen, true); // tampered
            }

            return new TrialData(firstLaunch, lastSeen, tampered);
        } catch (Exception e) {
            return null;
        }
    }

    private void saveTrialData(TrialData data) {
        writeTrialFile(primaryTrialFile, data);
        writeTrialFile(backupTrialFile, data);
    }

    private void writeTrialFile(File file, TrialData data) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println(data.firstLaunch);
            pw.println(data.lastSeen);
            pw.println(data.tampered);
            pw.println(computeChecksum(data.firstLaunch, data.lastSeen, data.tampered));
        } catch (Exception e) {
            // ignore
        }
    }

    private String computeChecksum(long firstLaunch, long lastSeen, boolean tampered) {
        try {
            String data = firstLaunch + ":" + lastSeen + ":" + tampered + ":" + TRIAL_SECRET_SALT;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isLicenseValid() {
        if (!licenseFile.exists()) {
            return false;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(licenseFile))) {
            String key = br.readLine();
            return verifyLicenseKey(key);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean verifyLicenseKey(String key) {
        if (key == null) return false;
        key = key.trim();
        String[] parts = key.split("\\.");
        if (parts.length != 2) {
            return false;
        }

        try {
            String dataStr = new String(decodeBase64(parts[0]), StandardCharsets.UTF_8);
            String signatureBase64 = parts[1];

            // 1. Verify RSA Signature
            if (!verifySignature(dataStr, signatureBase64)) {
                return false;
            }

            // 2. Parse and verify data fields
            String[] dataParts = dataStr.split(";");
            if (dataParts.length < 2) {
                return false;
            }

            String machineId = dataParts[0];
            String expiryStr = dataParts[1];

            // Verify Machine ID
            if (!getMachineId().equals(machineId)) {
                return false;
            }

            // Verify Expiry
            if (!"perpetual".equalsIgnoreCase(expiryStr)) {
                LocalDate expiry = LocalDate.parse(expiryStr, DateTimeFormatter.ISO_LOCAL_DATE);
                if (LocalDate.now().isAfter(expiry)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean verifySignature(String data, String signatureBase64) {
        try {
            byte[] pubKeyBytes = decodeBase64(PUBLIC_KEY_BASE64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data.getBytes(StandardCharsets.UTF_8));
            return sig.verify(decodeBase64(signatureBase64));
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] decodeBase64(String base64Str) {
        try {
            return Base64.getDecoder().decode(base64Str);
        } catch (IllegalArgumentException e) {
            return Base64.getUrlDecoder().decode(base64Str);
        }
    }
}
