/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class Block implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private int difficultyLevel = 20;

  @Getter(AccessLevel.NONE)
  private List<String> transactions = new ArrayList<>();

  private long timestamp;
  private String previousBlockHashID;
  private int nonce = 0;
  private String hashID;

  public Block(String previousBlockHashID, int difficultyLevel) {
    this.previousBlockHashID = previousBlockHashID;
    this.timestamp = UtilityMethods.getTimeStamp();
    this.difficultyLevel = difficultyLevel;
  }

  protected String computeHashID() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.previousBlockHashID + Long.toHexString(this.timestamp));
    for (String t : transactions) {
      sb.append(t);
    }
    sb.append(Integer.toHexString(this.difficultyLevel) + nonce);
    byte[] b = UtilityMethods.messageDigestSHA256_toBytes(sb.toString());
    return UtilityMethods.toBinaryString(b);
  }

  public boolean mineTheBlock() {
    hashID = computeHashID();
    while (!UtilityMethods.hashMeetsDifficultyLevel(hashID, difficultyLevel)) {
      // System.out.println(hashID);
      this.nonce++;
      this.hashID = computeHashID();
    }
    return true;
  }

  public void addTransaction(String t) {
    this.transactions.add(t);
  }
}
