package com.bookmap.api.simple.demo.utils.data;

import java.util.TreeMap;

public class OrderBookSum extends OrderBookBase {
    private final int levels;

    public OrderBookSum(int levels) {
        this.levels = levels;
    }

    public int getSizeSum(boolean isBid) {
        TreeMap<Integer, Integer> book = isBid ? bids : asks;
        int size = 0;
        if (!book.isEmpty()) {
            Integer price = book.firstKey();
            for (int i = 0; i < levels; i++) {
                Integer q = book.get(price);
                if (q != null) {
                    size += q;
                }
                price += isBid ? -1 : 1;
            }
        }
        return size;
    }
}
