/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

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

  public static byte[] encryptByXOR(byte[] key, String password) {
    byte[] pwds = messageDigestSHA256_toBytes(password);
    byte[] result = new byte[key.length];
    for (int i = 0; i < key.length; i++) {
      int j = i % pwds.length;
      // result[i] = (byte)((key[i] ^ pwds[j]) & 0xFF);
      result[i] = (byte) (key[i] ^ pwds[j]);
    }
    return result;
  }

  public static byte[] decryptByXOR(byte[] key, String password) {
    return encryptByXOR(key, password);
  }

  public static byte[] encryptByAES(byte[] key, String password) {
    try {
      byte[] salt = new byte[8];
      SecureRandom rand = new SecureRandom();
      rand.nextBytes(salt);
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1024, 128);
      SecretKey tmp = factory.generateSecret(spec);
      SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      AlgorithmParameters parameters = cipher.getParameters();
      byte[] iv = parameters.getParameterSpec(IvParameterSpec.class).getIV();
      byte[] output = cipher.doFinal(key);
      byte[] outputSizeBytes = intToBytes(output.length);
      byte[] ivSizeBytes = intToBytes(iv.length);
      byte[] data = new byte[Integer.BYTES * 2 + salt.length + iv.length + output.length];

      // The order of the data is arranged as the following:
      // int_forOutputSize + int_forIVsize + 8_byte_salt + iv_bytes + output_bytes
      int z = 0;
      for (int i = 0; i < outputSizeBytes.length; i++, z++) {
        data[z] = outputSizeBytes[i];
      }
      for (int i = 0; i < ivSizeBytes.length; i++, z++) {
        data[z] = ivSizeBytes[i];
      }
      for (int i = 0; i < salt.length; i++, z++) {
        data[z] = salt[i];
      }
      for (int i = 0; i < iv.length; i++, z++) {
        data[z] = iv[i];
      }
      for (int i = 0; i < output.length; i++, z++) {
        data[z] = output[i];
      }
      return data;
    } catch (NoSuchAlgorithmException
        | InvalidKeySpecException
        | NoSuchPaddingException
        | InvalidKeyException
        | InvalidParameterSpecException
        | IllegalBlockSizeException
        | BadPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] decryptByAES(byte[] key, String password) {
    try {
      // Divide the input data key[] into proper values
      // The order of the data is int_forOutputSize + int_forIVsize + 8_byte_salt + iv_bytes +
      // output_bytes
      int z = 0;
      byte[] lengthByte = new byte[Integer.BYTES];
      for (int i = 0; i < lengthByte.length; i++, z++) {
        lengthByte[i] = key[z];
      }
      int dataSize = bytesToInt(lengthByte);
      for (int i = 0; i < lengthByte.length; i++, z++) {
        lengthByte[i] = key[z];
      }
      int ivSize = bytesToInt(lengthByte);
      byte[] salt = new byte[8];
      for (int i = 0; i < salt.length; i++, z++) {
        salt[i] = key[z];
      }
      // iv bytes
      byte[] ivBytes = new byte[ivSize];
      for (int i = 0; i < ivBytes.length; i++, z++) {
        ivBytes[i] = key[z];
      }
      // Real data bytes
      byte[] dataBytes = new byte[dataSize];
      for (int i = 0; i < dataBytes.length; i++, z++) {
        dataBytes[i] = key[z];
      }
      // Once data are ready, reconstruct the key and cipher
      PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt, 1024, 128);
      SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      SecretKey tmp = secretKeyFactory.generateSecret(pbeKeySpec);
      SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      // Algorithm parameters (ivBytes) are necessary to initiate cipher
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));
      byte[] data = cipher.doFinal(dataBytes);
      return data;
    } catch (NoSuchAlgorithmException
        | InvalidKeySpecException
        | NoSuchPaddingException
        | InvalidKeyException
        | InvalidAlgorithmParameterException
        | IllegalBlockSizeException
        | BadPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] intToBytes(int v) {
    byte[] b = new byte[Integer.BYTES];
    for (int i = b.length - 1; i >= 0; i--) {
      b[i] = (byte) (v & 0xFF);
      v = v >> Byte.SIZE;
    }
    return b;
  }

  public static int bytesToInt(byte[] b) {
    int v = 0;
    for (int i = 0; i < b.length; i++) {
      v = v << Byte.SIZE;
      v = v | (b[i] & 0xFF);
    }
    return v;
  }
}
