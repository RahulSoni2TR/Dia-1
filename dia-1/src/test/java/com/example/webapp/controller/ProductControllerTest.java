package com.example.webapp.controller;

import com.example.webapp.models.Orders;
import com.example.webapp.models.Product;
import com.example.webapp.repository.VerificationConfigRepository;
import com.example.webapp.service.LogService;
import com.example.webapp.service.ProductService;
import com.example.webapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ProductControllerTest {

    private MockMvc mockMvc;
    private ProductService mockProductService;
    private UserService mockUserService;
    private LogService mockLogService;
    private VerificationConfigRepository mockSettingRepository;

    @TempDir
    Path tempQrDir;

    @TempDir
    Path tempUploadDir;

    @BeforeEach
    public void setUp() {
        mockProductService = Mockito.mock(ProductService.class);
        mockUserService = Mockito.mock(UserService.class);
        mockLogService = Mockito.mock(LogService.class);
        mockSettingRepository = Mockito.mock(VerificationConfigRepository.class);

        ProductController controller = new ProductController();
        ReflectionTestUtils.setField(controller, "productService", mockProductService);
        ReflectionTestUtils.setField(controller, "userService", mockUserService);
        ReflectionTestUtils.setField(controller, "logService", mockLogService);
        ReflectionTestUtils.setField(controller, "verificationConfigRepository", mockSettingRepository);
        
        ReflectionTestUtils.setField(controller, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(controller, "qrDir", tempQrDir.toString() + "/");
        ReflectionTestUtils.setField(controller, "qrPublicPath", "/uploads/qr_codes/");
        ReflectionTestUtils.setField(controller, "uploadDir", tempUploadDir.toString() + "/");

        // Mock default order behavior (order id not present -> custom ID path)
        when(mockProductService.findByOrderId(anyString())).thenReturn(Optional.empty());

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testAddProductDiamondRings() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("imageUrl", "test.jpg", "image/jpeg", "image-bytes".getBytes());

        mockMvc.perform(multipart("/add")
                .file(mockFile)
                .param("productName", "Test Ring")
                .param("stockQuantity", "10")
                .param("categoryId", "1")
                .param("subCategoryId", "11")
                .param("karatId", "18")
                .param("orderId", "ORD-R1")
                .param("designNoDR", "DR-999")
                .param("diamondGross", "10.550")
                .param("net", "9.250")
                .param("pcs", "1")
                .param("diaWeight", "2.45")
                .param("diaRate", "45000.00")
                .param("diaSt", "1.10")
                .param("diaStRate", "12000.00")
                .param("diamondLabour", "350.00")
                .param("drLabourP", "15.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(mockProductService).addProduct(productCaptor.capture());

        Product p = productCaptor.getValue();
        assertEquals("Test Ring", p.getItem());
        assertEquals(10, p.getStockQuantity());
        assertEquals(1, p.getCategoryId());
        assertEquals(11L, p.getSubCategoryId());
        assertEquals(0, new BigDecimal("18").compareTo(p.getKarat()));
        assertEquals("DR-999", p.getDesignNo());
        assertEquals(0, new BigDecimal("10.550").compareTo(p.getGross()));
        assertEquals(0, new BigDecimal("9.250").compareTo(p.getNet()));
        assertEquals(1, p.getPcs());
        assertEquals(0, new BigDecimal("2.45").compareTo(p.getDiamondsCt()));
        assertEquals(0, new BigDecimal("45000.00").compareTo(p.getDiaRt()));
        assertEquals(0, new BigDecimal("1.10").compareTo(p.getOtherStonesCt()));
        assertEquals(0, new BigDecimal("12000.00").compareTo(p.getOtherStonesRt()));
        assertEquals(0, new BigDecimal("350.00").compareTo(p.getLabour()));
        assertEquals(0, new BigDecimal("15.00").compareTo(p.getLabourP()));
    }

    @Test
    public void testAddProductOpenSetting() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("imageUrl", "test.jpg", "image/jpeg", "image-bytes".getBytes());

        mockMvc.perform(multipart("/add")
                .file(mockFile)
                .param("productName", "Open Set")
                .param("stockQuantity", "5")
                .param("categoryId", "2")
                .param("subCategoryId", "14")
                .param("karatId", "22")
                .param("orderId", "ORD-OS1")
                .param("designNoOS", "OS-888")
                .param("gross", "15.420")
                .param("net", "12.110")
                .param("osLabour", "400.00")
                .param("vilandiCt", "3.20")
                .param("vilandiRate", "50000.00")
                .param("diamondsCt", "1.80")
                .param("diamondsCtRate", "48000.00")
                .param("beadsCt", "2.50")
                .param("vilandiBeadsRate", "1500.00")
                .param("pearlsGm", "4.100")
                .param("vilandiPearlRate", "2000.00")
                .param("ssPearlCts", "1.20")
                .param("osSSPearlRate", "800.00")
                .param("otherStonesCt", "0.90")
                .param("openStRate", "1200.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(mockProductService).addProduct(productCaptor.capture());

        Product p = productCaptor.getValue();
        assertEquals("Open Set", p.getItem());
        assertEquals(2, p.getCategoryId());
        assertEquals(14L, p.getSubCategoryId());
        assertEquals("OS-888", p.getDesignNo());
        assertEquals(0, new BigDecimal("15.420").compareTo(p.getGross()));
        assertEquals(0, new BigDecimal("12.110").compareTo(p.getNet()));
        assertEquals(0, new BigDecimal("400.00").compareTo(p.getLabour()));
        assertEquals(0, new BigDecimal("3.20").compareTo(p.getVilandiCt()));
        assertEquals(0, new BigDecimal("50000.00").compareTo(p.getvRate()));
        assertEquals(0, new BigDecimal("1.80").compareTo(p.getDiamondsCt()));
        assertEquals(0, new BigDecimal("48000.00").compareTo(p.getDiaRt()));
        assertEquals(0, new BigDecimal("2.50").compareTo(p.getBeadsCt()));
        assertEquals(0, new BigDecimal("1500.00").compareTo(p.getBdRate()));
        assertEquals(0, new BigDecimal("4.100").compareTo(p.getPearlsGm()));
        assertEquals(0, new BigDecimal("2000.00").compareTo(p.getPrlRate()));
        assertEquals(0, new BigDecimal("1.20").compareTo(p.getSsPearlCt()));
        assertEquals(0, new BigDecimal("800.00").compareTo(p.getSsRate()));
        assertEquals(0, new BigDecimal("0.90").compareTo(p.getOtherStonesCt()));
        assertEquals(0, new BigDecimal("1200.00").compareTo(p.getOtherStonesRt()));
    }

    @Test
    public void testAddProductChains() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("imageUrl", "test.jpg", "image/jpeg", "image-bytes".getBytes());

        mockMvc.perform(multipart("/add")
                .file(mockFile)
                .param("productName", "Chain")
                .param("stockQuantity", "1")
                .param("categoryId", "3")
                .param("subCategoryId", "16")
                .param("karatId", "22")
                .param("orderId", "ORD-CH1")
                .param("designNo", "CH-777")
                .param("chainGross", "20.350")
                .param("chainNet", "19.800")
                .param("chainLabour", "120.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(mockProductService).addProduct(productCaptor.capture());

        Product p = productCaptor.getValue();
        assertEquals("CH-777", p.getDesignNo());
        assertEquals(0, new BigDecimal("20.350").compareTo(p.getGross()));
        assertEquals(0, new BigDecimal("19.800").compareTo(p.getNet()));
        assertEquals(0, new BigDecimal("120.00").compareTo(p.getLabour()));
    }

    @Test
    public void testAddProductDiamondEarrings() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("imageUrl", "test.jpg", "image/jpeg", "image-bytes".getBytes());

        mockMvc.perform(multipart("/add")
                .file(mockFile)
                .param("productName", "Earring")
                .param("stockQuantity", "20")
                .param("categoryId", "1")
                .param("subCategoryId", "19")
                .param("karatId", "18")
                .param("orderId", "ORD-ER1")
                .param("designNoEarring", "ER-666")
                .param("earringGross", "8.450")
                .param("earringNet", "7.150")
                .param("earringPcs", "2")
                .param("diamondWeightEarring", "1.65")
                .param("diamondsWtRate", "43000.00")
                .param("earSt", "0.85")
                .param("earStRate", "10000.00")
                .param("diamondLabour", "300.00")
                .param("drLabourP", "12.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(mockProductService).addProduct(productCaptor.capture());

        Product p = productCaptor.getValue();
        assertEquals("ER-666", p.getDesignNo());
        assertEquals(0, new BigDecimal("8.450").compareTo(p.getGross()));
        assertEquals(0, new BigDecimal("7.150").compareTo(p.getNet()));
        assertEquals(2, p.getPcs());
        assertEquals(0, new BigDecimal("1.65").compareTo(p.getDiamondsCt()));
        assertEquals(0, new BigDecimal("43000.00").compareTo(p.getDiaRt()));
        assertEquals(0, new BigDecimal("0.85").compareTo(p.getOtherStonesCt()));
        assertEquals(0, new BigDecimal("10000.00").compareTo(p.getOtherStonesRt()));
        assertEquals(0, new BigDecimal("300.00").compareTo(p.getLabour()));
        assertEquals(0, new BigDecimal("12.00").compareTo(p.getLabourP()));
    }

    @Test
    public void testAddProductVilandi() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("imageUrl", "test.jpg", "image/jpeg", "image-bytes".getBytes());

        mockMvc.perform(multipart("/add")
                .file(mockFile)
                .param("productName", "Vilandi Neck")
                .param("stockQuantity", "2")
                .param("categoryId", "4")
                .param("subCategoryId", "9")
                .param("karatId", "22")
                .param("orderId", "ORD-VI1")
                .param("designNoVilandi", "VI-555")
                .param("vilandiGross", "30.120")
                .param("net", "25.800")
                .param("vilandi", "5.20")
                .param("vilandiRate", "52000.00")
                .param("stones", "2.10")
                .param("vilandiStoneRate", "4500.00")
                .param("beadsVilandi", "3.60")
                .param("vilandiBeadsRate", "1600.00")
                .param("pearlsVilandi", "6.420")
                .param("vilandiPearlRate", "2200.00")
                .param("ssPearlCt", "1.80")
                .param("vilandiSSPearlRate", "900.00")
                .param("realStone", "2.40")
                .param("vilandiFitting", "1500.00")
                .param("vilandiLabour", "450.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(mockProductService).addProduct(productCaptor.capture());

        Product p = productCaptor.getValue();
        assertEquals("VI-555", p.getDesignNo());
        assertEquals(0, new BigDecimal("30.120").compareTo(p.getGross()));
        assertEquals(0, new BigDecimal("25.800").compareTo(p.getNet()));
        assertEquals(0, new BigDecimal("5.20").compareTo(p.getVilandiCt()));
        assertEquals(0, new BigDecimal("52000.00").compareTo(p.getvRate()));
        assertEquals(0, new BigDecimal("2.10").compareTo(p.getStones()));
        assertEquals(0, new BigDecimal("4500.00").compareTo(p.getStRate()));
        assertEquals(0, new BigDecimal("3.60").compareTo(p.getBeadsCt()));
        assertEquals(0, new BigDecimal("1600.00").compareTo(p.getBdRate()));
        assertEquals(0, new BigDecimal("6.420").compareTo(p.getPearlsGm()));
        assertEquals(0, new BigDecimal("2200.00").compareTo(p.getPrlRate()));
        assertEquals(0, new BigDecimal("1.80").compareTo(p.getSsPearlCt()));
        assertEquals(0, new BigDecimal("900.00").compareTo(p.getSsRate()));
        assertEquals(0, new BigDecimal("2.40").compareTo(p.getRealStone()));
        assertEquals(0, new BigDecimal("1500.00").compareTo(p.getFitting()));
        assertEquals(0, new BigDecimal("450.00").compareTo(p.getLabour()));
    }

    @Test
    public void testAddProductJadtar() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("imageUrl", "test.jpg", "image/jpeg", "image-bytes".getBytes());

        mockMvc.perform(multipart("/add")
                .file(mockFile)
                .param("productName", "Jadtar Set")
                .param("stockQuantity", "3")
                .param("categoryId", "5")
                .param("subCategoryId", "6")
                .param("karatId", "22")
                .param("orderId", "ORD-JA1")
                .param("designNoJadtar", "JA-444")
                .param("jadtarGross", "45.650")
                .param("jadtarNet", "38.210")
                .param("jadtarStones", "4.50")
                .param("jadtarStoneRate", "4000.00")
                .param("jadtarBeads", "5.10")
                .param("jadtarBeadsRate", "1800.00")
                .param("jadtarPearls", "7.820")
                .param("jadtarPearlRate", "2500.00")
                .param("jadtarSSPearl", "3.20")
                .param("jadtarSSPearlRate", "1000.00")
                .param("jadtarRealStone", "3.80")
                .param("jadtarFitting", "2000.00")
                .param("jadvilandi", "6.50")
                .param("jadvilandiRate", "55000.00")
                .param("mozStone", "4.20")
                .param("mozStoneRate", "3500.00")
                .param("jadtarLabour", "500.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(mockProductService).addProduct(productCaptor.capture());

        Product p = productCaptor.getValue();
        assertEquals("JA-444", p.getDesignNo());
        assertEquals(0, new BigDecimal("45.650").compareTo(p.getGross()));
        assertEquals(0, new BigDecimal("38.210").compareTo(p.getNet()));
        assertEquals(0, new BigDecimal("4.50").compareTo(p.getStones()));
        assertEquals(0, new BigDecimal("4000.00").compareTo(p.getStRate()));
        assertEquals(0, new BigDecimal("5.10").compareTo(p.getBeadsCt()));
        assertEquals(0, new BigDecimal("1800.00").compareTo(p.getBdRate()));
        assertEquals(0, new BigDecimal("7.820").compareTo(p.getPearlsGm()));
        assertEquals(0, new BigDecimal("2500.00").compareTo(p.getPrlRate()));
        assertEquals(0, new BigDecimal("3.20").compareTo(p.getSsPearlCt()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(p.getSsRate()));
        assertEquals(0, new BigDecimal("3.80").compareTo(p.getRealStone()));
        assertEquals(0, new BigDecimal("2000.00").compareTo(p.getFitting()));
        assertEquals(0, new BigDecimal("6.50").compareTo(p.getVilandiCt()));
        assertEquals(0, new BigDecimal("55000.00").compareTo(p.getvRate()));
        assertEquals(0, new BigDecimal("4.20").compareTo(p.getMozonite()));
        assertEquals(0, new BigDecimal("3500.00").compareTo(p.getmRate()));
        assertEquals(0, new BigDecimal("500.00").compareTo(p.getLabour()));
    }
}
