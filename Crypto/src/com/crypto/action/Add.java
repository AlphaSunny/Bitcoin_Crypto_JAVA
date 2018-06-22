package com.crypto.action;

import com.crypto.entity.Point;

import java.math.BigDecimal;
import java.math.MathContext;

public class Add {
    public static void main(String[] args)
    {
        double a=-4, b=4;
        Point p = new Point();
        p.setPointX(BigDecimal.valueOf(-2));
        p.setPointY(BigDecimal.valueOf(-2));

        /*
        // doubling
        Point point2p = pointAddition(p, p, a, b);
        System.out.println(displayPoint(point2p));

        // point addition
        Point point3p = pointAddition(point2p, p, a, b);
        Point point4p = pointAddition(point3p, p, a, b);
        System.out.println(displayPoint(point3p));
        System.out.println(displayPoint(point4p));
        */

        //------------------------------------------------------
        Point newpoint = pointAddition(p, p, a, b);
        for(int i =3; i<=100; i++)
        {
            //System.out.print(i+"P:");
            newpoint = pointAddition(newpoint, p, a, b);
        }

        System.out.println(displayPoint(newpoint));

    }

    public static Point pointAddition(Point p, Point q, double a, double b)
    {
        int scale = 10;
        MathContext mc = new MathContext(128);
        BigDecimal x1 = p.getPointX();
        BigDecimal y1 = p.getPointY();

        BigDecimal x2 = q.getPointX();
        BigDecimal y2 = q.getPointY();

        BigDecimal beta;

        if(x1.compareTo(x2) == 0 && y1.compareTo(y2) == 0)
        {
            beta = (BigDecimal.valueOf(3).multiply(x1.multiply(x1))
                        .add(BigDecimal.valueOf(a)))
                        .divide((BigDecimal.valueOf(2).multiply(y1)), mc);
        }
        else
        {
            beta = (y2.subtract(y1)).divide((x2.subtract(x1)), mc);
        }

        BigDecimal x3 = (beta.multiply(beta)).subtract(x1).subtract(x2);
        BigDecimal y3 = (beta.multiply(x1.subtract(x3))).subtract(y1);

        Point  r = new Point();
        r.setPointX(x3);
        r.setPointY(y3);

        //System.out.println("("+x3.setScale(scale, BigDecimal.ROUND_HALF_UP)+", "+y3.setScale(scale, BigDecimal.ROUND_HALF_UP)+")");
        return r;
    }

    public static String displayPoint(Point p) {
        int scale = 20;
        return "(" + p.getPointX().setScale(scale, BigDecimal.ROUND_HALF_UP) +
                        ", " + p.getPointY().setScale(scale, BigDecimal.ROUND_HALF_UP) + ")";
    }
}
