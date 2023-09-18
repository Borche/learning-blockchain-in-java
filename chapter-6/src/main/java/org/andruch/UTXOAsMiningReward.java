/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch;

import java.io.Serial;
import java.security.PublicKey;

public class UTXOAsMiningReward extends UTXO {
  @Serial private static final long serialVersionUID = 1L;

  public UTXOAsMiningReward(
      String parentTransactionID, PublicKey sender, PublicKey receiver, double fundsTransferred) {
    super(parentTransactionID, sender, receiver, fundsTransferred);
  }

  public boolean isMiningReward() {
    return true;
  }
}
