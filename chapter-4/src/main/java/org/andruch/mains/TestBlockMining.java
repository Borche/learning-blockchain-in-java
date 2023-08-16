/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch.mains;

import org.andruch.Block;

public class TestBlockMining {
  public static void main(String[] args) {
    Block b = new Block("0", 20);
    for (int t = 0; t < 10; t++) {
      b.addTransaction("Transaction-" + t);
    }
    System.out.println("Starting the mining process");
    long start = System.currentTimeMillis();
    b.mineTheBlock();
    long stop = System.currentTimeMillis();
    System.out.println("Block is successfully mined, hashID is: " + b.getHashID());
    System.out.println("It took " + (stop - start) + " milliseconds.");
  }
}
