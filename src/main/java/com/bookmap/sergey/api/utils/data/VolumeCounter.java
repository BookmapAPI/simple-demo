package com.bookmap.sergey.api.utils.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class VolumeCounter {

    public enum VolumeCounterType {
        MOVING_SUM, EXPONENTIAL, CUMULATIVE;
        public static String[] names() {
            return Stream.of(values()).map(Object::toString).toArray(String[]::new);
        }
    }

    private interface IVolumeCounter {
        public void onTrade(long nanoseconds, int size);

        public double getVolume(long nanoseconds);
    }

    static class VolumeMovingSum implements IVolumeCounter {

        protected final long intervalNanoseconds;
        private ArrayList<Pair<Long, Integer>> trades = new ArrayList<>();
        private long volume = 0;

        public VolumeMovingSum(long intervalNanoseconds) {
            this.intervalNanoseconds = intervalNanoseconds;
        }

        public void onTrade(long nanoseconds, int size) {
            trades.add(new MutablePair<Long, Integer>(nanoseconds, size));
            volume += size;
        }

        public double getVolume(long nanoseconds) {
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

    static class VolumeExponential implements IVolumeCounter {
        private double volume = 0;
        private final double factor;
        private Long nanosecondsPrev = null;

        public VolumeExponential(long intervalNanoseconds) {
            factor = -Math.log(2) / intervalNanoseconds;
        }

        @Override
        public void onTrade(long nanoseconds, int size) {
            volume = getVolumeRaw(nanoseconds);
            volume += size;
            nanosecondsPrev = nanoseconds;
        }

        @Override
        public double getVolume(long nanoseconds) {
            return Math.log(2) * getVolumeRaw(nanoseconds);
        }

        private double getVolumeRaw(long nanoseconds) {
            if (nanosecondsPrev == null) {
                nanosecondsPrev = nanoseconds;
            }
            long dt = nanoseconds > nanosecondsPrev ? nanoseconds - nanosecondsPrev : 0;
            return volume * Math.exp(factor * dt);
        }
    }

    static class VolumeCumulative implements IVolumeCounter {
        private final long resetNanoseconds;
        private Long nextReset = null;
        private long volume = 0;

        VolumeCumulative(long resetNanoseconds) {
            this.resetNanoseconds = resetNanoseconds;
        }

        @Override
        public void onTrade(long nanoseconds, int size) {
            checkReset(nanoseconds);
            volume += size;
        }

        @Override
        public double getVolume(long nanoseconds) {
            checkReset(nanoseconds);
            return volume == 0 ? Double.NaN : volume;
        }

        private void checkReset(long nanoseconds) {
            if (nextReset == null) {
                nextReset = resetNanoseconds * (1 + nanoseconds / resetNanoseconds);
            } else if (nanoseconds > nextReset) {
                nextReset += resetNanoseconds;
                volume = 0;
            }
        }
    }

    private IVolumeCounter volumeBuy;
    private IVolumeCounter volumeSell;

    public VolumeCounter(long intervalNanoSeconds, VolumeCounterType type) {
        volumeBuy = buildVolumeCounter(intervalNanoSeconds, type);
        volumeSell = buildVolumeCounter(intervalNanoSeconds, type);
    }

    public void onTrade(long nanoseconds, boolean isBuy, int size) {
        if (isBuy) {
            volumeBuy.onTrade(nanoseconds, size);
        } else {
            volumeSell.onTrade(nanoseconds, size);
        }
    }

    public double getVolume(long nanoseconds, boolean isBid) {
        return isBid ? volumeBuy.getVolume(nanoseconds) : volumeSell.getVolume(nanoseconds);
    }

    private IVolumeCounter buildVolumeCounter(long intervalNanoSeconds, VolumeCounterType type) {
        if (type == VolumeCounterType.MOVING_SUM) {
            return new VolumeMovingSum(intervalNanoSeconds);
        } else if (type == VolumeCounterType.EXPONENTIAL) {
            return new VolumeExponential(intervalNanoSeconds);
        } else {
            return new VolumeCumulative(intervalNanoSeconds);
        }
    }
}
