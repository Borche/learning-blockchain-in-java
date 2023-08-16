package org.andruch.mains;

import org.andruch.Wallet;

import java.util.Scanner;

public class TestWallet_1 {
  public static void main(String[] args) {
    Scanner in = new Scanner(System.in);
    System.out.println("Name:");
    String name = in.nextLine();
    System.out.println("Password:");
    String password = in.nextLine();
    in.close();
    Wallet w = new Wallet(name, password);
    System.out.println("Wallet created: " + w.getWalletName());

    // Load the wallet
    Wallet w2 = new Wallet(name, password);
    System.out.println("Wallet loaded: " + w2.getWalletName());
  }
}
