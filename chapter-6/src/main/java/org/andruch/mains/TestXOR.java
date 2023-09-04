/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch.mains;

import org.andruch.UtilityMethods;

public class TestXOR {
  public static void main(String[] args) {
    String message = "At the most beautiful place, remember the most beautiful you.";
    String password = "blockchains";
    byte[] encrypted = UtilityMethods.encryptByXOR(message.getBytes(), password);

    // Take a peek at the encrypted data
    System.out.println(new String(encrypted));
    byte[] decrypted = UtilityMethods.decryptByXOR(encrypted, password);
    System.out.println("after proper decryption, the message is:\n");
    System.out.println(new String(decrypted));
    for (byte b : decrypted) {
      System.out.println(b);
    }
    System.out.println("\nwith an incorrect password, the decrypted message looks like: ");
    decrypted = UtilityMethods.decryptByXOR(encrypted, "Block Chain");
    System.out.println(new String(decrypted));
    for (byte b : decrypted) {
      System.out.println(b);
    }

    int i = 200;
    byte b = (byte) 200;
    System.out.println(b);
    System.out.println(b & 0xFF);
  }
}
