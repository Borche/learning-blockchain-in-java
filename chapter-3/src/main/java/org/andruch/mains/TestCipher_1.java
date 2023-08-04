/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch.mains;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.*;

public class TestCipher_1 {
  public static void main(String[] args)
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          IllegalBlockSizeException,
          BadPaddingException {
    Cipher cipher = Cipher.getInstance("AES"); // RSA, AES, DES, DESede
    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
    keyGenerator.init(sr);
    SecretKey secretKey = keyGenerator.generateKey();
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    String message = "If you were a drop of tear in my eyes";
    byte[] cipherText = cipher.doFinal(message.getBytes());
    Cipher cipher2 = Cipher.getInstance("AES");
    cipher2.init(Cipher.DECRYPT_MODE, secretKey);
    byte[] decoded = cipher2.doFinal(cipherText);
    String msg = new String(decoded);
    System.out.println(msg);
  }
}
