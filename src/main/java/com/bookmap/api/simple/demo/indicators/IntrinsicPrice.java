package com.bookmap.api.simple.demo.indicators;

import java.awt.Color;
import java.util.Map.Entry;
import java.util.TreeMap;

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
@Layer1StrategyName("Intrinsic Price")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class IntrinsicPrice implements CustomModule, BarDataListener {

    private int hypotheticalMarketOrderSize = 1000;
    private Indicator line;

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        line = api.registerIndicator("Intrinsic Price", GraphType.PRIMARY);
        line.setColor(Color.WHITE);
    }

    @Override
    public void stop() {
    }

    @Override
    public long getInterval() {
        return Intervals.INTERVAL_100_MILLISECONDS;
    }

    @Override
    public void onBar(OrderBook orderBook, Bar bar) {
        double intrinsic = (calcIntrinsic(orderBook.getBidMap()) + calcIntrinsic(orderBook.getAskMap())) / 2;
        line.addPoint(intrinsic);
    }

    private double calcIntrinsic(final TreeMap<Integer, Long> book) {
        double executionPrice = 0;
        int hmoSize = hypotheticalMarketOrderSize;
        for (Entry<Integer, Long> entry : book.entrySet()) {
            if (hmoSize == 0) {
                break;
            }
            long matchedSize = Math.min(entry.getValue(), hmoSize);
            hmoSize -= matchedSize;
            executionPrice += entry.getKey() * matchedSize;
        }
        return hmoSize == 0 ? executionPrice / hypotheticalMarketOrderSize : Double.NaN;
    }
}
