/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch;

import java.security.PublicKey;
import java.util.ArrayList;

public class BlockchainPlatform {
  public static Blockchain ledger;

  public static void main(String[] args) {
    ArrayList<Wallet> users = new ArrayList<Wallet>();
    int difficultLevel = 22;
    System.out.println("Blockchain platform starts ...");
    System.out.println("creating genesis miner, genesis transaction and genesis block");
    // create a genesis miner to start a blockchain
    Miner genesisMiner = new Miner("genesis", "genesis");
    users.add(genesisMiner);
    // create genesis block
    Block genesisBlock = new Block("0", difficultLevel, genesisMiner.getPublicKey());
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
    b = genesisBlock.addTransaction(gt, genesisMiner.getPublicKey());
    if (!b) {
      System.out.println("failed to add the genesis transaction to the genesis block. System quit");
      System.exit(1);
    }
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
    ledger = new Blockchain(genesisBlock);
    System.out.println("block chain genesis successful");
    // genesisMiner copies the blockchain to his local ledger
    genesisMiner.setLocalLedger(ledger);
    System.out.println(
        "genesis miner balance: " + genesisMiner.getCurrentBalance(genesisMiner.getLocalLedger()));

    System.out.println("creating two miners and one wallet");
    Miner A = new Miner("A", "A");
    Wallet B = new Wallet("B", "B");
    Miner C = new Miner("C", "C");
    users.add(A);
    users.add(B);
    users.add(C);
    // only set copy now, so everyone has a local copy
    A.setLocalLedger(ledger.copy_NotDeepCopy());
    B.setLocalLedger(ledger.copy_NotDeepCopy());
    C.setLocalLedger(ledger.copy_NotDeepCopy());
    Block b2 = A.createNewBlock(A.getLocalLedger(), difficultLevel);
    System.out.println("Block b2 created by A");
    System.out.println("genesis miner sends B: 500+200, C: 300+100");
    PublicKey[] receiver = {B.getPublicKey(), B.getPublicKey(), C.getPublicKey(), C.getPublicKey()};
    double[] funds = {500, 200, 300, 100};
    Transaction t1 = genesisMiner.transferFund(receiver, funds);
    System.out.println("A is collecting Transactions ...");
    if (A.addTransaction(t1, b2)) {
      System.out.println("t1 added into block b2");
    } else {
      System.out.println("Warning: t1 cannot be added into b2");
    }

    System.out.println("A is generating reward transaction");
    if (A.generateRewardTransaction(b2)) {
      System.out.println("rewarding transaction successfully added to b2");
    } else {
      System.out.println("rewarding transaction cannot be added to b2");
    }
    System.out.println("A is mining block b2");
    if (A.mineBlock(b2)) {
      System.out.println("b2 is mined and signed by A");
    }
    // now, let C verify this block b2
    b = verifyBlock(C, b2, "b2");
    if (b) {
      System.out.println("all blockchain users begin to update their local blockchain now with b2");
      allUpdateBlockchain(users, b2);
      System.out.println("after b2 is added to the blockchain, the balances are:");
      displayAllBalances(users);
    }

    System.out.println(
        "total should="
            + (20000 + Blockchain.MINING_REWARD)
            + ". Adding all wallets, total="
            + (genesisMiner.getCurrentBalance(ledger)
                + A.getCurrentBalance(ledger)
                + B.getCurrentBalance(ledger)
                + C.getCurrentBalance(ledger)));

    Block b3 = A.createNewBlock(ledger, difficultLevel);
    System.out.println("Again, genesis miner sends B: 500+200, C: 300+100");
    Transaction t2 = genesisMiner.transferFund(receiver, funds);
    // try to add t1 into b3 to check, it should fail
    if (A.addTransaction(t1, b3)) {
      System.out.println("t1 added into block b3");
    } else {
      System.out.println("Warning: t1 cannot be added into b3, t1 already exists");
    }

    if (A.addTransaction(t2, b3)) {
      System.out.println("t2 added into block b3");
    } else {
      System.out.println("Warning: t2 cannot be added into b3");
    }
    System.out.println("A is collecting Transactions ...");
    System.out.println("A is generating reward transaction");
    if (A.generateRewardTransaction(b3)) {
      System.out.println("rewarding transaction successfully added to b3");
    } else {
      System.out.println("rewarding transaction CANNOT be added to b3");
    }

    // let miner C to verify this block b3
    // Assume that miner C wants to change the block
    if (C.mineBlock(b3)) {
      System.out.println("b3 is mined and signed by C");
    } else {
      System.out.println("C cannot mine b3");
    }

    // assume that miner C wants to change the block
    if (C.deleteTransaction(b3.getTransaction(0), b3)) {
      System.out.println("C deleted the first transaction from b3");
    } else {
      System.out.println("C cannot delete the first transaction from b3");
    }

    if (A.mineBlock(b3)) {
      System.out.println("b3 is mined and signed by A");
    } else {
      System.out.println("ERROR: b3 is created by A, why A cannot mine it?");
    }

    // Assume that miner A wants to change the block
    if (A.deleteTransaction(b3.getTransaction(0), b3)) {
      System.out.println("A deleted the first transaction from b3");
    } else {
      System.out.println("A cannot delete the first transaction from b3, block already signed");
    }

    // now, let C verify this block b3
    b = verifyBlock(C, b3, "b3");
    if (b) {
      System.out.println("all blockchain users begin to update their local blockchain now with b3");
      allUpdateBlockchain(users, b3);
      System.out.println("after b3 is added to the blockchain, the balances are:");
      displayAllBalances(users);
    }

    System.out.println(
        "total should="
            + (20000 + Blockchain.MINING_REWARD * 2)
            + ". Adding all wallets, total="
            + (genesisMiner.getCurrentBalance(ledger)
                + A.getCurrentBalance(ledger)
                + B.getCurrentBalance(ledger)
                + C.getCurrentBalance(ledger)));

    Transaction t5 = C.transferFund(C.getPublicKey(), 20);
    // assume that miner A wants to add t5 into the b3
    if (A.addTransaction(t5, b3)) {
      System.out.println("A added t5 into b3");
    } else {
      System.out.println("A cannot add t5 into b3, block already signed");
    }

    System.out.println();
    Block b4 = C.createNewBlock(ledger, difficultLevel);
    System.out.println("C created block b4");

    if (C.addTransaction(t5, b4)) {
      System.out.println("C added t5 into b4");
    } else {
      System.out.println("C failed to add t5 into b4");
    }

    Transaction t6 = C.transferFund(A.getPublicKey(), 100);
    Transaction t7 = B.transferFund(A.getPublicKey(), 100);
    Transaction t8 = C.transferFund(B.getPublicKey(), 100);
    if (C.addTransaction(t6, b4)) {
      System.out.println("C added t6 into b4");
    } else {
      System.out.println("C failed to add t6 into b4");
    }
    if (C.addTransaction(t7, b4)) {
      System.out.println("C added t7 into b4");
    } else {
      System.out.println("C failed to add t7 into b4");
    }
    if (C.addTransaction(t8, b4)) {
      System.out.println("C added t8 into b4");
    } else {
      System.out.println("C failed to add t8 into b4");
    }
    if (C.generateRewardTransaction(b4)) {
      System.out.println("C generated reward transaction in b4");
    } else {
      System.out.println("C CANNOT generat reward transaction in b4");
    }
    if (C.mineBlock(b4)) {
      System.out.println("C mined b4, hashID:");
      System.out.println(b4.getHashID());
      b = verifyBlock(A, b4, "b4");
      if (b) {
        System.out.println(
            "all blockchain users begin to update their local blockchain now with b4");
        allUpdateBlockchain(users, b4);
        System.out.println("after b4 is added to the blockchain, the balances are:");
        displayAllBalances(users);
      }
    }

    // let's try to add it twice, it should fail
    b = ledger.addBlock(b4);
    if (b) {
      System.out.println("ERROR: b4 is added again into the ledger.");
    } else {
      System.out.println("b4 CANNOT be added again into the ledger.");
    }
    // check the balance again
    System.out.println("after b4, the balances are:");
    displayAllBalances(users);

    System.out.println(
        "total should="
            + (20000 + Blockchain.MINING_REWARD * 3)
            + ". Adding all wallets, total="
            + (genesisMiner.getCurrentBalance(ledger)
                + A.getCurrentBalance(ledger)
                + B.getCurrentBalance(ledger)
                + C.getCurrentBalance(ledger)));

    System.out.println();
    System.out.println("========================================");
    System.out.println("blockchain looks like:");
    System.out.println();
    PrintUtils.displayBlockchain(ledger, System.out, 0);
    System.out.println("=========BlockChain platform shuts down=========");
  }

