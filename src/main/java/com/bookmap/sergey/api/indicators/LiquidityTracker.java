package com.bookmap.sergey.api.indicators;

import java.awt.Color;

import com.bookmap.sergey.api.utils.data.OrderBookSum;

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
import velox.api.layer1.simplified.InitialState;
import velox.api.layer1.simplified.Intervals;

@Layer1SimpleAttachable
@Layer1StrategyName("Liquidity Tracker")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class LiquidityTracker implements CustomModule, DepthDataListener, BarDataListener {

    protected Indicator indicatorBid;
    protected Indicator indicatorAsk;
    protected final int levels = 10; // number of price levels
    protected OrderBookSum orderBook = new OrderBookSum(levels);

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        registerIndicators(api);
    }

    protected void registerIndicators(Api api) {
        indicatorBid = api.registerIndicator("Liquidity Bid", GraphType.BOTTOM, Color.GREEN);
        indicatorAsk = api.registerIndicator("Liquidity Ask", GraphType.BOTTOM, Color.RED);
    }

    @Override
    public void onDepth(boolean isBid, int price, int size) {
        orderBook.onDepth(isBid, price, size);
    }

    @Override
    public long getBarInterval() {
        return Intervals.INTERVAL_100_MILLISECONDS;
    }

    @Override
    public void onBar(OrderBook orderBook, Bar bar) {
        onBarTemp();
    }

    protected void onBarTemp() {
        int bidSize = orderBook.getSizeSum(true);
        indicatorBid.addPoint(bidSize);
        int askSize = orderBook.getSizeSum(false);
        indicatorAsk.addPoint(askSize);
    }

    @Override
    public void stop() {
    }
}