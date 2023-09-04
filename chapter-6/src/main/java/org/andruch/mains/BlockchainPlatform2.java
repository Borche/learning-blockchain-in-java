/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch.mains;

import java.util.ArrayList;
import org.andruch.*;

/**
 * this is the blockchain system. You will experience some errors inside because the current system
 * does not do a complete verification.
 */
public class BlockchainPlatform2 {
  private static Blockchain blockchain;
  private static double transactionFee = 0.0;

  public static void main(String[] args) throws Exception {
    int difficultLevel = 20;
    System.out.println("Blockchain platform starts ...");
    System.out.println("creating genesis miner, genesis transaction and genesis block");
    // create a genesis miner to start a blockchain
    Miner genesisMiner = new Miner("genesis", "genesis");
    // create genesis block
    Block genesisBlock = new Block("0", difficultLevel);
    // System.out.println("inside genesis(), before gt");
    UTXO u1 = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10001.0);
    UTXO u2 = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000.0);
    ArrayList<UTXO> inputs = new ArrayList<UTXO>();
    inputs.add(u1);
    inputs.add(u2);
    Transaction gt =
        new Transaction(genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000.0, inputs);
    boolean b = gt.prepareOutputUTXOs();
    if (!b) {
      System.out.println("genesis transaction failed.");
      System.exit(1);
    }
    gt.signTheTransaction(genesisMiner.getPrivateKey());
    genesisBlock.addTransaction(gt);
    // the genesis miner mines the genesis block
    System.out.println("genesis miner is mining the genesis block");
    b = genesisMiner.mineBlock(genesisBlock);
    if (b) {
      System.out.println("genesis block is successfully mined. HashID:");
      System.out.println(genesisBlock.getHashID());
    } else {
      System.out.println("failed to mine genesis block. System exit");
      System.exit(1);
    }
    blockchain = new Blockchain(genesisBlock);
    System.out.println("block chain genesis successful");
    // genesisMiner copies the blockchain to his local ledger
    genesisMiner.setLocalLedger(blockchain);
    System.out.println(
        "genesis miner balance: " + genesisMiner.getCurrentBalance(genesisMiner.getLocalLedger()));
    // create another miner and one wallet
    Miner A = new Miner("Miner A", "miner A");
    Wallet B = new Wallet("wallet B", "wallet B");
    Miner C = new Miner("Miner C", "miner c");
    A.setLocalLedger(blockchain);
    B.setLocalLedger(blockchain);
    C.setLocalLedger(blockchain);

    double transactionFee = 0.0;
    Block b2 = new Block(blockchain.getLastBlock().getHashID(), difficultLevel);
    System.out.println("\n\nBlock b2 created");
    // let the genesis miner transfer 100 to A and 200 to B
    Transaction t1 = genesisMiner.transferFund(A.getPublicKey(), 100);
    if (t1 != null) {
      if (t1.verifySignature() && b2.addTransaction(t1)) {
        System.out.println(
            "t1 added to block b2. Before b2 is mined and added to the chain, the balances are:");
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
            "t2 added to block b2. Before b2 is mined and added to the chain, the balances are:");
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
    if (A.mineBlock(b2)) {
      System.out.println("A mined b2,hashID is:");
      System.out.println(b2.getHashID());
      blockchain.addBlock(b2);
      System.out.println("After block b2 is added to the chain, the balances are:");
      displayBalanceAfterBlock(b2, genesisMiner, A, B, C);
    }

    // another block
    Block b3 = new Block(blockchain.getLastBlock().getHashID(), difficultLevel);
    System.out.println("\n\nblock b3 created");
    Transaction t3 = A.transferFund(B.getPublicKey(), 200.0);
    if (t3 != null) {
      if (t3.verifySignature() && b3.addTransaction(t3)) {
        System.out.println(
            "t3 added to block b3. Before b3 is mined and added to the chain, the balances are:");
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
        System.out.println("t3 failed to add to block b3");
      }

    } else {
      System.out.println("t3 failed to create");
    }

    Transaction t4 = A.transferFund(C.getPublicKey(), 200.0);
    if (t4 != null) {
      if (t4.verifySignature() && b3.addTransaction(t4)) {
        System.out.println(
            "t4 added to block b3. Before b3 is mined and added to the chain, the balances are:");
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
        System.out.println("t4 failed to add to block b3");
      }

    } else {
      System.out.println("t4 failed to create");
    }

    Transaction t5 = A.transferFund(C.getPublicKey(), 20.0);
    if (t5 != null) {
      if (t5.verifySignature() && b3.addTransaction(t5)) {
        System.out.println(
            "t5 added to block b3. Before b3 is mined and added to the chain, the balances are:");
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
        System.out.println("t5 failed to add to block b3");
      }
    } else {
      System.out.println("t5 failed to create");
    }

    Transaction t6 = B.transferFund(C.getPublicKey(), 80.0);
    if (t6 != null) {
      if (t6.verifySignature() && b3.addTransaction(t6)) {
        System.out.println(
            "t6 added to block b3. Before b3 is mined and added to the chain, the balances are:");
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
        System.out.println("t6 failed to add to block b3");
      }
    } else {
      System.out.println("t6 failed to create");
    }

    // now mine the block 3.
    if (C.mineBlock(b3)) {
      System.out.println("C mined b3, hashID is:");
      System.out.println(b3.getHashID());
      blockchain.addBlock(b3);
      System.out.println("After block b3 is added to the chain, the balances are:");
      displayBalanceAfterBlock(b3, genesisMiner, A, B, C);
    }

    // try block 4. It has the wrong hashID for its previous block. It cannot be added
    // because the program does not handle this, then you will find the transaction
    // fee is not correct from this point
    Block b4 = new Block(b2.getHashID(), difficultLevel);
    System.out.println("\n\nblock b4 created");
    Transaction t7 = genesisMiner.transferFund(A.getPublicKey(), 500);
    Transaction t8 = genesisMiner.transferFund(B.getPublicKey(), 200);
    Transaction t9 = genesisMiner.transferFund(C.getPublicKey(), 100);

    if (t7 != null && t7.verifySignature() && b4.addTransaction(t7)) {
      System.out.println("t7 added to b4");
    } else {
      if (t7 == null) {
        System.out.println("t7 failed to create");
      } else if (!t7.verifySignature()) {
        System.out.println("t7 failed to verify signature");
      } else {
        System.out.println("t7 failed to add to b4");
      }
    }

    if (t8 != null && t8.verifySignature() && b4.addTransaction(t8)) {
      System.out.println("t8 added to b4");
    } else {
      if (t8 == null) {
        System.out.println("t8 failed to create");
      } else if (!t8.verifySignature()) {
        System.out.println("t8 failed to verify signature");
      } else {
        System.out.println("t8 failed to add to b4");
      }
    }

    if (t9 != null && t9.verifySignature() && b4.addTransaction(t9)) {
      System.out.println("t9 added to b4");
    } else {
      if (t9 == null) {
        System.out.println("t9 failed to create");
      } else {
        System.out.println("t9 failed to add to b4");
      }
    }

    // now mine the block 4.
    if (C.mineBlock(b4)) {
      System.out.println("C mined b4, hashID is:");
      System.out.println(b4.getHashID());
      blockchain.addBlock(b4);
      System.out.println("ERROR: After block b4 is NOT added to the chain, the balances are:");
      displayBalanceAfterBlock(b4, genesisMiner, A, B, C);
    }
    System.out.println("====>the length of the blockchain=" + blockchain.size());

    Transaction t10 = A.transferFund(C.getPublicKey(), 20);
    Block b5 = new Block(blockchain.getLastBlock().getHashID(), difficultLevel);
    System.out.println("\n\nblock b5 is created");
    // let's add t7. Since our program does not check it,
    // t7 will be added twice
    if (t7 != null && t7.verifySignature() && b5.addTransaction(t7)) {
      System.out.println("ERROR: t7 added to b5 again");
    } else {
      if (t7 == null) {
        System.out.println("t7 failed to create");
      } else {
        System.out.println("t7 failed to add to b5");
      }
    }

    if (t10 != null && t10.verifySignature() && b5.addTransaction(t10)) {
      System.out.println("t10 added to b5");
    } else {
      if (t10 == null) {
        System.out.println("t10 failed to create");
      } else {
        System.out.println("t10 failed to add to b5");
      }
    }
    // now mine the block 5.
    if (A.mineBlock(b5)) {
      System.out.println("A mined b5, hashID is:");
      System.out.println(b5.getHashID());
      blockchain.addBlock(b5);
      System.out.println("After block b5 is added to the chain, the balances are:");
      displayBalanceAfterBlock(b5, genesisMiner, A, B, C);
    }

    System.out.println("=========BlockChain platform shuts down=========");
  }

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
