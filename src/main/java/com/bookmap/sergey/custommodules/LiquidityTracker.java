package com.bookmap.sergey.custommodules;

import java.awt.Color;

import com.bookmap.sergey.custommodules.utils.OrderBookExponential;

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
import velox.api.layer1.simplified.DepthDataListener;
import velox.api.layer1.simplified.Indicator;
import velox.api.layer1.simplified.Intervals;

@Layer1SimpleAttachable
@Layer1StrategyName("Liquidity Tracker")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class LiquidityTracker implements CustomModule, DepthDataListener, BarDataListener {

    protected final int nLevels = 5;
    protected final OrderBookExponential book = new OrderBookExponential(nLevels);
    private long barInterval = Intervals.INTERVAL_100_MILLISECONDS;

    protected Indicator line1;
    protected Indicator line2;

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api) {
        initLines(api);
    }

    protected void initLines(Api api) {
        line1 = api.registerIndicator("Bid", GraphType.BOTTOM, Color.GREEN);
        line2 = api.registerIndicator("Ask", GraphType.BOTTOM, Color.RED);
    }

    @Override
    public void onBar(OrderBook orderBook, Bar bar) {
        onUpdate();
    }

    @Override
    public void onDepth(boolean isBid, int price, int size) {
        book.onDepth(isBid, price, size);
    }

    protected void onUpdate() {
        int bidSize = (int) Math.round(book.getExponentiallyWeightedAverage(true));
        line1.addPoint(bidSize);
        int askSize = (int) Math.round(book.getExponentiallyWeightedAverage(false));
        line2.addPoint(askSize);
    }

    @Override
    public long getBarInterval() {
        return barInterval;
    }

    @Override
    public void stop() {
    }
}
