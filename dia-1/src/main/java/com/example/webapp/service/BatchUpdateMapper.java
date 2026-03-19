package com.example.webapp.service;

import java.math.BigDecimal;
import java.util.Map;

import com.example.webapp.models.Product;

public final class BatchUpdateMapper {
    private BatchUpdateMapper() {
    }

    public static int applyUpdates(Product product, Map<String, String> updates) {
        int applied = 0;

        String labourKey = resolveLabourKey(updates);
        if (labourKey != null) {
            applied += applyLabourUpdate(product, updates, labourKey);
        }

        for (Map.Entry<String, String> entry : updates.entrySet()) {
            String key = entry.getKey();
            String raw = entry.getValue();
            if (raw == null || raw.isBlank()) {
                continue;
            }
            if (isLabourKey(key)) {
                // Handled by applyLabourUpdate to keep fields exclusive.
                continue;
            }

            switch (key) {
                case "productName" -> { product.setItem(raw); applied++; }
                case "karat" -> { product.setKarat(toDecimal(raw, key)); applied++; }
                case "gross" -> { product.setGross(toDecimal(raw, key)); applied++; }
                case "productNet" -> { product.setNet(toDecimal(raw, key)); applied++; }
                case "labour" -> { product.setLabour(toDecimal(raw, key)); applied++; }
                case "labourAll" -> { product.setLabourAll(toDecimal(raw, key)); applied++; }
                case "labourPer" -> { product.setLabourP(toDecimal(raw, key)); applied++; }
                case "productRemarks" -> { product.setRemarks(raw); applied++; }
                case "pcs" -> { product.setPcs(toInteger(raw, key)); applied++; }
                case "diaWeight" -> { product.setDiaWeight(toDecimal(raw, key)); applied++; }
                case "diaRate" -> { product.setDiaRt(toDecimal(raw, key)); applied++; }
                case "diaOs" -> { product.setOtherStonesCt(toDecimal(raw, key)); applied++; }
                case "diaOsRate" -> { product.setOtherStonesRt(toDecimal(raw, key)); applied++; }
                case "vilandiCt" -> { product.setVilandiCt(toDecimal(raw, key)); applied++; }
                case "vilandiRate" -> { product.setvRate(toDecimal(raw, key)); applied++; }
                case "diamondsCt" -> { product.setDiamondsCt(toDecimal(raw, key)); applied++; }
                case "diamondsCtRate" -> { product.setDiaRt(toDecimal(raw, key)); applied++; }
                case "beadsCt" -> { product.setBeadsCt(toDecimal(raw, key)); applied++; }
                case "beadsRate" -> { product.setBdRate(toDecimal(raw, key)); applied++; }
                case "pearlsGm" -> { product.setPearlsGm(toDecimal(raw, key)); applied++; }
                case "openPearlsRate" -> { product.setPrlRate(toDecimal(raw, key)); applied++; }
                case "ssosPearllbl" -> { product.setSsPearlCt(toDecimal(raw, key)); applied++; }
                case "ssosPearlCt" -> { product.setSsRate(toDecimal(raw, key)); applied++; }
                case "otherStonesCt" -> { product.setOtherStonesCt(toDecimal(raw, key)); applied++; }
                case "otherOsRate" -> { product.setOtherStonesRt(toDecimal(raw, key)); applied++; }
                case "vilandi" -> { product.setVilandiCt(toDecimal(raw, key)); applied++; }
                case "vRate" -> { product.setvRate(toDecimal(raw, key)); applied++; }
                case "stones" -> { product.setStones(toDecimal(raw, key)); applied++; }
                case "vsRate" -> { product.setStRate(toDecimal(raw, key)); applied++; }
                case "beadsCtVilandi" -> { product.setBeadsCt(toDecimal(raw, key)); applied++; }
                case "vbRate" -> { product.setBdRate(toDecimal(raw, key)); applied++; }
                case "pearlsGmVilandi" -> { product.setPearlsGm(toDecimal(raw, key)); applied++; }
                case "vpRate" -> { product.setPrlRate(toDecimal(raw, key)); applied++; }
                case "ssPearlCt" -> { product.setSsPearlCt(toDecimal(raw, key)); applied++; }
                case "vssRate" -> { product.setSsRate(toDecimal(raw, key)); applied++; }
                case "vrealStone" -> { product.setRealStone(toDecimal(raw, key)); applied++; }
                case "vfitting" -> { product.setFitting(toDecimal(raw, key)); applied++; }
                case "vmoz" -> { product.setMozonite(toDecimal(raw, key)); applied++; }
                case "vmRate" -> { product.setmRate(toDecimal(raw, key)); applied++; }
                case "stonesJadtar" -> { product.setStones(toDecimal(raw, key)); applied++; }
                case "jsRate" -> { product.setStRate(toDecimal(raw, key)); applied++; }
                case "beadsCtJadtar" -> { product.setBeadsCt(toDecimal(raw, key)); applied++; }
                case "jbRate" -> { product.setBdRate(toDecimal(raw, key)); applied++; }
                case "pearlsGmJadtar" -> { product.setPearlsGm(toDecimal(raw, key)); applied++; }
                case "jpRate" -> { product.setPrlRate(toDecimal(raw, key)); applied++; }
                case "ssPearlCtJadtar" -> { product.setSsPearlCt(toDecimal(raw, key)); applied++; }
                case "jssRate" -> { product.setSsRate(toDecimal(raw, key)); applied++; }
                case "realStoneJadtar" -> { product.setRealStone(toDecimal(raw, key)); applied++; }
                case "jfitting" -> { product.setFitting(toDecimal(raw, key)); applied++; }
                case "jmoz" -> { product.setMozonite(toDecimal(raw, key)); applied++; }
                case "jmRate" -> { product.setmRate(toDecimal(raw, key)); applied++; }
                default -> throw new IllegalArgumentException("Unsupported update field: " + key);
            }
        }
        return applied;
    }

    private static String resolveLabourKey(Map<String, String> updates) {
        if (hasNonBlank(updates, "labour")) {
            return "labour";
        }
        if (hasNonBlank(updates, "labourAll")) {
            return "labourAll";
        }
        if (hasNonBlank(updates, "labourPer")) {
            return "labourPer";
        }
        return null;
    }

    private static int applyLabourUpdate(Product product, Map<String, String> updates, String key) {
        String raw = updates.get(key);
        if (raw == null || raw.isBlank()) {
            return 0;
        }

        if ("labour".equals(key)) {
            product.setLabour(toDecimal(raw, key));
            product.setLabourAll(null);
            product.setLabourP(null);
            return 1;
        }
        if ("labourAll".equals(key)) {
            product.setLabour(null);
            product.setLabourAll(toDecimal(raw, key));
            product.setLabourP(null);
            return 1;
        }
        if ("labourPer".equals(key)) {
            product.setLabour(null);
            product.setLabourAll(null);
            product.setLabourP(toDecimal(raw, key));
            return 1;
        }
        return 0;
    }

    private static boolean isLabourKey(String key) {
        return "labour".equals(key) || "labourAll".equals(key) || "labourPer".equals(key);
    }

    private static boolean hasNonBlank(Map<String, String> updates, String key) {
        String raw = updates.get(key);
        return raw != null && !raw.isBlank();
    }

    private static BigDecimal toDecimal(String raw, String key) {
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid decimal for " + key + ": " + raw);
        }
    }

    private static Integer toInteger(String raw, String key) {
        try {
            return Integer.valueOf(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid integer for " + key + ": " + raw);
        }
    }
}