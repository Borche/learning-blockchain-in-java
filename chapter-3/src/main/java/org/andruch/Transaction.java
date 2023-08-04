/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import lombok.Getter;

public class Transaction implements Serializable {
  private static final long serialVersionUID = 1L;
  public static final double TRANSACTION_FEE = 1.0;
  @Getter private String hashID;
  @Getter private PublicKey sender;
  private PublicKey[] receivers;
  private double[] fundsToTransfer;
  @Getter private long timestamp;
  private ArrayList<UTXO> inputs = null;
  private ArrayList<UTXO> outputs = new ArrayList<>(4);
  private byte[] signature = null;
  private boolean signed = false;
  @Getter private long mySeqNumber;

  public Transaction(
      PublicKey sender, PublicKey receiver, double fundsToTransfer, ArrayList<UTXO> inputs) {
    PublicKey[] pks = new PublicKey[1];
    pks[0] = receiver;
    double[] funds = new double[1];
    funds[0] = fundsToTransfer;
    this.setUp(sender, pks, funds, inputs);
  }

  public Transaction(
      PublicKey sender, PublicKey[] receivers, double[] fundsToTransfer, ArrayList<UTXO> inputs) {
    this.setUp(sender, receivers, fundsToTransfer, inputs);
  }

  private void setUp(
      PublicKey sender, PublicKey[] receivers, double[] fundsToTransfer, ArrayList<UTXO> inputs) {
    this.mySeqNumber = UtilityMethods.getUniqueNumber();
    this.sender = sender;
    this.receivers = new PublicKey[1];
    this.receivers = receivers;
    this.fundsToTransfer = fundsToTransfer;
    this.inputs = inputs;
    this.timestamp = UtilityMethods.getTimeStamp();
    computeHashID();
  }

  public void signTheTransaction(PrivateKey privateKey) {
    if (signature == null && !signed) {
      signature = UtilityMethods.generateSignature(privateKey, getMessageData());
      signed = true;
    }
  }

  public boolean verifySignature() {
    String message = getMessageData();
    return UtilityMethods.verifySignature(this.sender, this.signature, message);
  }

  private void computeHashID() {
    String message = getMessageData();
    this.hashID = UtilityMethods.messageDigestSHA256_toString(message);
  }

  private String getMessageData() {
    StringBuilder sb = new StringBuilder();
    sb.append(
        UtilityMethods.getKeyString(sender)
            + Long.toHexString(timestamp)
            + Long.toString(mySeqNumber));
    for (int i = 0; i < receivers.length; i++) {
      sb.append(UtilityMethods.getKeyString(receivers[i]) + Double.toHexString(fundsToTransfer[i]));
    }
    for (int i = 0; i < getNumberOfInputUTXOs(); i++) {
      UTXO in = inputs.get(i);
      sb.append(in.getHashID());
    }
    return sb.toString();
  }

  public double getTotalFundsToTransfer() {
    double f = 0;
    for (int i = 0; i < fundsToTransfer.length; i++) {
      f += fundsToTransfer[i];
    }
    return f;
  }

  private int getNumberOfInputUTXOs() {
    if (inputs == null) return 0;
    return inputs.size();
  }

  public void addOutputUTXO(UTXO out) {
    if (!signed) {
      outputs.add(out);
    }
  }

  public boolean equals(Transaction other) {
    return this.getHashID().equals(other.getHashID());
  }

  public int getNumberOfOutputUTXOs() {
    return outputs.size();
  }

  public UTXO getOutputUTXO(int i) {
    return outputs.get(i);
  }
}
