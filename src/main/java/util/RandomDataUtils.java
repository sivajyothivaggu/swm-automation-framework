package com.swm.core.utils;

import java.util.Random;

public class RandomDataUtils {
    private static Random random = new Random();
    
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    public static int generateRandomNumber(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
    
    public static String generateRandomEmail() {
        return "test_" + generateRandomString(8) + "@test.com";
    }
}
