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
  @Getter private String name;

  public Wallet(String walletName, String password) {
    this.name = walletName;
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

  // When setting local blockchain (ledger), if the wallet does not have a local ledger, the wallet
  // simply accepts the incoming ledger.
  // If the wallet already has a local ledger, then it ie necessary to compare the existing ledger
  // to the incoming one. The wallet only accepts the incoming ledger if it
  // 1) is validated, 2) is longer than the existing one and 3) both the incoming one and local one
  // have the same genesis block.
  public synchronized boolean setLocalLedger(Blockchain ledger) {
    boolean isValid = Blockchain.validateBlockchain(ledger);
    if (!isValid) {
      System.out.println();
      System.out.println(this.getName() + "] Warning: the incoming blockchain failed validation");
      System.out.println();
      return false;
    }
    if (this.localLedger == null) {
      this.localLedger = ledger;
      return true;
    } else {
      if (ledger.size() > this.localLedger.size()
          && ledger.getGenesisMiner().equals(this.localLedger.getGenesisMiner())) {
        this.localLedger = ledger;
        return true;
      } else if (ledger.size() <= this.localLedger.size()) {
        System.out.println(
            this.getName()
                + "] Warning: the incoming blockchain is no longer than current local one"
                + ", local size="
                + this.localLedger.size()
                + ", incoming size="
                + ledger.size());
        return false;
      } else {
        System.out.println(
            this.getName()
                + "] Warning: the incoming blockchain has a different genesis miner than current"
                + " local one");
        return false;
      }
    }
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
        new FileOutputStream(KEY_LOCATION + "/" + getName().replaceAll(" ", "_") + "_keys");
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
          this.name
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

  public synchronized boolean updateLocalLedger(ArrayList<Blockchain> chains) {
    if (chains.size() == 0) {
      return false;
    }
    if (this.localLedger != null) {
      Blockchain max = this.localLedger;
      for (int i = 0; i < chains.size(); i++) {
        Blockchain bc = chains.get(i);
        if (bc.getGenesisMiner().equals(this.localLedger.getGenesisMiner())
            && bc.size() > max.size()
            && Blockchain.validateBlockchain(bc)) {
          max = bc;
        }
      }
      this.localLedger = max; // it is possible that nothing changed
      return true;
    } else {
      Blockchain max = null;
      int currentLength = 0;
      for (int i = 0; i < chains.size(); i++) {
        Blockchain bc = chains.get(i);
        boolean b = Blockchain.validateBlockchain(bc);
        if (b && bc.size() > currentLength) {
          max = bc;
          currentLength = max.size();
        }
      }
      if (max != null) {
        this.localLedger = max;
        return true;
      } else {
        return false;
      }
    }
  }

  public synchronized boolean updateLocalLedger(Block block) {
    if (verifyGuestBlock(block)) {
      return this.localLedger.addBlock(block);
    }
    return false;
  }

  public boolean verifyGuestBlock(Block block) {
    return this.verifyGuestBlock(block, this.getLocalLedger());
  }

  // Be aware of the difference between verifying a block and validating a blockchain. Validating a
  // blockchain in this implementation
  // does not validate each transaction. However, verifying a block must make sure that each
  // transaction in the block is validating against the local blockchain.
  public boolean verifyGuestBlock(Block block, Blockchain ledger) {
    // checking the signature
    if (!block.verifySignature(block.getCreator())) {
      System.out.println("\tWarning: block(" + block.getHashID() + ") signature tampered");
      return false;
    }
    // got to verify the proof of work, too
    if (!UtilityMethods.hashMeetsDifficultyLevel(block.getHashID(), block.getDifficultyLevel())
        || !block.computeHashID().equals(block.getHashID())) {
      System.out.println("\tWarning: block(" + block.getHashID() + ") mining is not successful!");
      return false;
    }

    // making sure that this block is build upon last block, i.e, verify its hashID
    if (!ledger.getLastBlock().getHashID().equals(block.getPreviousBlockHashID())) {
      System.out.println("\tWarning: block(" + block.getHashID() + ") is not linked to last block");
      return false;
    }

    // checking all the transactions are valid
    int size = block.getNumberOfTransactions();
    for (int i = 0; i < size; i++) {
      Transaction T = block.getTransaction(i);
      if (!validateTransaction(T)) {
        System.out.println(
            "\tWarning: block("
                + block.getHashID()
                + ") transaction "
                + i
                + " is invalid either "
                + "because of signature being tampered or already existing in the blockchain.");
        return false;
      }
    }
    // here, we do not examine if the transaction balance is good, however
    // check the rewarding transaction
    Transaction tr = block.getRewardTransaction();
    // if(tr.getTotalFundToTransfer() > BlockChain.MINING_REWARD){ --> resulted rejection, tested
    if (tr.getTotalFundsToTransfer() > Blockchain.MINING_REWARD + block.getTransactionFeeAmount()) {
      System.out.println("\tWarning: block(" + block.getHashID() + ") over rewarded");
      return false;
    }
    return true;
  }

  public boolean validateTransaction(Transaction ts) {
    if (ts == null) {
      return false;
    }
    if (!ts.verifySignature()) {
      System.out.println(
          "WARNING: transaction ID="
              + ts.getHashID()
              + " from "
              + UtilityMethods.getKeyString(ts.getSender())
              + " is invalid. It has been tampered.");
      return false;
    }
    // make sure that this transaction does not exist in the existing ledger
    // this is a time consuming process.
    boolean exists;
    if (this.getLocalLedger() == null) {
      exists = false;
    } else {
      exists = this.getLocalLedger().transactionExists(ts);
    }
    return !exists;
  }

  public PublicKey getPublicKey() {
    return keyPair.getPublic();
  }

  public PrivateKey getPrivateKey() {
    return keyPair.getPrivate();
  }
}
