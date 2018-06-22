package com.crypto.action;

import com.crypto.entity.Point;
import com.sun.deploy.util.SyncAccess;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.PolicyNode;
import java.util.Date;
import java.util.Random;

public class ECC {
    public static void main(String[] args) throws Exception {
        Random rand = new Random();

        BigInteger mod;
        BigInteger order;

        mod = generatePrimeModulo();
        order = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);

        BigInteger a = new BigInteger("0");
        BigInteger b = new BigInteger("7");

        //base point on the curve
        Point basePoint = new Point();


        basePoint.setPointX(new BigInteger("55066263022277343669578718895168534326250603453777594175500187360389116729240"));
        basePoint.setPointY(new BigInteger("32670510020758816978083085130507043184471273380659243275938904335757337482424"));


        // brute force
        System.out.println("------------------------------");
        System.out.println("brute force addition");
        System.out.println("------------------------------");

        System.out.println("P: " + displayPoint(basePoint));

        Point newPoint = pointAddition(basePoint, basePoint, a, b, mod);

        System.out.println("2P: " + displayPoint(newPoint));

        for(int i = 3; i<=20; i++)
        {
            try
            {
                newPoint = pointAddition(newPoint, newPoint, a, b, mod);
                System.out.println(i + "P: " +displayPoint(newPoint));
            }
            catch ( Exception ex)
            {
                System.out.println("order of group: " + (i));
                break;
            }
        }

        System.out.println();


        //------------------------------------------------------------------------
        // key exchange
        System.out.println("-------------------------------------");
        System.out.println("Elliptic Curve Diffie Hellman Key Exchange");
        System.out.println("---------------------------------------------");

        Date generationBegin = new Date();

        System.out.println("public key generation...");

        BigInteger kAlice = new BigInteger("2010000000000017");  // alice's private key
        Point alicePublic = applyDoubleAndAddMethod(basePoint, kAlice, a, b, mod);
        System.out.println("Alice public: \t" + displayPoint(alicePublic));

        BigInteger kBob = new BigInteger("2010000000000061");
        Point bobPublic = applyDoubleAndAddMethod(basePoint, kBob, a, b, mod);
        System.out.println("Bob public key: \t" + displayPoint(bobPublic));

        Date generationEnd = new Date();

        System.out.println("Public key generation lasts " +
                (double)(generationBegin.getTime() - generationEnd.getTime())/1000 + " seconds\n");

        //----------------------------------------------------------
        Date exchangeBegin = new Date();
        System.out.println("Key exchange...");
        Point aliceShared = applyDoubleAndAddMethod(bobPublic, kAlice, a, b, mod);
        System.out.println("Alice Shared:\t" + displayPoint(aliceShared));
        Point bobShared = applyDoubleAndAddMethod(alicePublic, kBob, a, b, mod);
        System.out.println("Bod shared:\t" + displayPoint(bobShared));

        Date exchangeEnd = new Date();

        System.out.println("shared key lasts " + (double) (exchangeEnd.getTime() -
                exchangeBegin.getTime())/1000+ " seconds\n");


        //------------------------------------------------------------

        //ecdsa - elliptic curve digital signature algorithm

        System.out.println("---------------------------------------------------");
        System.out.println("Elliptic Curve Digital Signature Algorithm - ECDSA");
        System.out.println("---------------------------------------------------");

        String text = "ECC beats RSA";
        //String text = "ECC beats RSA, bitcoin";

        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(text.getBytes());
        byte[] hashByte = md.digest();

        BigInteger hash = new BigInteger(hashByte).abs();

        System.out.println("message: " + text);
        System.out.println("hash: " + hash);

        //---------------------------------------------------

        // use the private key
        BigInteger privateKey = new BigInteger("75263518707598184987916378021939673586055614731957507592904438851787542395619");

        Point publicKey = applyDoubleAndAddMethod(basePoint, privateKey, a, b, mod);

        System.out.println("public key: " + displayPoint(publicKey));

        // BigInteger randomKey, 每次签名都必须产生一个随机的biginterget，和私钥的长度一样
        BigInteger randomKey = new BigInteger("28695618543805844332113829720373285210420739438570883203839696518176414791234");

        Point randomPoint = applyDoubleAndAddMethod(basePoint, randomKey, a, b, mod);

        System.out.println("Random point: " + displayPoint(randomPoint));

        //----------------------------------------------------------------------
        //signing

        System.out.println("\nSigning...");
        Date signing = new Date();
        BigInteger r = randomPoint.getPointX().remainder(order);                       //返回一个BigInteger，其值是 (this % val)
        BigInteger s = (hash.add(r.multiply(privateKey)).multiply(multiplicativeInverse(randomKey, order))).remainder(order);
        System.out.println("Signature: (r, s) = (" + r + ", "+s+ ")");

        Date signEnd = new Date();

        System.out.println("\nmessage signing lasts " + (double)(signEnd.getTime() - signing.getTime())/1000 + " seconds\n");

        // verification

        Date verifyBegin = new Date();
        System.out.println("verification....");

        BigInteger w =  multiplicativeInverse(s, order);
        Point u1 = applyDoubleAndAddMethod(basePoint, (hash.multiply(w).remainder(order)), a, b,mod);
        Point u2 = applyDoubleAndAddMethod(publicKey, (r.multiply(w).remainder(order)), a, b, mod);

        Point checkpoint = pointAddition(u1, u2, a, b,mod);
        System.out.println("checkpoint:" + displayPoint(checkpoint));

