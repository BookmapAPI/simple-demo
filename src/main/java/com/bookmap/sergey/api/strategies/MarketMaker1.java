package com.bookmap.sergey.api.strategies;

import java.awt.Color;

import com.bookmap.sergey.api.indicators.VolumeTracker;
import com.bookmap.sergey.api.utils.data.OrderBookExponential;
import com.bookmap.sergey.api.utils.data.OrderBookSum;
import com.bookmap.sergey.api.utils.data.VolumeCounter;
import com.bookmap.sergey.api.utils.data.VolumeCounter.VolumeCounterType;

import velox.api.layer1.data.ExecutionInfo;
import velox.api.layer1.data.OrderInfoUpdate;
import velox.api.layer1.data.StatusInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.OrdersListener;
import velox.api.layer1.simplified.PositionListener;

public class MarketMaker1 extends VolumeTracker implements OrdersListener, PositionListener {

    private final int levels = 10;
    OrderBookSum orderBook = new OrderBookExponential(levels);
    StatusInfo statusInfo;
    Api api;

    protected void init(Api api) {
        this.api = api;
        volumeCounter = new VolumeCounter(interval, VolumeCounterType.EXPONENTIAL);
        indicatorBid = api.registerIndicator("MM Buy", GraphType.PRIMARY, Color.GREEN);
        indicatorAsk = api.registerIndicator("MM Sell", GraphType.PRIMARY, Color.RED);
    }

    @Override
    public void onPositionUpdate(StatusInfo statusInfo) {
        this.statusInfo = statusInfo;
    }

    @Override
    public void onOrderUpdated(OrderInfoUpdate orderInfoUpdate) {
//        OrderSendParameters orderSendParameters = new OrderSendParameters() {
//        };
//        api.sendOrder(orderSendParameters);
    }

    @Override
    public void onOrderExecuted(ExecutionInfo executionInfo) {
    }

}
