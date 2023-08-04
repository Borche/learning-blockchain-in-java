/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch.mains;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import org.andruch.Transaction;
import org.andruch.UTXO;
import org.andruch.UtilityMethods;

public class TestTransaction {
  public static void main(String[] args) {
    // Generate the sender
    KeyPair sender = UtilityMethods.generateKeyPair();
    // Let us have two recipients
    PublicKey[] receivers = new PublicKey[2];
    double[] fundsToTransfer = new double[receivers.length];
    for (int i = 0; i < receivers.length; i++) {
      receivers[i] = UtilityMethods.generateKeyPair().getPublic();
      fundsToTransfer[i] = (i + 1) * 100;
    }
    // Since we don't have a wallet class to make the transaction,
    // we need to manually create the input UTXOs and output UTXO.
    UTXO in = new UTXO("0", sender.getPublic(), sender.getPublic(), 1000);
    ArrayList<UTXO> ins = new ArrayList<>();
    ins.add(in);

    Transaction t = new Transaction(sender.getPublic(), receivers, fundsToTransfer, ins);

    // Make sure that the sender has enough funds
    double available = 0.0;
    for (int i = 0; i < ins.size(); i++) {
      available += ins.get(i).getFundsTransferred();
    }
    // Compute the total cost and add the transaction fee
    double totalCost = t.getTotalFundsToTransfer() + Transaction.TRANSACTION_FEE;
    // If funds are not enough, abort
    if (available < totalCost) {
      System.out.println(
          "funds available=" + available + ", not enough for total cost of " + totalCost);
      return;
    }

    // Generate the output UTXOs
    for (int i = 0; i < receivers.length; i++) {
      UTXO ut = new UTXO(t.getHashID(), sender.getPublic(), receivers[i], fundsToTransfer[i]);
      t.addOutputUTXO(ut);
    }
    // Generate the change as an UTXO to the sender
    UTXO change =
        new UTXO(t.getHashID(), sender.getPublic(), sender.getPublic(), available - totalCost);
    t.addOutputUTXO(change);
    // Sign the transaction
    t.signTheTransaction(sender.getPrivate());
    // Display the transaction to take a look
    displayTransaction(t);
  }

  private static void displayTransaction(Transaction t) {
    System.out.println("Transaction{");
    System.out.println("\tID: " + t.getHashID());
    System.out.println("\tsender: " + UtilityMethods.getKeyString(t.getSender()));
    System.out.println("\tfundsToBeTransferred total: " + t.getTotalFundsToTransfer());
    System.out.println("\tReceivers: ");
    for (int i = 0; i < t.getNumberOfOutputUTXOs() - 1; i++) {
      UTXO ut = t.getOutputUTXO(i);
      System.out.println(
          "\t\tfund="
              + ut.getFundsTransferred()
              + ",receiver="
              + UtilityMethods.getKeyString(ut.getReceiver()));
    }
    UTXO change = t.getOutputUTXO(t.getNumberOfOutputUTXOs() - 1);
    System.out.println("\ttransaction fee: " + Transaction.TRANSACTION_FEE);
    System.out.println("\tchange: " + change.getFundsTransferred());
    boolean b = t.verifySignature();
    System.out.println("\tsignature verification: " + b);
    System.out.println("}");
  }
}
