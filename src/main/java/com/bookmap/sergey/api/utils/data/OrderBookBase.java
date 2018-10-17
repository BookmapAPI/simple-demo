package com.bookmap.sergey.api.utils.data;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import velox.api.layer1.simplified.DepthDataListener;

public class OrderBookBase implements DepthDataListener {

    public TreeMap<Integer, Integer> bids = new TreeMap<>(Collections.reverseOrder());
    public TreeMap<Integer, Integer> asks = new TreeMap<>();

    public void onDepth(boolean isBid, int price, int size) {
        Map<Integer, Integer> book = isBid ? bids : asks;
        if (size == 0)
            book.remove(price);
        else
            book.put(price, size);
    }

    public int getSizeOrZero(boolean isBid, int price) {
        Integer size = (isBid ? bids : asks).get(price);
        return size == null ? 0 : size;
    }
}
