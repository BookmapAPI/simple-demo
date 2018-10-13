package com.bookmap.sergey.custommodules;

import java.awt.Color;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.layers.utils.OrderBook;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.Bar;
import velox.api.layer1.simplified.BarDataListener;
import velox.api.layer1.simplified.CustomModule;
import velox.api.layer1.simplified.Indicator;
import velox.api.layer1.simplified.Intervals;
import velox.api.layer1.simplified.TradeDataListener;

@Layer1SimpleAttachable
@Layer1StrategyName("ATR Trailing Stop")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class AtrTrailingStop implements CustomModule, TradeDataListener, BarDataListener {

    private static enum Trend {
        Up, Down, Undefined
    }

    protected Indicator lineBuy;
    protected Indicator lineSell;

    private double buyPrice = Double.NaN;
    private double sellPrice = Double.NaN;

    private final long barPeriod = Intervals.INTERVAL_1_MINUTE;
    private double atr = 5;
    private final double multiplier = 3.5;
    private final int atrPeriod = 10;

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api) {
        lineBuy = api.registerIndicator("ATR TS Buy", GraphType.PRIMARY, Color.WHITE);
        lineSell = api.registerIndicator("ATR TS Sell", GraphType.PRIMARY, Color.WHITE);
    }

    @Override
    public void onBar(OrderBook orderBook, Bar bar) {
        double tr = multiplier * (bar.getHigh() - bar.getLow());
        atr = ((atrPeriod - 1) * atr + tr) / atrPeriod;
    }

    @Override
    public long getBarInterval() {
        return barPeriod;
    }

    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        Trend trend = !Double.isNaN(buyPrice) ? Trend.Down : (!Double.isNaN(sellPrice) ? Trend.Up : Trend.Undefined);
        switch (trend) {
        case Up:
            if (sellPrice < price - atr) {
                setSellPrice(price - atr);
            } else if (sellPrice > price) {
                setBuyPrice(price + atr);
            }
            break;
        case Down:
            if (buyPrice > price + atr) {
                setBuyPrice(price + atr);
            } else if (buyPrice < price) {
                setSellPrice(price - atr);
            }
            break;
        case Undefined:
            setBuyPrice(price + atr);
        }
    }

    @Override
    public void stop() {
    }

    private void setBuyPrice(double price) {
        buyPrice = price;
        lineBuy.addPoint(price);
        if (!Double.isNaN(sellPrice)) {
            sellPrice = Double.NaN;
            lineSell.addPoint(Double.NaN);
        }
    }

    private void setSellPrice(double price) {
        sellPrice = price;
        lineSell.addPoint(price);
        if (!Double.isNaN(buyPrice)) {
            buyPrice = Double.NaN;
            lineBuy.addPoint(Double.NaN);
        }
    }
}
