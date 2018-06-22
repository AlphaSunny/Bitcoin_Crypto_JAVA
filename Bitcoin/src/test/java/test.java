import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.Security;

import org.bitcoinj.core.Base58;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.crypto.entity.Point;

public class test {

    public static void main(String[] args) throws Exception {

        BigInteger base = new BigInteger("2");

        BigInteger mod = base.pow(256)
                .subtract(base.pow(32))
                .subtract(base.pow(9))
                .subtract(base.pow(8))
                .subtract(base.pow(7))
                .subtract(base.pow(6))
                .subtract(base.pow(4))
                .subtract(base.pow(0));

        BigInteger order = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);

        //curve equation: y^2 = x^3 + ax + b -> current curve: y^2 = x^3 + 7
        BigInteger a = new BigInteger("0");
        BigInteger b = new BigInteger("7");

        System.out.println("Curve: y^2 = x^3 + "+a+"*x + "+b);

        Point basePoint = new Point();
        basePoint.setPointX(new BigInteger("55066263022277343669578718895168534326250603453777594175500187360389116729240"));
        basePoint.setPointY(new BigInteger("32670510020758816978083085130507043184471273380659243275938904335757337482424"));

        System.out.println("base point: ("+basePoint.getPointX()+", "+basePoint.getPointY()+")\n");

        System.out.println("modulo: "+mod);
        System.out.println("order of group: "+order+"\n");

        System.out.println("--------------------------------");
        System.out.println("Bitcoin Address Generator");
        System.out.println("--------------------------------\n");

        //BigInteger privateKey = new BigInteger("11253563012059685825953619222107823549092147699031672238385790369351542642469");

        //BigInteger privateKey = new BigInteger("18E14A7B6A307F426A94F8114701E7C8E774E7F9A47E2C2035DB29A206321725", 16);
        //BigInteger privateKey = new BigInteger("945FB84E92575908A000D9D4A0B136EB0B69F8B5BCDAF157A3A91B7C83A02B1B", 16);
        BigInteger privateKey = new BigInteger("81BFAEB343C4D0138658C4D9C46822A4E27DE8291C7B657232681FA904FF1C58",16);

        System.out.println("private key (hex): "+privateKey.toString(16)+" ("+privateKey.toString(16).length()*4+" bits)\n");

        Point publicKey = applyDoubleAndAddMethod(basePoint, privateKey, a, b, mod);

        System.out.println("public key: ("+publicKey.getPointX()+", "+publicKey.getPointY()+")");

        String publicKeyString = "04"+publicKey.getPointX().toString(16)+publicKey.getPointY().toString(16);

        BigInteger publicKeyMerged = new BigInteger(publicKeyString, 16);

        System.out.println("public key (merged): "+publicKeyString+" ("+publicKeyString.length()*4+" bits)\n");

        System.out.println("--------------------------------\n");

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(publicKeyMerged.toByteArray());
        byte[] sha256HashByte = md.digest();

        BigInteger hash = new BigInteger(sha256HashByte).abs();

        System.out.println("sha-256 applied hash: "+hash.toString(16)+" ("+hash.toString(16).length()*4+" bits)");

        //-------------------------------------------------------

        Security.addProvider(new BouncyCastleProvider());

        md = MessageDigest.getInstance("RIPEMD160");
        md.update(sha256HashByte);
        byte[] ripemd256HashByte = md.digest();

        BigInteger ripemdHash = new BigInteger(1, ripemd256HashByte);
        String ripemdHashHex = ripemdHash.toString(16);

        while(ripemdHashHex.length()<40){

            ripemdHashHex = "0"+ripemdHashHex;

        }

        System.out.println("ripemd160 applied hash: "+ripemdHashHex+" ("+ripemdHashHex.length()*4+" bits)");

        //-------------------------------------------------------

        ripemdHashHex = "00"+ripemdHashHex;

        System.out.println("adding network bytes to ripemd160 hash - extended ripemd160: "+ripemdHashHex+"\n");

        //-------------------------------------------------------

        md = MessageDigest.getInstance("SHA-256");
        md.update(hexStringToByte(ripemdHashHex, 21));
        byte[] checking = md.digest();

        System.out.println("sha-256 to extended ripemd160: "+new BigInteger(checking).toString(16)+" ("+new BigInteger(checking).toString(16).length()*4+" bits)");

        //-------------------------------------------------------

        md = MessageDigest.getInstance("SHA-256");
        md.update(checking);
        checking = md.digest();

        BigInteger checkingInt = new BigInteger(1, checking); // use this 1 to tell it is positive (https://stackoverflow.com/questions/6357234/sha-hash-function-gives-a-negative-output)

        System.out.println("second time sha-256 applied to extended ripe160: "+checkingInt.toString(16)+" ("+checkingInt.toString(16).length()*4+" bits)");

        String checksum = checkingInt.toString(16).substring(0,8);

        System.out.println("checksum: "+checksum+"\n");

        //-------------------------------------------

        //add checksum to network bytes added RIPEMD160 hash

        String address = ripemdHashHex+checksum;

        System.out.println("adding checksum to extended ripemd160 "+address+" ("+address.length()*4+" bits)");

        //-------------------------------------------

        String base58Address = Base58.encode(new BigInteger(address, 16).toByteArray());

        while(base58Address.length() < 34){

            base58Address = "1"+base58Address;

        }

