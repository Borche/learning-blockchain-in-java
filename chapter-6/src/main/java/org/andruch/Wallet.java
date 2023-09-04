/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch;

import java.io.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import lombok.Getter;

public class Wallet {
  private static final String KEY_LOCATION = "keys";

  private Blockchain localLedger = null;

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

  public synchronized Blockchain getLocalLedger() {
    return localLedger;
  }

  public synchronized boolean setLocalLedger(Blockchain ledger) {
    this.localLedger = ledger;
    return true;
  }

  public double getCurrentBalance(Blockchain ledger) {
    return ledger.checkBalance(getPublicKey());
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

  public Transaction transferFund(PublicKey receiver, double fundToTransfer) {
    PublicKey[] receivers = new PublicKey[1];
    double[] funds = new double[1];
    receivers[0] = receiver;
    funds[0] = fundToTransfer;
    return transferFund(receivers, funds);
  }

  public Transaction transferFund(PublicKey[] receivers, double[] fundToTransfer) {
    ArrayList<UTXO> unspent = new ArrayList<>();
    double available = this.getLocalLedger().findUnspentUTXOs(this.getPublicKey(), unspent);
    double totalNeeded = Transaction.TRANSACTION_FEE;
    for (int i = 0; i < fundToTransfer.length; i++) {
      totalNeeded += fundToTransfer[i];
    }
    if (available < totalNeeded) {
      System.out.println(
          this.walletName
              + " balance="
              + available
              + ", not enough to make the transfer of "
              + totalNeeded);
      return null;
    }
    // create input for the transaction
    ArrayList<UTXO> inputs = new ArrayList<>();
    available = 0;
    for (int i = 0; i < unspent.size() && available < totalNeeded; i++) {
      UTXO uxo = unspent.get(i);
      available += uxo.getFundsTransferred();
      inputs.add(uxo);
    }

    // create the Transaction
    Transaction T = new Transaction(this.getPublicKey(), receivers, fundToTransfer, inputs);

    // sign the transaction
    boolean b = T.prepareOutputUTXOs();
    if (b) {
      T.signTheTransaction(this.getPrivateKey());
      return T;
    } else {
      return null;
    }
  }

  public PublicKey getPublicKey() {
    return keyPair.getPublic();
  }

  public PrivateKey getPrivateKey() {
    return keyPair.getPrivate();
  }
}
