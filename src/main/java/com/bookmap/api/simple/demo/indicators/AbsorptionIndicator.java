package com.bookmap.api.simple.demo.indicators;

import java.awt.Color;

import com.bookmap.api.simple.demo.utils.data.ExponentialSumBars;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.layers.utils.OrderBook;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.Bar;
import velox.api.layer1.simplified.BarDataListener;
import velox.api.layer1.simplified.CustomModule;
import velox.api.layer1.simplified.Indicator;
import velox.api.layer1.simplified.InitialState;
import velox.api.layer1.simplified.Intervals;

@Layer1SimpleAttachable
@Layer1StrategyName("Absorption")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class AbsorptionIndicator implements CustomModule, BarDataListener {

    private int longWindowNumBars = 300;
    private int shortWindowNumBars = 20;
    private double factor = longWindowNumBars / (double) shortWindowNumBars;
    private Indicator line1;
    private long barInterval = Intervals.INTERVAL_100_MILLISECONDS;
    private ExponentialSumBars volumeLong = new ExponentialSumBars(longWindowNumBars);
    private ExponentialSumBars volumeShort = new ExponentialSumBars(shortWindowNumBars);
    private ExponentialSumBars trendLong = new ExponentialSumBars(longWindowNumBars);
    private ExponentialSumBars trendShort = new ExponentialSumBars(shortWindowNumBars);

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        line1 = api.registerIndicator("Absorption", GraphType.BOTTOM, 0);
        line1.setColor(Color.PINK);
    }

    @Override
    public void onBar(OrderBook orderBook, Bar bar) {
        double trend = bar.getClose() - bar.getOpen();
        if (Double.isNaN(trend)) {
            trend = 0;
        }
        double volume = bar.getVolumeTotal();
        trendShort.onBar(trend);
        trendLong.onBar(trend);
        volumeShort.onBar(volume);
        volumeLong.onBar(volume);
        double p = factor * trendShort.getValue() - trendLong.getValue();
        double v = factor * volumeShort.getValue() / volumeLong.getValue();
        double absorption = Double.isNaN(v) ? 0 : p * v;
        line1.addPoint(absorption);
    }

    @Override
    public void stop() {
    }

    @Override
    public long getInterval() {
        return barInterval;
    }

}
