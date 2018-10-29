package com.bookmap.api.simple.demo.recorders;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.common.Log;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.Bar;
import velox.api.layer1.simplified.CustomModule;
import velox.api.layer1.simplified.HistoricalDataListener;
import velox.api.layer1.simplified.InitialState;
import velox.api.layer1.simplified.Intervals;
import velox.api.layer1.simplified.TimeListener;
import velox.api.layer1.simplified.TradeDataListener;

@Layer1SimpleAttachable
@Layer1StrategyName("Bar Builder")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class BarBuilder extends DataRecorderBase implements CustomModule, TradeDataListener, TimeListener, HistoricalDataListener {

    protected Long barTime = null;
    protected final long barInterval = Intervals.INTERVAL_1_MINUTE;
    private Bar bar = new Bar();

    @Override
    public void onTimestamp(long t) {
        if (barTime == null || barTime > t) {
            barTime = barInterval * (t / barInterval);
        }
        while (barTime + barInterval < t) {
            barTime += barInterval;
            onBar();
            bar.startNext();
        }
    }

    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        bar.addTrade(tradeInfo.isBidAggressor, size, price);
        writeObjects(getDateTime(barTime), tradeInfo.isBidAggressor ? "Buy" : "Sell", price, size);
    }

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
    }

    protected void onBar() {
        String datetime = getDateTime(barTime);
        String info = String.format("onBar time: %s. OHLC: %.0f, %.0f, %.0f, %.0f. Volume Buy Sell: %d, %d", datetime,
                bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getVolumeBuy(), bar.getVolumeSell());
        Log.info(info);
    }

    @Override
    protected String getFilename() {
        return "Volume_" + System.currentTimeMillis() + ".txt";
    }
}
