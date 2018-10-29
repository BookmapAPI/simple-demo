package com.bookmap.api.simple.demo.utils.data;

public class MovingAverage {
    private long n;
    private long counter = 0;
    private double value;

    public MovingAverage(long n) {
        this.n = n;
    }

    public double update(double x) {
        long k = Math.min(n, ++counter);
        value = ((k - 1) * value + x) / k;
        return value;
    }
    
    public void updatePeriod(long newN) {
        counter = Math.min(counter,  n);
        n = newN;
    }
}
