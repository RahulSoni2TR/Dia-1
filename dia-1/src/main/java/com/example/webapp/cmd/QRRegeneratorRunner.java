package com.example.webapp.cmd;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.webapp.models.Product;
import com.example.webapp.repository.ProductRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/*@Component*/
public class QRRegeneratorRunner implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.qr-dir}")
    private String qrDir;

    @Value("${app.qr-public-path}")
    private String qrPublicPath;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Regenerating QR Codes for all products...");

        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            try {
                // 1. Create QR URL
                String qrUrl = baseUrl + "/loadProductByDesignNo/" + product.getDesignNo();

                // 2. Ensure folder exists
                Files.createDirectories(Paths.get(qrDir));
                String qrFileName = "QR_" + product.getDesignNo() + ".png";
                String qrFilePath = qrDir + qrFileName;

                // 3. Generate QR image file
                generateQRCodeImage(qrUrl, qrFilePath);

                // 4. Set public path
                String qrPath = qrPublicPath + qrFileName;
                product.setQrCodePath(qrPath);

                System.out.println("Updated QR for: " + product.getDesignNo());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        productRepository.saveAll(products);
        System.out.println("✅ QR regeneration complete!");
    }
    
	public static String generateQRCodeImage(String text, String filePath) 
	        throws WriterException, IOException {

	    QRCodeWriter qrCodeWriter = new QRCodeWriter();

	    Map<EncodeHintType, Object> hints = new HashMap<>();
	    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
	    hints.put(EncodeHintType.MARGIN, 1);

	    // Generate higher resolution (300x300 px)
	    BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300, hints);

	    Path path = FileSystems.getDefault().getPath(filePath);
	    MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

	    return filePath;
	}

}