        System.out.println(checkpoint.getPointX() + " ?=" +r );

        if(checkpoint.getPointX().compareTo(r) == 0){

            System.out.println("signature is valid...");

        }
        else{

            System.out.println("invalid signature detected!!!");

        }

        Date verifyEnd = new Date();

        System.out.println("\nverification lasts "
                +(double)(verifyEnd.getTime() - verifyBegin.getTime())/1000+" seconds\n");

    }

    public static Point pointAddition(Point p, Point q, BigInteger a, BigInteger b, BigInteger mod) throws Exception
    {
        BigInteger x1 = p.getPointX();
        BigInteger y1 = p.getPointY();

        BigInteger x2 = q.getPointX();
        BigInteger y2 = q.getPointY();

        BigInteger beta;

        if(x1.compareTo(x2) == 0 && y1.compareTo(y2)==0)
        {
            beta = (BigInteger.valueOf(3).multiply(x1.multiply(x1)).add(a))
                    .multiply(multiplicativeInverse(BigInteger.valueOf(2).multiply(y1), mod));
        }
        else
        {
            beta = (y2.subtract(y1))
                    .multiply(multiplicativeInverse(x2.subtract(x1), mod));
        }

        BigInteger x3 = (beta.multiply(beta)).subtract(x1).subtract(x2);
        BigInteger y3 = (beta.multiply(x1.subtract(x3))).subtract(y1);

        while (x3.compareTo(BigInteger.valueOf(0))< 0)
        {
            BigInteger times = x3.abs().divide(mod).add(BigInteger.valueOf(1));
            x3 = x3.add(times.multiply(mod));
        }

        while (y3.compareTo(BigInteger.valueOf(0))<0)
        {
            BigInteger times = y3.abs().divide(mod).add(BigInteger.valueOf(1));
            y3 = y3.add(times.multiply(mod));
        }

        x3 = x3.remainder(mod);
        y3 = y3.remainder(mod);

        Point r = new Point();
        r.setPointX(x3);
        r.setPointY(y3);
        return r;
    }

    public static Point applyDoubleAndAddMethod(Point p, BigInteger k, BigInteger a, BigInteger b, BigInteger mod) throws Exception
    {
        Point tempPoint = new Point();
        tempPoint.setPointX(p.getPointX());
        tempPoint.setPointY(p.getPointY());

        String kAsBinary = k.toString(2);

        for(int i =1; i<kAsBinary.length(); i++)
        {
            int currentBit = Integer.parseInt(kAsBinary.substring(i, i+1));
            tempPoint = pointAddition(tempPoint,tempPoint, a, b, mod);
            if(currentBit == 1)
                tempPoint = pointAddition(tempPoint, p, a, b, mod);
        }

        return tempPoint;
    }



    public static BigInteger multiplicativeInverse(BigInteger a, BigInteger mod) {
        while (a.compareTo(new BigInteger("0")) == -1) {
            a = a.add(mod);
        }

        BigInteger x1 = new BigInteger("1");
        BigInteger x2 = new BigInteger("0");
        BigInteger x3 = mod;

        BigInteger y1 = new BigInteger("0");
        BigInteger y2 = new BigInteger("1");
        BigInteger y3 = a;

        BigInteger q = x3.divide(y3);

        BigInteger t1 = x1.subtract(q.multiply(y1));
        BigInteger t2 = x2.subtract(q.multiply(y2));
        BigInteger t3 = x3.subtract(q.multiply(y3));

        while (y3.compareTo(new BigInteger("1")) != 0) {
            x1 = y1;
            x2 = y2;
            x3 = y3;

            y1 = t1;
            y2 = t2;
            y3 = t3;

            q = x3.divide(y3);

            t1 = x1.subtract(q.multiply(y1));
            t2 = x2.subtract(q.multiply(y2));
            t3 = x3.subtract(q.multiply(y3));
        }

        while (y2.compareTo(new BigInteger("0")) == -1)
        {
            y2 = y2.add(mod);
        }

        return y2;
    }

    public static BigInteger generatePrimeModulo()
    {
        //Spec256k1
        //建议的256位椭圆曲线域系数   http://www.secg.org/sec2-v2.pdf
        //比特币的模
        //2^256 - 2^32 - 2^9 - 2^8 - 2^7 - 2^6 - 2^4 - 2^0
        BigInteger base = new BigInteger("2");
        BigInteger modulus = base.pow(256)
                .subtract(base.pow(32))
                .subtract(base.pow(9))
                .subtract(base.pow(8))
                .subtract(base.pow(7))
                .subtract(base.pow(6))
                .subtract(base.pow(4))
                .subtract(base.pow(0));

        return modulus;
    }

    public static String displayPoint(Point p)
    {
        return "(" + p.getPointX() +", " + p.getPointY()+")";
    }

    public static BigInteger sqrt(BigInteger n)
    {
        BigInteger a = BigInteger.ONE;
        BigInteger b = new BigInteger(n.shiftRight(5).add(new BigInteger("8")).toString());
        while (b.compareTo(a)>=0)
        {
            BigInteger mid = new BigInteger(a.add(b).shiftRight(1).toString());
            if(mid.multiply(mid).compareTo(n) > 0)
                b = mid.subtract(BigInteger.ONE);
            else
                a = mid.add(BigInteger.ONE);

        }

        return a.subtract(BigInteger.ONE);
    }

}
