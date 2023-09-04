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

  public synchronized void addBlock(Block block) {
    if (block.getPreviousBlockHashID().equals(this.getLastBlock().getHashID())) {
      this.blockchain.add(block);
    }
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

  public double findRelatedUTXOs(
      PublicKey key,
      List<UTXO> all,
      List<UTXO> spent,
      List<UTXO> unspent,
      List<Transaction> sentTransactions) {
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
}
