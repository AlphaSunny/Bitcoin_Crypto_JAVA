package com.crypto.action;

import com.crypto.entity.Point;

import java.math.BigDecimal;
import java.math.BigInteger;


import static com.crypto.action.Add.pointAddition;

public class DoubleAndAdd {
    public static void main(String[] args)
    {
        int scale = 10;
        double a = -4, b = 4;
        Point p = new Point();
        p.setPointX(BigDecimal.valueOf(-2));
        p.setPointY(BigDecimal.valueOf(-2));
        BigInteger k = new BigInteger("100");
        Point tempPoint = new Point();
        tempPoint = applyDoubleAndAddMethod(p, k, a, b);


        System.out.println("("+tempPoint.getPointX().setScale(scale, BigDecimal.ROUND_HALF_UP)+", "+tempPoint.getPointY().setScale(scale, BigDecimal.ROUND_HALF_UP)+")");



    }

    public static Point applyDoubleAndAddMethod(Point p, BigInteger k, double a, double b) {
        Point tempPoint = new Point();
        tempPoint.setPointX(p.getPointX());
        tempPoint.setPointY(p.getPointY());

        String kAsBinary = k.toString(2);
        for (int i = 1; i < kAsBinary.length(); i++) {
            int currentBit = Integer.parseInt(kAsBinary.substring(i, i + 1));

            tempPoint = pointAddition(tempPoint, tempPoint, a, b);

            if (currentBit == 1)
            {
                tempPoint = pointAddition(tempPoint, p, a, b);
            }
        }
        return tempPoint;
    }


}

