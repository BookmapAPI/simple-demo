package com.bookmap.api.simple.demo.recorders;

import java.text.SimpleDateFormat;
import java.util.Date;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.BboListener;
import velox.api.layer1.simplified.CustomModule;
import velox.api.layer1.simplified.DepthDataListener;
import velox.api.layer1.simplified.InitialState;
import velox.api.layer1.simplified.TimeListener;
import velox.api.layer1.simplified.TradeDataListener;

@Layer1SimpleAttachable
@Layer1StrategyName("Data Recorder")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class DataRecorder extends DataRecorderBase
        implements CustomModule, DepthDataListener, TradeDataListener, BboListener, TimeListener {

    private long nanoseconds;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        addInstrument(alias, info);
    }

    @Override
    public void onTimestamp(long t) {
        nanoseconds = t;
    }

    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        String side = tradeInfo.isBidAggressor ? "Buy" : "Sell";
        writeObjects("Trade", side, price, size);
    }

    @Override
    public void onDepth(boolean isBid, int price, int size) {
        String side = isBid ? "Buy" : "Sell";
        writeObjects("Quote", side, price, size);
    }

    @Override
    public void onBbo(int bidPrice, int bidSize, int askPrice, int askSize) {
        // BBO is redundant given market depth data. For demonstration only.
        writeObjects("BBO", "Buy", bidPrice, bidSize);
        writeObjects("BBO", "Sell", askPrice, askSize);
    }

    protected void addInstrument(String alias, InstrumentInfo info) {
        writeObjects("InstrumentAdded", "Alias=" + alias, "MinPriceIncrement=" + info.pips, "Multiplier=" + info.multiplier);
    }

    @Override
    protected String getFilename() {
        return "DataRecorder_" + System.currentTimeMillis() + ".txt";
    }

    @Override
    protected void appendFirst(final StringBuilder s) {
        s.append(getTimestamp());
    }

    protected String getTimestamp() {
        long millis = nanoseconds / 1_000_000L;
        long nanos = nanoseconds - 1_000_000L * millis;
        String t = sdf.format(new Date(millis)) + String.format("%06d", nanos);
        return t;
    }
}
