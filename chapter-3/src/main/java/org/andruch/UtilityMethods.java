/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch;

import java.security.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;

public class UtilityMethods {

  private static long uniqueNumber = 0;

  public static long getUniqueNumber() {
    return uniqueNumber++;
  }

  public static KeyPair generateKeyPair() {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
      kpg.initialize(2048);
      KeyPair keyPair = kpg.generateKeyPair();
      return keyPair;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] generateSignature(PrivateKey privateKey, String message) {
    try {
      Signature sig = Signature.getInstance("SHA256withRSA");
      sig.initSign(privateKey);
      sig.update(message.getBytes());
      return sig.sign();
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean verifySignature(PublicKey publicKey, byte[] signature, String message) {
    try {
      Signature sig = Signature.getInstance("SHA256withRSA");
      sig.initVerify(publicKey);
      sig.update(message.getBytes());
      return sig.verify(signature);
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getKeyString(Key key) {
    // key.getEncoded() contains the actual key
    return Base64.getEncoder().encodeToString(key.getEncoded());
  }

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
    return LocalDateTime.now(ZoneId.of("Europe/Stockholm"))
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli();
  }
}
