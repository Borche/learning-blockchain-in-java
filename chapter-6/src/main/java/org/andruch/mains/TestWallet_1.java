/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch.mains;

import java.util.Scanner;
import org.andruch.Wallet;

public class TestWallet_1 {
  public static void main(String[] args) {
    Scanner in = new Scanner(System.in);
    System.out.println("Name:");
    String name = in.nextLine();
    System.out.println("Password:");
    String password = in.nextLine();
    in.close();
    Wallet w = new Wallet(name, password);
    System.out.println("Wallet created: " + w.getName());

    // Load the wallet
    Wallet w2 = new Wallet(name, password);
    System.out.println("Wallet loaded: " + w2.getName());
  }
}
