package com.bookmap.sergey.custommodules.utils;

public class SlidingVolumeHandler {

	private final SlidingVolume volumeBuy;
	private final SlidingVolume volumeSell;

	public SlidingVolumeHandler(long intervalNanoseconds) {
		volumeBuy = new SlidingVolume(intervalNanoseconds);
		volumeSell = new SlidingVolume(intervalNanoseconds);
	}

	public void onTrade(long nanoseconds, boolean isBuy, int size) {
		if (isBuy) {
			volumeBuy.onTrade(nanoseconds, size);
		} else {
			volumeSell.onTrade(nanoseconds, size);
		}
	}

	public int getVolume(long nanoseconds, boolean isBid) {
		return isBid ? volumeBuy.getVolume(nanoseconds) : volumeSell.getVolume(nanoseconds);
	}
}
