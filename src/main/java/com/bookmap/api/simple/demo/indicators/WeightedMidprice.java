package com.bookmap.api.simple.demo.indicators;

import java.awt.Color;

import com.bookmap.api.simple.demo.utils.data.OrderBookBase;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.CustomModule;
import velox.api.layer1.simplified.DepthDataListener;
import velox.api.layer1.simplified.Indicator;
import velox.api.layer1.simplified.InitialState;
import velox.api.layer1.simplified.SnapshotEndListener;
import velox.api.layer1.simplified.TradeDataListener;

@Layer1SimpleAttachable
@Layer1StrategyName("Weighted Midprice")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class WeightedMidprice implements CustomModule, TradeDataListener, DepthDataListener, SnapshotEndListener {

    protected Indicator indicator;
    private OrderBookBase orderBook = new OrderBookBase();
    protected InitialState initialState;

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        indicator = api.registerIndicator(getLineTitle(), GraphType.PRIMARY);
        indicator.setColor(Color.WHITE);
        this.initialState = initialState;
    }

    @Override
    public void onDepth(boolean isBid, int price, int size) {
        orderBook.onDepth(isBid, price, size);
    }

    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        double bestBid = (double) orderBook.bids.firstKey();
        int bestBidSize = orderBook.bids.firstEntry().getValue();
        double bestAsk = (double) orderBook.asks.firstKey();
        int bestAskSize = orderBook.asks.firstEntry().getValue();
        double weightedMidprice = (bestBid * bestAskSize + bestAsk * bestBidSize) / (bestBidSize + bestAskSize);
        indicator.addPoint(weightedMidprice);
    }

    @Override
    public void onSnapshotEnd() {
        onTrade(initialState.getLastTradePrice(), initialState.getLastTradeSize(), initialState.getTradeInfo());
    }

    @Override
    public void stop() {
    }

    protected String getLineTitle() {
        return "Weighted Midprice";
    }
}
