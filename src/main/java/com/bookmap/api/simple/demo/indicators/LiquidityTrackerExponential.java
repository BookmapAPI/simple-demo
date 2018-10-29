package com.bookmap.api.simple.demo.indicators;

import java.awt.Color;

import com.bookmap.api.simple.demo.utils.data.OrderBookExponential;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.InitialState;

@Layer1SimpleAttachable
@Layer1StrategyName("Liquidity Tracker Exponential")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class LiquidityTrackerExponential extends LiquidityTracker {

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        super.initialize(alias, info, api, initialState);
        orderBook = new OrderBookExponential(levels);
    }
    
    protected void registerIndicators(Api api) {
        indicatorBid = api.registerIndicator("Liquidity Bid Exponential", GraphType.BOTTOM);
        indicatorBid.setColor(Color.GREEN);
        indicatorAsk = api.registerIndicator("Liquidity Ask Exponential", GraphType.BOTTOM);
        indicatorAsk.setColor(Color.RED);
    }
}
