package com.bookmap.api.simple.demo.utils.data;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class OrderBookBase {

    public TreeMap<Integer, Integer> bids = new TreeMap<>(Collections.reverseOrder());
    public TreeMap<Integer, Integer> asks = new TreeMap<>();

    public int onDepth(boolean isBid, int price, int size) {
        Map<Integer, Integer> book = isBid ? bids : asks;
        Integer sizePrevious = (size == 0) ? book.remove(price) : book.put(price, size);
        return (sizePrevious == null) ? 0 : sizePrevious;
    }
}
