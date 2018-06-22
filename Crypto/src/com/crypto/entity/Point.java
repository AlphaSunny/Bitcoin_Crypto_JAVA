package com.crypto.entity;

import java.math.BigDecimal;

public class Point {
    BigDecimal pointX;
    BigDecimal pointY;

    public Point(BigDecimal pointX, BigDecimal pointY) {
        this.pointX = pointX;
        this.pointY = pointY;
    }

    public Point(){

    }

    public BigDecimal getPointX() {
        return pointX;
    }

    public void setPointX(BigDecimal pointX) {
        this.pointX = pointX;
    }

    public BigDecimal getPointY() {
        return pointY;
    }

    public void setPointY(BigDecimal pointY) {
        this.pointY = pointY;
    }
}
