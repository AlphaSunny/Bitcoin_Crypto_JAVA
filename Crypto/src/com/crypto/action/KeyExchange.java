package com.crypto.action;

import com.crypto.entity.Point;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.cert.PolicyNode;

import static com.crypto.action.Add.displayPoint;
import static com.crypto.action.DoubleAndAdd.applyDoubleAndAddMethod;

public class KeyExchange {
    public static void main(String[] args)
    {
        double a = -4, b = 4;
        Point p = new Point();
        p.setPointX(BigDecimal.valueOf(-2));
        p.setPointY(BigDecimal.valueOf(-2));

        // info of yang
        BigInteger ky = new BigInteger("100000000021");    // private key of yang
        Point yangPublic = applyDoubleAndAddMethod(p, ky, a, b);
        System.out.println("Yang's public key:\t" + displayPoint(yangPublic));

        // info of xu
        BigInteger kx = new BigInteger("100000011121");
        Point xuPublic = applyDoubleAndAddMethod(p, kx, a, b);
        System.out.println("Xu's public key:\t" + displayPoint(xuPublic));

        // exchange
        Point yangShared = applyDoubleAndAddMethod(xuPublic, ky, a, b);
        Point xuShared = applyDoubleAndAddMethod(yangPublic, kx, a, b);

        System.out.println("Yang produce this shared key: " + displayPoint(yangShared));
        System.out.println("Xu produces this shared key: " + displayPoint(xuShared));
    }
}
