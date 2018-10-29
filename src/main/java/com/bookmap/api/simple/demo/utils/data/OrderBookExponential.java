package com.bookmap.api.simple.demo.utils.data;

import java.util.TreeMap;

public class OrderBookExponential extends OrderBookSum {
    private double halfLifeLevelFactor;
    private double bidSizeWeighted = 0.0;
    private double askSizeWeighted = 0.0;

    public OrderBookExponential(int levels) {
        super(levels);
        this.halfLifeLevelFactor = Math.log(2) / levels;
    }

    @Override
    public int getSizeSum(boolean isBid) {
        return (int) Math.round(Math.log(2) * (isBid ? bidSizeWeighted : askSizeWeighted));
    }

    @Override
    public int onDepth(boolean isBid, int price, int size) {
        int priceLevel = calcPriceLevel(isBid, price);
        int prevSize = super.onDepth(isBid, price, size);
        double value = isBid ? bidSizeWeighted : askSizeWeighted;
        if (priceLevel < 0) { // best price improved
            value *= Math.exp(halfLifeLevelFactor * priceLevel);
            value += size;
        } else if (priceLevel == 0 && size == 0) { // best price deleted
            priceLevel = calcPriceLevel(isBid, price); // must be negative
            value -= prevSize;
            value *= Math.exp(-halfLifeLevelFactor * priceLevel);
        } else {
            value += (size - prevSize) * Math.exp(-halfLifeLevelFactor * priceLevel);
        }
        if (isBid) {
            bidSizeWeighted = value;
        } else {
            askSizeWeighted = value;
        }
        return prevSize;
    }

    private int calcPriceLevel(boolean isBid, int price) {
        TreeMap<Integer, Integer> book = isBid ? bids : asks;
        return book.isEmpty() ? 0 : (isBid ? book.firstKey() - price : price - book.firstKey());
    }
}
