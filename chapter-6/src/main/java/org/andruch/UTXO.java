/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch;

import java.io.Serializable;
import java.security.PublicKey;
import lombok.Getter;

@Getter
public class UTXO implements Serializable {
  private static final long serialVersionUID = 1L;
  private String hashID;
  private String parentTransactionID;
  private PublicKey receiver;
  private PublicKey sender;
  private long timestamp;
  private double fundsTransferred;
  private long seqNumber = 0;

  public UTXO(
      String parentTransactionID, PublicKey sender, PublicKey receiver, double fundsTransferred) {
    this.seqNumber = UtilityMethods.getUniqueNumber();
    this.parentTransactionID = parentTransactionID;
    this.receiver = receiver;
    this.sender = sender;
    this.fundsTransferred = fundsTransferred;
    this.timestamp = UtilityMethods.getTimeStamp();
    this.hashID = computeHashID();
  }

  private String computeHashID() {
    String message =
        parentTransactionID
            + UtilityMethods.getKeyString(sender)
            + UtilityMethods.getKeyString(receiver)
            + Double.toHexString(fundsTransferred)
            + Long.toHexString(timestamp)
            + Long.toHexString(seqNumber);
    return UtilityMethods.messageDigestSHA256_toString(message);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof UTXO other) {
      return getHashID().equals(other.getHashID());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getHashID().hashCode();
  }

  public boolean isMiningReward() {
    return false;
  }
}
