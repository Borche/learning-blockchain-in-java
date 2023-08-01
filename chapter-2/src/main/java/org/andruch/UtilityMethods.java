package org.andruch;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;

public class UtilityMethods {

    public static boolean hashMeetsDifficultyLevel(String hash, int difficultyLevel) {
        char[] c = hash.toCharArray();
        for (int i = 0; i < difficultyLevel; i++) {
            if (c[i] != '0') {
                return false;
            }
        }
        return true;
    }

    public static String toBinaryString(byte[] hash) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            // Transform a byte into an unsigned integer
            int x = ((int) hash[i]) + 128;
            StringBuilder s = new StringBuilder(Integer.toBinaryString(x));
            while (s.length() < 8) {
                s.insert(0, "0");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public static byte[] messageDigestSHA256_toBytes(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(message.getBytes());
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String messageDigestSHA256_toString(String message) {
        return Base64.getEncoder().encodeToString(messageDigestSHA256_toBytes(message));
    }

    public static long getTimeStamp() {
        return LocalDateTime.now(ZoneId.of("Europe/Stockholm")).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
