package com.crypto.action;

import java.math.BigInteger;

public class AttackECDSA {
    public static void main(String[] args) throws Exception
    {
        BigInteger order = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141");

        //hash value of first msg
        BigInteger h1 = new BigInteger("320026739459778556085970613903841025917693204146");

        //hash value of second msg
        BigInteger h2 = new BigInteger("657861580680934472493746918957902219226036806926");

        //r values is same, and this is the bug
        BigInteger r = new BigInteger("28695618543805844332113829720373285210420739438570883203839696518176414791234");

        //s value of 1st message
        BigInteger s1 = new BigInteger("")
    }
}
