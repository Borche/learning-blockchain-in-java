package org.andruch;


public class TestHashing {
    public static void main(String[] args) {
        String msg = "Isf you are a drop of tears in my eyes";
        String hash = UtilityMethods.messageDigestSHA256_toString(msg);
        System.out.println(hash);
    }
}