package com.bookmap.sergey.custommodules.utils;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class OrderBookBase {

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
	
	public int getBookSizeSum(boolean isBid, int nLevels) {
		TreeMap<Integer, Integer> levels = isBid ? bids : asks;
		Integer price = levels.firstKey();
		int size = 0;
		int n = Math.min(nLevels, levels.size());
		for (int i = 0; i < n; i++) {
			Integer q = levels.get(price);
			if (q != null) {
				size += q;
			}
			price += isBid ? -1 : 1;
		}
		return size;
	}
}
