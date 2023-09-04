/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch.mains;

import java.util.ArrayList;
import org.andruch.*;

/** This class simulates the blockchain system */
public class BlockchainPlatform {
  // the blockchain
  private static Blockchain blockchain;
  // having this variable here to track how many transactions have spent
  // in total, though currently no transaction fee is collected
  // by the miner
  private static double transactionFee = 0.0;

  public static void main(String[] args) throws Exception {
    // set the mining difficult level. 25 is good for practice
    int difficultLevel = 25;
    System.out.println("Blockchain platform starts ...");
    System.out.println("creating genesis miner, " + "genesis transaction and genesis block");
    // create a genesis miner to start a blockchain
    Miner genesisMiner = new Miner("genesis", "genesis");
    // create genesis block
    Block genesisBlock = new Block("0", difficultLevel);
    // manually creates two UTXOs as input for the genesis transaction
    // Please notice that one input is 10001 instead of 10000. The extra 1 is for transaction fee.
    UTXO u1 = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10001.0);
    UTXO u2 = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000.0);
    // prepare the input
    ArrayList<UTXO> inputs = new ArrayList<UTXO>();
    inputs.add(u1);
    inputs.add(u2);
    // prepare the genesis transaction
    Transaction gt =
        new Transaction(genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000.0, inputs);
    // sign the transaction by the genesis miner
    boolean success = gt.prepareOutputUTXOs();
    // check if the signing is successful. If not, exit the system
    if (!success) {
      System.out.println("genesis transaction failed.");
      System.exit(1);
    }
    gt.signTheTransaction(genesisMiner.getPrivateKey());
    // add the genesis transaction into the genesis  block
    genesisBlock.addTransaction(gt);
    // the genesis miner mines the genesis block
    System.out.println("genesis miner is mining the genesis block");
    success = genesisMiner.mineBlock(genesisBlock);
    // check if mining is successful
    if (success) {
      System.out.println("genesis block is successfully mined. HashID:");
      System.out.println(genesisBlock.getHashID());
    } else {
      System.out.println("failed to mine genesis block. System exit");
      System.exit(1);
    }
    // construct the blockchain
    blockchain = new Blockchain(genesisBlock);
    System.out.println("blockchain genesis successful");
    // genesisMiner copies the blockchain to his local ledger
    genesisMiner.setLocalLedger(blockchain);
    // manually check the balance of the genesis miner. Please verify if
    // it is correct
    System.out.println(
        "genesis miner balance: " + genesisMiner.getCurrentBalance(genesisMiner.getLocalLedger()));
    // create other wallet and miners
    Miner A = new Miner("Miner A", "miner A");
    Wallet B = new Wallet("wallet B", "wallet B");
    Miner C = new Miner("Miner C", "miner c");
    // every wallet and miner stores a local ledger. Please be aware that they
    // are in fact sharing the same blockchain as it is not distributed
    A.setLocalLedger(blockchain);
    B.setLocalLedger(blockchain);
    C.setLocalLedger(blockchain);

    // create the second block
    Block b2 = new Block(blockchain.getLastBlock().getHashID(), difficultLevel);
    System.out.println("\n\nBlock b2 created");
    // let the genesis miner transfer 100 to A and 200 to B
    Transaction t1 = genesisMiner.transferFund(A.getPublicKey(), 100);
    // make sure that the transaction is not null. If null, it means that
    // the transaction construction is not successful
    if (t1 != null) {
      // assuming that someone is examining the transaction
      if (t1.verifySignature() && b2.addTransaction(t1)) {
        // display the balance to show that everything works. At this
        // moment, A, B, C, should have zero balance
        System.out.println(
            "t1 added to block b2. Before b2 is mined "
                + "and added to the chain, the balances are:");
        double total =
            genesisMiner.getCurrentBalance(blockchain)
                + A.getCurrentBalance(blockchain)
                + B.getCurrentBalance(blockchain)
                + C.getCurrentBalance(blockchain);

        System.out.println(
            "genesisMiner="
                + genesisMiner.getCurrentBalance(blockchain)
                + ", A="
                + A.getCurrentBalance(blockchain)
                + ", B="
                + B.getCurrentBalance(blockchain)
                + ", C="
                + C.getCurrentBalance(blockchain)
                + ", total="
                + total);
      } else {
        System.out.println("t1 failed to add to b2");
      }
    } else {
      System.out.println("t1 failed to create");
    }
    Transaction t2 = genesisMiner.transferFund(B.getPublicKey(), 200);
    if (t2 != null) {
      if (t2.verifySignature() && b2.addTransaction(t2)) {
        System.out.println(
            "t2 added to block b2. Before b2 is mined "
                + "and added to the chain, the balances are:");
        double total =
            genesisMiner.getCurrentBalance(blockchain)
                + A.getCurrentBalance(blockchain)
                + B.getCurrentBalance(blockchain)
                + C.getCurrentBalance(blockchain);

        System.out.println(
            "genesisMiner="
                + genesisMiner.getCurrentBalance(blockchain)
                + ", A="
                + A.getCurrentBalance(blockchain)
                + ", B="
                + B.getCurrentBalance(blockchain)
                + ", C="
                + C.getCurrentBalance(blockchain)
                + ", total="
                + total);
      } else {
        System.out.println("t2 failed to add to block b2");
      }
    } else {
      System.out.println("t2 failed to create");
    }
    // now mine the block 2
    System.out.println("Miner A is mining block 2 ...");
    if (A.mineBlock(b2)) {
      System.out.println("A mined b2, hashID is:");
      System.out.println(b2.getHashID());
      blockchain.addBlock(b2);
      System.out.println("After block b2 is added to the chain, " + "the balances are:");
      displayBalanceAfterBlock(b2, genesisMiner, A, B, C);
    }

    // another block
    Block b3 = new Block(blockchain.getLastBlock().getHashID(), difficultLevel);
    System.out.println("\n\nblock b3 created");
    Transaction t3 = A.transferFund(B.getPublicKey(), 200.0);
    if (t3 != null) {
      if (t3.verifySignature() && b3.addTransaction(t3)) {
        System.out.println("t3 added to block b3");
      } else {
        System.out.println("t3 failed to add to block b3");
      }
    } else {
      System.out.println("t3 failed to create");
    }
    Transaction t4 = A.transferFund(C.getPublicKey(), 300.0);
    if (t4 != null) {
      if (t4.verifySignature() && b3.addTransaction(t4)) {
        System.out.println("t4 added to block b3.");
      } else {
        System.out.println("t4 failed to add to block b3");
      }
    } else {
      System.out.println("t4 failed to create");
    }

    Transaction t5 = A.transferFund(C.getPublicKey(), 20.0);
    if (t5 != null) {
      if (t5.verifySignature() && b3.addTransaction(t5)) {
        System.out.println("t5 added to block b3.");
      } else {
        System.out.println("t5 failed to add to block b3");
      }
    } else {
      System.out.println("t5 failed to create");
    }

    Transaction t6 = B.transferFund(C.getPublicKey(), 80.0);
    if (t6 != null) {
      if (t6.verifySignature() && b3.addTransaction(t6)) {
        System.out.println("t6 added to block b3.");
      } else {
        System.out.println("t6 failed to add to block b3");
      }
    } else {
      System.out.println("t6 failed to create");
    }

    // now mine the block 3.
    System.out.println("Miner C is mining block 3 ...");
    if (C.mineBlock(b3)) {
      System.out.println("C mined b3, hashID is:");
      System.out.println(b3.getHashID());
      blockchain.addBlock(b3);
      System.out.println("After block b3 is added to the chain, " + "the balances are:");
      displayBalanceAfterBlock(b3, genesisMiner, A, B, C);
    }

    System.out.println("=========BlockChain platform shuts down=========");
  }

  // a method to display the balance of the wallets and miners
  private static void displayBalanceAfterBlock(
      Block b, Wallet genesisMiner, Wallet A, Wallet B, Wallet C) {
    double total =
        genesisMiner.getCurrentBalance(blockchain)
            + A.getCurrentBalance(blockchain)
            + B.getCurrentBalance(blockchain)
            + C.getCurrentBalance(blockchain);
    transactionFee += b.getNumberOfTransactions() * Transaction.TRANSACTION_FEE;
    System.out.println(
        "genesisMiner="
            + genesisMiner.getCurrentBalance(blockchain)
            + ", A="
            + A.getCurrentBalance(blockchain)
            + ", B="
            + B.getCurrentBalance(blockchain)
            + ", C="
            + C.getCurrentBalance(blockchain)
            + ", total cash ="
            + total
            + ", transaction fee="
            + transactionFee);
    System.out.println("====>the length of the blockchain=" + blockchain.size());
  }
}
