package com.example.webapp.cmd;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Command-line utility to generate offline license keys for the Product Manager app.
 * Run via:
 * mvnw exec:java -Dexec.mainClass="com.example.webapp.cmd.LicenseGenerator" -Dexec.args="<machineId> <expiryDate>"
 * E.g.:
 * mvnw exec:java -Dexec.mainClass="com.example.webapp.cmd.LicenseGenerator" -Dexec.args="ABCD-EFGH perpetual"
 */
public class LicenseGenerator {

    // Hardcoded RSA 2048-bit Private Key in Base64 (Keep secure, only compiled in administrator builds)
    private static final String PRIVATE_KEY_BASE64 = 
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCklsOIYHLfIubP56u31eRhvYiV3DC9niEEJtocbAq/6cUGTzf8eb1C7nSg4OboecbO63JQpjtced0vdP8mFYMNVNjRfb9snFnK/gA2Mn2QWIS6Q1uYVo51fVhsheVJfFFmGoKneLx3Jwcec0KZfLCdDms7/Zq67WZydEM2kQM1xGyCJ5REZ0io+LquuWJ/SDe34j2wbX2jemCMKYn9RbSYKLuWK9wsn3Bt9uIjSHJ9LFEtQPVgo/EJv9fMVgkMbQ2m1vqKmyf1JTxfxsGPHFlH9uPEfEhg3mXJEy04N4Bx5RaNu0wZs61wJCfEC58idGNUxJlwBreWcTPTmFgujb+HAgMBAAECggEAE+o2zEHDVH2Fc8LtSQgFTiWf/mYPXbo29ijPUQtqUjtdKXzHYkdxX5pjxp2VQwGNIoy5nx7mEDP3T/Pg9ZY6bmVa+3Tdhyz0mSEaccmyiMHf2XvR/ED+Ps1vuBM2zFP8XhWMw+p81LwGWqheoXIaxVOGen1JTh9F5fPBuDtb/z12+nIl+OPmi+6MMEAUo5ANDgvzNe/bkv1Dj5piRC0o69IcQld+wjlOfrJZKVxa8p0s3LKTZLpS3udCwEvoS2Pv402JWx0hRLPekWmWQXp1zhIa26TmNcnS4r1bU6wXLdj6PHR8X83+2gmdJcaRc9F/hObSoz+1WFFVLgNg2LhS3QKBgQC2WgYGuGPCdxur9f+tC4T6fz+bY9UR4/gOgbav7CquKtHnURdzpZcVwa9/HbE6h+Gx07nYqf/Q+yoOF6VXaYN4NeTg7G0zwMmCtDIPsYki7vqrYJwkqad/AmlxeXbEwIqsYzoj0R5PA3o7o9QN9UXFU0vxjSsY+FQ8/AHn6xMPXQKBgQDnEDBjwVE+FyQJ9KD+hp7icBeaieuf/hb89n56XImbnnI6x/lr9sSUVJ/Y4uFhEP3pwBaFkI+++WXSKCKQe5Ql7s3TJ7IHMUKSxq1zVhpD3Mt8XBIj2tqdTYuCQjfINxukjsNwr2z3gTUs1epyMDaCo2R+Y6VOOJJO5N7Lg51wMwKBgAihjjN3OtGTja3AARARwYORzlLukME+Bxm4rgr5pLOFt1W5kuCYb+RJvKLJpv/cOqSOHvfQZBliKgVsvRi8F8ry0hiLWEfg0ijrmor/njwXD6pY8ksR9KmgVZlXZHW/n1C1iaT0WvjmczyrbngSqfDDFo8iXW3bIzGXxAdUKxzJAoGAfJ9jqfnzKozqmCAD0SOkgDa61FP87L7rgSYlUzOj2HYd4AxJP2zJ28LEsAK2UlcKy88ZlpJApVz4COAyvECax9bD1lY7k9uCr41Osb1Hz0A/0+QIuKPqcxsG2ouCcI8gbqG9UYKcP+XFW1hI6auNSup7Yhu2ZbjnWHvimltzR7cCgYEAjcTP3SnK9rho/wjIK67oo2cGSLVN8pfOBlEPwLf7xb9HUKv/wWx4rdinSI2Gtc1pZZM4ZAGt/+qNzBgmckt25N/Rt7M3LeOWC4yUMmlaVfUvbXV33Im0WFqllULii4XMSr7NUvP3qNw587WE4pDeIjG0wxp/sv8Vq6w3Kvpn3Fo=";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Error: Insufficient arguments.");
            System.out.println("Usage: java LicenseGenerator <machineId> <expiryDate>");
            System.out.println("  - machineId: Output from getMachineId() (e.g. ABCD-EFGH)");
            System.out.println("  - expiryDate: yyyy-MM-dd (e.g. 2026-12-31) or 'perpetual'");
            System.exit(1);
        }

        String machineId = args[0].trim();
        String expiryDate = args[1].trim();
        String data = machineId + ";" + expiryDate;

        try {
            // Parse private key
            byte[] privKeyBytes = decodeBase64(PRIVATE_KEY_BASE64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // Sign data
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            sig.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = sig.sign();

            // Encode as URL safe strings
            String encodedData = Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes(StandardCharsets.UTF_8));
            String encodedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);

            String licenseKey = encodedData + "." + encodedSignature;
            System.out.println("\n------------------------------------------------------------");
            System.out.println("LICENSE GENERATED SUCCESSFULLY");
            System.out.println("------------------------------------------------------------");
            System.out.println("Machine ID : " + machineId);
            System.out.println("Expiry     : " + expiryDate);
            System.out.println("License Key:\n");
            System.out.println(licenseKey);
            System.out.println("------------------------------------------------------------\n");

        } catch (Exception e) {
            System.err.println("Error generating license key: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static byte[] decodeBase64(String base64Str) {
        base64Str = base64Str.replaceAll("\\s", "");
        try {
            return Base64.getDecoder().decode(base64Str);
        } catch (IllegalArgumentException e) {
            try {
                return Base64.getUrlDecoder().decode(base64Str);
            } catch (IllegalArgumentException e2) {
                return Base64.getMimeDecoder().decode(base64Str);
            }
        }
    }
}
