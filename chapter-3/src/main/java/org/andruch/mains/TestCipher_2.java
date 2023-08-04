/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch.mains;

import java.security.*;
import javax.crypto.*;

public class TestCipher_2 {
  public static void main(String[] args)
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          IllegalBlockSizeException,
          BadPaddingException {
    String message = "If you were a drop of tear in my eyes, I will never cry";
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(4096);
    KeyPair pair = kpg.generateKeyPair();
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.ENCRYPT_MODE, pair.getPublic());
    byte[] b1 = cipher.doFinal(message.getBytes());
    cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
    byte[] b2 = cipher.doFinal(b1);
    System.out.println(b2);
    System.out.println(new String(b2));
  }
}
