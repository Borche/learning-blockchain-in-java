/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch;

import java.io.PrintStream;

public class PrintUtils {
  public static void printTransaction(Transaction t, PrintStream out, int level) {
    printWithTab(out, level, "Transaction {");
    printWithTab(out, level + 1, "ID: " + t.getHashID());
    printWithTab(out, level + 1, "sender: " + UtilityMethods.getKeyString(t.getSender()));
    printWithTab(out, level + 1, "fundsToBeTransferred total: " + t.getTotalFundsToTransfer());
    printWithTab(out, level + 1, "Inputs: ");
    for (int i = 0; i < t.getNumberOfInputsUTXOs(); i++) {
      UTXO in = t.getInputUTXO(i);
      printUTXO(in, out, level + 2);
    }
    printWithTab(out, level + 1, "Outputs: ");
    for (int i = 0; i < t.getNumberOfOutputUTXOs() - 1; i++) {
      UTXO ut = t.getOutputUTXO(i);
      printUTXO(ut, out, level + 2);
    }
    UTXO change = t.getOutputUTXO(t.getNumberOfOutputUTXOs() - 1);
    printWithTab(out, level + 2, "change: " + change.getFundsTransferred());
    printWithTab(out, level + 1, "transaction fee: " + Transaction.TRANSACTION_FEE);
    boolean b = t.verifySignature();
    printWithTab(out, level + 1, "signature verification: " + b);
    printWithTab(out, level, "}");
  }

  private static void printUTXO(UTXO utxo, PrintStream out, int level) {
    printWithTab(
        out,
        level,
        "fund: "
            + utxo.getFundsTransferred()
            + ", receiver: "
            + UtilityMethods.getKeyString(utxo.getReceiver()));
  }

  private static void printWithTab(PrintStream out, int level, String s) {
    for (int i = 0; i < level; i++) {
      out.print("\t");
    }
    out.println(s);
  }
}
