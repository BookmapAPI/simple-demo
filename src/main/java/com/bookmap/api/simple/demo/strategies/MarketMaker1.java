package com.bookmap.api.simple.demo.strategies;

import java.awt.Color;

import com.bookmap.api.simple.demo.indicators.VolumeTracker;
import com.bookmap.api.simple.demo.utils.data.OrderBookExponential;
import com.bookmap.api.simple.demo.utils.data.OrderBookSum;
import com.bookmap.api.simple.demo.utils.data.VolumeCounter;
import com.bookmap.api.simple.demo.utils.data.VolumeCounter.VolumeCounterType;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.annotations.Layer1TradingStrategy;
import velox.api.layer1.data.ExecutionInfo;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.data.OrderInfoUpdate;
import velox.api.layer1.data.OrderSendParameters;
import velox.api.layer1.data.SimpleOrderSendParametersBuilder;
import velox.api.layer1.data.StatusInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.InitialState;
import velox.api.layer1.simplified.OrdersListener;
import velox.api.layer1.simplified.PositionListener;

@Layer1SimpleAttachable
@Layer1TradingStrategy
@Layer1StrategyName("MarketMaker1")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class MarketMaker1 extends VolumeTracker implements OrdersListener, PositionListener {

    private final int levels = 10;
    OrderBookSum orderBook = new OrderBookExponential(levels);
    StatusInfo statusInfo;
    Api api;
    private String alias;

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        this.alias = alias;
        super.initialize(alias, info, api, initialState);
    }
    
    protected void init(Api api) {
        this.api = api;
        volumeCounter = new VolumeCounter(interval, VolumeCounterType.EXPONENTIAL);
        indicatorBid = api.registerIndicator("MM Buy", GraphType.PRIMARY);
        indicatorBid.setColor(Color.GREEN);
        indicatorAsk = api.registerIndicator("MM Sell", GraphType.PRIMARY);
        indicatorAsk.setColor(Color.RED);
    }

    @Override
    public void onPositionUpdate(StatusInfo statusInfo) {
        this.statusInfo = statusInfo;
    }

    @Override
    public void onOrderUpdated(OrderInfoUpdate orderInfoUpdate) {
        OrderSendParameters orderSendParameters = new SimpleOrderSendParametersBuilder(alias, true, 1)
                .build();
        api.sendOrder(orderSendParameters);
    }

    @Override
    public void onOrderExecuted(ExecutionInfo executionInfo) {
    }

}
