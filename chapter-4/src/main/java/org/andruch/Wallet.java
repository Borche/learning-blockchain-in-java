/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch;

import java.io.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import lombok.Getter;

public class Wallet {
  private static final String KEY_LOCATION = "keys";

  private KeyPair keyPair;
  @Getter private String walletName;

  public Wallet(String walletName, String password) {
    this.walletName = walletName;
    try {
      populateExistingWallet(walletName, password);
      System.out.println(
          "A wallet exists with the same name and password. Loaded the existing wallet.");
    } catch (Exception e) {
      try {
        prepareWallet(password);
        System.out.println("Created a new wallet.");
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  private void prepareWallet(String password) throws IOException {
    this.keyPair = UtilityMethods.generateKeyPair();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(this.keyPair);
    byte[] encrypted = UtilityMethods.encryptByXOR(baos.toByteArray(), password);
    File f = new File(KEY_LOCATION);
    if (!f.exists()) {
      f.mkdir();
    }
    FileOutputStream fos =
        new FileOutputStream(KEY_LOCATION + "/" + getWalletName().replaceAll(" ", "_") + "_keys");
    fos.write(encrypted);
    fos.close();
    baos.close();
  }

  private void populateExistingWallet(String walletName, String password)
      throws IOException, ClassNotFoundException {
    FileInputStream fis =
        new FileInputStream(Wallet.KEY_LOCATION + "/" + walletName.replaceAll(" ", "_") + "_keys");
    byte[] bb = new byte[4096];
    int size = fis.read(bb);
    fis.close();
    byte[] data = new byte[size];
    for (int i = 0; i < data.length; i++) {
      data[i] = bb[i];
    }
    byte[] keyBytes = UtilityMethods.decryptByXOR(data, password);
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(keyBytes));
    this.keyPair = (KeyPair) ois.readObject();
  }

  public PublicKey getPublicKey() {
    return keyPair.getPublic();
  }

  protected PrivateKey getPrivateKey() {
    return keyPair.getPrivate();
  }
}
