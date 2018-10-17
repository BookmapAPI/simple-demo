package com.bookmap.sergey.api.utils.data;

public class EmaBars {
    private double value = 0;
    private final double halfLifeBarsFactor;

    public EmaBars(double halfLife) {
        this.halfLifeBarsFactor = -Math.log(2) / halfLife;
    }

    public void onBar(double x) {
        value *= Math.exp(halfLifeBarsFactor);
        value += x;
    }

    public double getValue() {
        return value;
    }

    public long getValueLong() {
        return Math.round(value);
    }
}