  public static boolean verifyBlock(Wallet w, Block b, String blockName) {
    if (w.verifyGuestBlock(b)) {
      System.out.println(w.getName() + " accepted block " + blockName);
      return true;
    } else {
      System.out.println(w.getName() + " rejected block " + blockName);
      return false;
    }
  }

  public static void allUpdateBlockchain(ArrayList<Wallet> users, Block b) {
    for (int i = 0; i < users.size(); i++) {
      Wallet w = users.get(i);
      w.updateLocalLedger(b);
      System.out.println(w.getName() + " updated its local blockchain.");
    }
  }

  public static void displayUTXOs(ArrayList<UTXO> us, int level) {
    for (int i = 0; i < us.size(); i++) {
      UTXO xo = us.get(i);
      PrintUtils.printUTXO(xo, System.out, level);
    }
  }

  public static void displayBalance(Wallet w) {
    Blockchain ledger = w.getLocalLedger();
    ArrayList<UTXO> all = new ArrayList<UTXO>();
    ArrayList<UTXO> spent = new ArrayList<UTXO>();
    ArrayList<UTXO> unspent = new ArrayList<UTXO>();
    ArrayList<Transaction> sentT = new ArrayList<Transaction>();
    ArrayList<UTXO> rewards = new ArrayList<UTXO>();
    double balance = ledger.findRelatedUTXOs(w.getPublicKey(), all, spent, unspent, sentT, rewards);
    int level = 0;
    PrintUtils.printWithTab(System.out, level, w.getName() + "{");
    PrintUtils.printWithTab(System.out, level + 1, "All UTXOs:");
    displayUTXOs(all, level + 2);
    PrintUtils.printWithTab(System.out, level + 1, "Spent UTXOs:");
    displayUTXOs(spent, level + 2);
    PrintUtils.printWithTab(System.out, level + 1, "unspent UTXOs:");
    displayUTXOs(unspent, level + 2);
    if (w instanceof Miner) {
      PrintUtils.printWithTab(System.out, level + 1, "Mining Rewards:");
      displayUTXOs(rewards, level + 2);
    }
    PrintUtils.printWithTab(System.out, level + 1, "Balance=" + balance);
    PrintUtils.printWithTab(System.out, level, "}");
  }

  public static void displayAllBalances(ArrayList<Wallet> users) {
    for (int i = 0; i < users.size(); i++) {
      displayBalance(users.get(i));
    }
  }
}
