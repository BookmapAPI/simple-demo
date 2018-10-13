package com.bookmap.sergey.custommodules.utils;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class SlidingVolume {

	private final long intervalNanoseconds;
	private ArrayList<Pair<Long, Integer>> trades = new ArrayList<>();
	private int volume = 0;

	public SlidingVolume(long intervalNanoseconds) {
		this.intervalNanoseconds = intervalNanoseconds;
	}

	public void onTrade(long nanoseconds, int size) {
		trades.add(new MutablePair<Long, Integer>(nanoseconds, size));
		volume += size;
	}

	public int getVolume(long nanoseconds) {
		Iterator<Pair<Long, Integer>> iterator = trades.iterator();
		while (iterator.hasNext()) {
			Pair<Long, Integer> pair = iterator.next();
			if (pair.getKey() < nanoseconds - intervalNanoseconds) {
				iterator.remove();
				volume -= pair.getValue();
			} else {
				break;
			}
		}
		return volume;
	}
}
