package com.kairos.utils;

import java.text.DecimalFormat;

public class NumberUtils {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    public static String formatDouble(double value) {
        return DECIMAL_FORMAT.format(value);
    }
    
    public static int parseIntOrDefault(String string, int fallback) {
        try { return Integer.parseInt(string); } 
        catch (NumberFormatException e) { return fallback; }
    }
}