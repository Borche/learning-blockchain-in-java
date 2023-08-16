/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch.mains;

import org.andruch.UtilityMethods;

public class TestEncryptionDecryptionWithAES {
  public static void main(String[] args) {
    String message = "At the most beautiful place, remember you're the most beautiful.";
    String password = "blockchains";
    byte[] encrypted = UtilityMethods.encryptByAES(message.getBytes(), password);
    System.out.println(new String(encrypted));

    byte[] decrypted = UtilityMethods.decryptByAES(encrypted, password);
    System.out.println(new String(decrypted));

    System.out.println("With incorrect password:");
    decrypted = UtilityMethods.decryptByAES(encrypted, "Block Chain");
    System.out.println(new String(decrypted));
  }
}