        System.out.println("base 58 bitcoin address: "+base58Address+" ("+base58Address.length()*4+" bits)");

    }

    public static byte[] hexStringToByte(String hexString, int desiredLength){

        byte[] byteTransformation = new BigInteger(hexString, 16).toByteArray();

        if(byteTransformation.length < desiredLength){

            byte[] paddingApplied = new byte[desiredLength];

            //initialize
            for(int i=0;i<desiredLength;i++){

                paddingApplied[i] = 0;

            }

            //transfer

            for(int i=0;i<byteTransformation.length;i++){

                paddingApplied[i + (desiredLength - byteTransformation.length)] = byteTransformation[i];

            }

            return paddingApplied;

        }

        return byteTransformation;

    }

    public static Point pointAddition(Point P, Point Q, BigInteger a, BigInteger b, BigInteger mod) throws Exception {

        BigInteger x1 = P.getPointX();
        BigInteger y1 = P.getPointY();

        BigInteger x2 = Q.getPointX();
        BigInteger y2 = Q.getPointY();

        BigInteger beta;

        if(x1.compareTo(x2) == 0 && y1.compareTo(y2) == 0) {

            //apply doubling

            beta = (BigInteger.valueOf(3).multiply(x1.multiply(x1)).add(a))
                    .multiply(multiplicativeInverse(BigInteger.valueOf(2).multiply(y1), mod));
        }
        else {

            //apply point addition

            beta = (y2.subtract(y1))
                    .multiply(multiplicativeInverse(x2.subtract(x1), mod));

        }

        BigInteger x3 = (beta.multiply(beta)).subtract(x1).subtract(x2);
        BigInteger y3 = (beta.multiply(x1.subtract(x3))).subtract(y1);

        while(x3.compareTo(BigInteger.valueOf(0)) < 0) {

            BigInteger times = x3.abs().divide(mod).add(BigInteger.valueOf(1));

            //x3 = x3.add(mod);
            x3 = x3.add(times.multiply(mod));

        }

        while(y3.compareTo(BigInteger.valueOf(0)) < 0) {

            BigInteger times = y3.abs().divide(mod).add(BigInteger.valueOf(1));

            //y3 = y3.add(mode);
            y3 = y3.add(times.multiply(mod));

        }

        x3 = x3.remainder(mod);
        y3 = y3.remainder(mod);

        Point R = new Point();
        R.setPointX(x3);
        R.setPointY(y3);

        return R;

    }

    public static Point applyDoubleAndAddMethod(Point P, BigInteger k, BigInteger a, BigInteger b, BigInteger mod) throws Exception {

        Point tempPoint = new Point();
        tempPoint.setPointX(P.getPointX());
        tempPoint.setPointY(P.getPointY());

        String kAsBinary = k.toString(2); //convert to binary

        //System.out.println("("+k+")10 = ("+kAsBinary+")2");

        for(int i=1;i<kAsBinary.length();i++) {

            int currentBit = Integer.parseInt(kAsBinary.substring(i, i+1));

            tempPoint = pointAddition(tempPoint, tempPoint, a, b, mod);

            if(currentBit == 1) {

                tempPoint = pointAddition(tempPoint, P, a, b, mod);

            }

        }

        return tempPoint;

    }

    public static BigInteger multiplicativeInverse(BigInteger a, BigInteger mod) throws Exception {

        //return a.modInverse(mod); //out-of-the-box function

        //extended euclidean algorithm to find modular inverse

        while(a.compareTo(new BigInteger("0")) == -1){

            a = a.add(mod);

        }

        BigInteger x1 = new BigInteger("1");
        BigInteger x2 = new BigInteger("0");
        BigInteger x3 = mod;

        BigInteger y1 = new BigInteger("0");;
        BigInteger y2 = new BigInteger("1");;
        BigInteger y3 = a;

        BigInteger q = x3.divide(y3);

        BigInteger t1 = x1.subtract(q.multiply(y1));
        BigInteger t2 = x2.subtract(q.multiply(y2));
        BigInteger t3 = x3.subtract(q.multiply(y3));

        while(y3.compareTo(new BigInteger("1")) != 0){

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

        while(y2.compareTo(new BigInteger("0")) == -1){

            y2 = y2.add(mod);

        }

        return y2;

    }

    public static BigInteger generatePrimeModulo(){

        //Secp256k1
        //Recommended 256-bit Elliptic Curve Domain Parameters over Fp (http://www.secg.org/sec2-v2.pdf)
        //modulo for bitcoin
        //2^256 - 2^32 - 2^9 - 2^8 - 2^7 - 2^6 - 2^4 - 2^0

        BigInteger base = new BigInteger("2");

        BigInteger modulus =  base.pow(256)
                .subtract(base.pow(32))
                .subtract(base.pow(9))
                .subtract(base.pow(8))
                .subtract(base.pow(7))
                .subtract(base.pow(6))
                .subtract(base.pow(4))
                .subtract(base.pow(0));

        return modulus;

    }

    public static String displayPoint(Point P) {

        return "("+P.getPointX()+", "+P.getPointY()+")";

    }

    public static BigInteger sqrt(BigInteger n) {

        BigInteger a = BigInteger.ONE;
        BigInteger b = new BigInteger(n.shiftRight(5).add(new BigInteger("8")).toString());

        while(b.compareTo(a) >= 0) {

            BigInteger mid = new BigInteger(a.add(b).shiftRight(1).toString());

            if(mid.multiply(mid).compareTo(n) > 0) b = mid.subtract(BigInteger.ONE);

            else a = mid.add(BigInteger.ONE);

        }

        return a.subtract(BigInteger.ONE);
    }

}