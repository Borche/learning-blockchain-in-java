/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch;

import java.io.Serial;
import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blockchain implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  public static final double MINING_REWARD = 100.0;
  private LedgerList<Block> blockchain;

  public Blockchain(Block genesisBlock) {
    this.blockchain = new LedgerList<>();
    this.blockchain.add(genesisBlock);
  }

  public static boolean validateBlockchain(Blockchain ledger) {
    int size = ledger.size();
    for (int i = size - 1; i > 0; i--) {
      Block currentBlock = ledger.getBlock(i);
      boolean b = currentBlock.verifySignature(currentBlock.getCreator());
      if (!b) {
        System.out.println("validateBlockChain(): block " + (i + 1) + "  signature is invalid.");
        return false;
      }
      b =
          UtilityMethods.hashMeetsDifficultyLevel(
                  currentBlock.getHashID(), currentBlock.getDifficultyLevel())
              && currentBlock.computeHashID().equals(currentBlock.getHashID());
      if (!b) {
        System.out.println("validateBlockChain():  block  " + (i + 1) + "  its hashing is bad");
        return false;
      }
      Block previousBlock = ledger.getBlock(i - 1);
      b = currentBlock.getPreviousBlockHashID().equals(previousBlock.getHashID());
      if (!b) {
        System.out.println(
            "validateBlockChain():  block  " + (i + 1) + "  invalid previous block hashID");
        return false;
      }
    }
    Block genesisBlock = ledger.getGenesisBlock();
    // confirm the genesis is signed
    boolean b2 = genesisBlock.verifySignature(genesisBlock.getCreator());
    if (!b2) {
      System.out.println("validateBlockChain():  genesis block is tampered, signature bad");
      return false;
    }

    b2 =
        UtilityMethods.hashMeetsDifficultyLevel(
                genesisBlock.getHashID(), genesisBlock.getDifficultyLevel())
            && genesisBlock.computeHashID().equals(genesisBlock.getHashID());
    if (!b2) {
      System.out.println("validateBlockChain(): gensis block is hashing is bad");
      return false;
    }
    return true;
  }

  public PublicKey getGenesisMiner() {
    return this.getGenesisBlock().getCreator();
  }

  public synchronized boolean addBlock(Block block) {
    if (this.size() == 0) {
      this.blockchain.add(block);
      return true;
    }
    if (block.getPreviousBlockHashID().equals(this.getLastBlock().getHashID())) {
      this.blockchain.add(block);
      return true;
    }
    return false;
  }

  public Block getGenesisBlock() {
    return blockchain.getFirst();
  }

  public Block getLastBlock() {
    return blockchain.getLast();
  }

  public int size() {
    return blockchain.size();
  }

  public Block getBlock(int index) {
    return blockchain.findByIndex(index);
  }

  protected boolean transactionExists(Transaction T) {
    int size = this.blockchain.size();
    for (int i = size - 1; i > 0; i--) {
      Block b = this.blockchain.findByIndex(i);
      int bs = b.getNumberOfTransactions();
      for (int j = 0; j < bs; j++) {
        Transaction t2 = b.getTransaction(j);
        if (T.equals(t2)) {
          return true;
        }
      }
    }
    return false;
  }

  public double findRelatedUTXOs(
      PublicKey key,
      List<UTXO> all,
      List<UTXO> spent,
      List<UTXO> unspent,
      List<Transaction> sentTransactions,
      List<UTXO> rewards) {
    double gain = 0.0, spending = 0.0;
    Map<String, UTXO> map = new HashMap<>();
    int limit = size();
    for (int a = 0; a < limit; a++) {
      Block block = blockchain.findByIndex(a);
      int size = block.getNumberOfTransactions();
      for (int i = 0; i < size; i++) {
        Transaction t = block.getTransaction(i);
        int N;
        if (a != 0 && t.getSender().equals(key)) {
          N = t.getNumberOfInputsUTXOs();
          for (int x = 0; x < N; x++) {
            UTXO in = t.getInputUTXO(x);
            spent.add(in);
            map.put(in.getHashID(), in);
            spending += in.getFundsTransferred();
          }
          sentTransactions.add(t);
        }
        N = t.getNumberOfOutputUTXOs();
        for (int x = 0; x < N; x++) {
          UTXO ut = t.getOutputUTXO(x);
          if (ut.getReceiver().equals(key)) {
            all.add(ut);
            gain += ut.getFundsTransferred();
          }
        }
      }
      // add reward transactions. The reward might be null since a miner might underpay himself
      if (block.getCreator().equals(key)) {
        Transaction rt = block.getRewardTransaction();
        if (rt != null && rt.getNumberOfOutputUTXOs() > 0) {
          UTXO ux = rt.getOutputUTXO(0);
          // double check again, so a miner can only reward himself
          // if he rewards others, this reward is not counted
          if (ux.getReceiver().equals(key)) {
            rewards.add(ux);
            all.add(ux);
            gain += ux.getFundsTransferred();
          }
        }
      }
    }
    for (int i = 0; i < all.size(); i++) {
      UTXO u = all.get(i);
      if (!map.containsKey(u.getHashID())) {
        unspent.add(u);
      }
    }
    return gain - spending;
  }

  public double findRelatedUTXOs(
      PublicKey key,
      List<UTXO> all,
      List<UTXO> spent,
      List<UTXO> unspent,
      List<Transaction> sentTransactions) {
    List<UTXO> rewards = new ArrayList<>();
    return findRelatedUTXOs(key, all, spent, unspent, sentTransactions, rewards);
  }

  public double findRelatedUTXOs(
      PublicKey key, List<UTXO> all, List<UTXO> spent, List<UTXO> unspent) {
    List<Transaction> sendingTransactions = new ArrayList<>();
    return findRelatedUTXOs(key, all, spent, unspent, sendingTransactions);
  }

  public double checkBalance(PublicKey key) {
    List<UTXO> all = new ArrayList<>();
    List<UTXO> spent = new ArrayList<>();
    List<UTXO> unspent = new ArrayList<>();
    return findRelatedUTXOs(key, all, spent, unspent);
  }

  public List<UTXO> findUnspentUTXOs(PublicKey key) {
    List<UTXO> all = new ArrayList<>();
    List<UTXO> spent = new ArrayList<>();
    List<UTXO> unspent = new ArrayList<>();
    findRelatedUTXOs(key, all, spent, unspent);
    return unspent;
  }

  public double findUnspentUTXOs(PublicKey key, List<UTXO> unspent) {
    List<UTXO> all = new ArrayList<>();
    List<UTXO> spent = new ArrayList<>();
    return findRelatedUTXOs(key, all, spent, unspent);
  }

  // Private constructor for copying purposes
  private Blockchain(LedgerList<Block> chain) {
    this.blockchain = new LedgerList<>();
    int size = chain.size();
    for (int i = 0; i < size; i++) {
      this.blockchain.add(chain.findByIndex(i));
    }
  }

  // Shallow copy. The blocks and their order are preserved.
  public synchronized Blockchain copy_NotDeepCopy() {
    return new Blockchain(this.blockchain);
  }
}
