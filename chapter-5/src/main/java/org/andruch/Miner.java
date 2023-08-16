package org.andruch;

public class Miner extends Wallet {
  public Miner(String walletName, String password) {
    super(walletName, password);
  }

  public boolean mineBlock(Block block) {
    return block.mineTheBlock();
  }
}
