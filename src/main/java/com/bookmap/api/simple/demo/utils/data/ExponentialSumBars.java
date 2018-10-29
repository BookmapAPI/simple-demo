package com.bookmap.api.simple.demo.utils.data;

public class ExponentialSumBars extends ExponentialSum {

    long counter = 0;

    /**
     * Same as ExponentialSum, but decays as number of bars. Expects onBar() to be
     * called every Bar period
     * 
     * @param halfLife
     */
    public ExponentialSumBars(int halfLife) {
        super(halfLife);
    }

    public void onBar(double x) {
        onUpdate(counter, x);
        counter++;
    }

    public double getValue() {
        return getValue(counter);
    }

    public long getValueLong() {
        return getValueLong(counter);
    }
}
