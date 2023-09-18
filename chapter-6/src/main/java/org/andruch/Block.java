/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch;

import java.io.Serial;
import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class Block implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private static final int TRANSACTION_UPPER_LIMIT = 100;
  // to set a lower limit for demonstration purpose
  public static final int TRANSACTION_LOWER_LIMIT = 1;
  private int difficultyLevel = 20;

  // The miner who created/mined this block
  private PublicKey creator;
  // Once a block has been mined, no more changes should be allowed.
  private boolean mined = false;

  // The minter must sign the block so that othet miners can verify the signature.
  @Getter(AccessLevel.NONE)
  private byte[] signature = null;

  // The transaction to reward the miner for his/her hard work
  private Transaction rewardTransaction = null;

  @Getter(AccessLevel.NONE)
  private List<Transaction> transactions = new ArrayList<>();

  private long timestamp;
  private String previousBlockHashID;
  private int nonce = 0;
  private String hashID;

  public Block(String previousBlockHashID, int difficultyLevel, PublicKey creator) {
    this.previousBlockHashID = previousBlockHashID;
    this.timestamp = UtilityMethods.getTimeStamp();
    this.difficultyLevel = difficultyLevel;
    this.creator = creator;
  }

  protected String computeHashID() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.previousBlockHashID + Long.toHexString(this.timestamp));
    sb.append(this.computeMerkleRoot());
    sb.append("" + nonce);
    byte[] b = UtilityMethods.messageDigestSHA256_toBytes(sb.toString());
    return UtilityMethods.toBinaryString(b);
  }

  private String computeMerkleRoot() {
    String[] hashes;
    // Lets allow blocks where the reward transaction is null
    if (this.rewardTransaction == null) {
      hashes = new String[this.transactions.size()];
      for (int i = 0; i < this.transactions.size(); i++) {
        hashes[i] = this.transactions.get(i).getHashID();
      }
    } else {
      hashes = new String[this.transactions.size() + 1];
      for (int i = 0; i < this.transactions.size(); i++) {
        hashes[i] = this.transactions.get(i).getHashID();
      }
      hashes[hashes.length - 1] = this.rewardTransaction.getHashID();
    }

    return UtilityMethods.computeMerkleTreeRootHash(hashes);
  }

  // Only the creator of this block can mine the block, and the block can only be mined for once.
  public boolean mineTheBlock(PublicKey key) {
    if (!key.equals(creator) || isMined()) return mined;
    hashID = computeHashID();
    while (!UtilityMethods.hashMeetsDifficultyLevel(hashID, difficultyLevel)) {
      // System.out.println(hashID);
      this.nonce++;
      this.hashID = computeHashID();
    }
    return true;
  }

  /**
   * A block must be signed. This is how it works: The miner of this block generates a signature
   * based on the block's hashID and calls this method to set the signature. This method checks if
   * the signature is valid or not before it is set. Once the signature is set, no change is
   * allowed.
   *
   * @return True if the signature was set successfully, false otherwise.
   */
  public boolean signTheBlock(PublicKey publicKey, byte[] signature) {
    if (!isSigned()
        && publicKey.equals(creator)
        && UtilityMethods.verifySignature(publicKey, signature, this.getHashID())) {
      this.signature = signature;
      return true;
    }
    return false;
  }

  // When a wallet/miner needs to add this block into its local blockchain,
  // it is necessary to verify the signature. The verification requires
  // a public key, which is usually the creator's key.
  public boolean verifySignature(PublicKey publicKey) {
    return UtilityMethods.verifySignature(publicKey, this.signature, this.getHashID());
  }

  // The transaction fee does not include the reward transaction
  public double getTransactionFeeAmount() {
    return this.transactions.size() * Transaction.TRANSACTION_FEE;
  }

  // A block has only one reward transaction. It can be added only by the block
  // creator (the miner) and it cannot be changed once it has been added.
  public boolean setRewardTransaction(PublicKey publicKey, Transaction rewardTransaction) {
    if (this.rewardTransaction == null && publicKey.equals(this.creator)) {
      this.rewardTransaction = rewardTransaction;
      return true;
    }
    return false;
  }

  public boolean addTransaction(Transaction t, PublicKey key) {
    if (this.getNumberOfTransactions() >= TRANSACTION_UPPER_LIMIT) {
      return false;
    }
    if (key.equals(this.creator) && !this.isMined() && !this.isSigned()) {
      this.transactions.add(t);
      return true;
    }
    return false;
  }

  /** only the creator can delete a transaction before mined and before signed */
  public boolean deleteTransaction(Transaction ts, PublicKey key) {
    if (!this.mined && !this.isSigned() && key.equals(this.getCreator())) {
      return this.transactions.remove(ts);
    } else {
      return false;
    }
  }

  public boolean deleteTransaction(int index, PublicKey key) {
    if (!this.mined && !this.isSigned() && key.equals(this.getCreator())) {
      Transaction ts = this.transactions.remove(index);
      return (ts != null);
    } else {
      return false;
    }
  }

  public int getNumberOfTransactions() {
    return transactions.size();
  }

  public Transaction getTransaction(int i) {
    return transactions.get(i);
  }

  public boolean isSigned() {
    return this.signature != null;
  }
}
