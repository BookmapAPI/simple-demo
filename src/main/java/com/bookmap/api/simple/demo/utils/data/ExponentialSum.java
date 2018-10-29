package com.bookmap.api.simple.demo.utils.data;

public class ExponentialSum {
    private double value = 0;
    private Long nanosecondsPrev = null;
    private final double halfLifeFactor;

    /**
     * Computes moving sum of inputs "x" which exponentially decays with time so
     * that inputs received halfLife time ago have weight of 0.5
     * 
     * @param halfLifeNanoseconds
     */
    public ExponentialSum(long halfLifeNanoseconds) {
        this.halfLifeFactor = -Math.log(2) / halfLifeNanoseconds;
    }

    public void onUpdate(long nanoseconds, double x) {
        value = getRawValue(nanoseconds);
        value += x;
        nanosecondsPrev = nanoseconds;
    }

    public double getValue(long nanoseconds) {
        return Math.log(2) * getRawValue(nanoseconds);
    }

    public long getValueLong(long nanoseconds) {
        return Math.round(getValue(nanoseconds));
    }

    private double getRawValue(long nanoseconds) {
        if (nanosecondsPrev == null) {
            nanosecondsPrev = nanoseconds;
        }
        if (nanoseconds < nanosecondsPrev) {
            nanosecondsPrev = nanoseconds;
        }
        long dt = nanoseconds - nanosecondsPrev;
        return value * Math.exp(dt * halfLifeFactor);
    }
}
