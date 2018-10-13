package com.bookmap.sergey.custommodules.utils;

public class Ema {
    private double value = 0;
    private Long nanosecondsPrev = null;
    private final double halfLifeFactor;

    public Ema(long halfLifeNanoseconds) {
        this.halfLifeFactor = -Math.log(2) / halfLifeNanoseconds;
    }

    public void onUpdate(long nanoseconds, double x) {
        if (nanosecondsPrev == null) {
            nanosecondsPrev = nanoseconds;
        }
        value = getValue(nanoseconds);
        value += x;
        nanosecondsPrev = nanoseconds;
    }

    public double getValue(long nanoseconds) {
        long dt = nanoseconds - nanosecondsPrev;
        return value * Math.exp(dt * halfLifeFactor);
    }
}
