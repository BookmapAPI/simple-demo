package com.bookmap.sergey.custommodules.utils;

import java.util.TreeMap;

public class OrderBookExponential extends OrderBookBase {
	private double halfLifeLevelAdj;
	private double bidSizeWeighted = 0.0;
	private double askSizeWeighted = 0.0;

	public OrderBookExponential(double halfLifeLevel) {
		this.halfLifeLevelAdj = halfLifeLevel / Math.log(2);
	}

	public double getExponentiallyWeightedAverage(boolean isBid) {
		return (isBid ? bidSizeWeighted : askSizeWeighted) / halfLifeLevelAdj;
	}

	public void onDepth(boolean isBid, int price, int size) {
		TreeMap<Integer, Integer> levels = isBid ? bids : asks;
		Integer prevSize = levels.get(price);
		if (prevSize == null) {
			prevSize = 0;
		}
		int sizeDiff = size - prevSize;
		int bestPrice = levels.isEmpty() ? price : levels.firstKey();
		int priceLevel = isBid ? bestPrice - price : price - bestPrice;

		super.onDepth(isBid, price, size);
		
		double value = isBid ? bidSizeWeighted : askSizeWeighted;
		if (priceLevel < 0) { // price improvement
			value *= Math.exp(priceLevel / halfLifeLevelAdj);
			value += size;
		} else if (priceLevel == 0 && size == 0) { // best price deleted
			value -= prevSize;
			bestPrice = levels.isEmpty() ? price : levels.firstKey();
			priceLevel = isBid ? bestPrice - price : price - bestPrice;
			value *= Math.exp(-priceLevel / halfLifeLevelAdj);
		} else { // best price didn't change
			value += sizeDiff * Math.exp(-priceLevel / halfLifeLevelAdj);
		}
		if (isBid) {
			bidSizeWeighted = value;
		} else {
			askSizeWeighted = value;
		}
	}
}
