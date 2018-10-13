package com.bookmap.sergey.custommodules;

import velox.api.layer1.data.BalanceInfo;
import velox.api.layer1.data.ExecutionInfo;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.data.OrderInfoUpdate;
import velox.api.layer1.data.StatusInfo;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.layers.utils.OrderBook;
import velox.api.layer1.simplified.AllDataModule;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.Bar;

public class TestInterfaces implements AllDataModule {

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBalance(BalanceInfo balanceInfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBar(OrderBook orderBook, Bar bar) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public long getBarInterval() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void onBbo(int bidPrice, int bidSize, int askPrice, int askSize) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDepth(boolean isBid, int price, int size) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onCurrentInstrument(String alias) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onInstrumentAdded(InstrumentInfo info) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onOrderUpdated(OrderInfoUpdate orderInfoUpdate) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onOrderExecuted(ExecutionInfo executionInfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPositionUpdate(StatusInfo statusInfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTimestamp(long t) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onRealtimeStart() {
        // TODO Auto-generated method stub
        
    }

}
