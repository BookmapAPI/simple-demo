package com.bookmap.api.simple.demo.utils.data;

public class ExponentialAverage {
    private double value = 0;
    private Long nanosecondsPrev = null;
    private final double halfLifeFactor;

    /**
     * Computes moving average of inputs "x" which gives exponentially decaying
     * weights for previous values so that inputs received halfLife time ago have
     * weight of 0.5
     * 
     * @param halfLifeNanoseconds
     */
    public ExponentialAverage(long halfLifeNanoseconds) {
        this.halfLifeFactor = -Math.log(2) / halfLifeNanoseconds;
    }

    public double onUpdate(long nanoseconds, double x) {
        if (nanosecondsPrev == null) {
            nanosecondsPrev = nanoseconds;
            value = x;
        }
        if (nanoseconds < nanosecondsPrev) {
            nanosecondsPrev = nanoseconds;
        }
        long dt = nanoseconds - nanosecondsPrev;
        double w = Math.exp(dt * halfLifeFactor);
        value = w * value + (1 - w) * x;
        return value;
    }

    public double getValue(long nanoseconds) {
        return nanosecondsPrev == null ? Double.NaN : value;
    }
}
