/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch.mains;

import java.security.*;

public class TestSignature {
  public static void main(String[] args)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    String msg = "If you never come, how do I age alone?";
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair keyPair = kpg.generateKeyPair();
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initSign(keyPair.getPrivate());
    signature.update(msg.getBytes());
    byte[] digitalSignature = signature.sign();
    System.out.println(new String(digitalSignature));
    Signature signature2 = Signature.getInstance("SHA256withRSA");
    signature2.initVerify(keyPair.getPublic());
    signature2.update(msg.getBytes());
    boolean verified = signature2.verify(digitalSignature);
    System.out.println("verified=" + verified);
  }
}
