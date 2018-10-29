package com.bookmap.api.simple.demo.indicators;

import java.awt.Color;

import com.bookmap.api.simple.demo.utils.data.VolumeCounter;
import com.bookmap.api.simple.demo.utils.data.VolumeCounter.VolumeCounterType;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.Intervals;
import velox.api.layer1.simplified.TimeListener;
import velox.api.layer1.simplified.TradeDataListener;

@Layer1SimpleAttachable
@Layer1StrategyName("Volatility Indicator")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class VolatilityIndicator extends LiquidityTrackerExponential implements TradeDataListener, TimeListener {

    private final long volumeInterval = Intervals.INTERVAL_10_MINUTES;
    private final int displayFactor = 100;
    final VolumeCounter volume = new VolumeCounter(volumeInterval, VolumeCounterType.MOVING_SUM);
    private long nanoseconds;

    protected void registerIndicators(Api api) {
        indicatorBid = api.registerIndicator("Volatility Absolute", GraphType.BOTTOM);
        indicatorBid.setColor(Color.WHITE);
        indicatorAsk = api.registerIndicator("Volatility Directional", GraphType.BOTTOM);
        indicatorAsk.setColor(Color.BLUE);
    }

    @Override
    public void onTimestamp(long t) {
        nanoseconds = t;
    }

    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        volume.onTrade(nanoseconds, tradeInfo.isBidAggressor, size);
    }

    @Override
    public void onInterval() {
        double bidSize = orderBook.getSizeSum(true);
        double askSize = orderBook.getSizeSum(false);
        if (bidSize == 0 || askSize == 0) {
            return;
        }
        double volumeBuy = volume.getVolume(nanoseconds, true);
        double volumeSell = volume.getVolume(nanoseconds, false);
        double absVolatility = displayFactor * (volumeBuy + volumeSell) / (bidSize + askSize);
        indicatorBid.addPoint(absVolatility);

        double dirBuy = volumeBuy / askSize;
        double dirSell = volumeSell / bidSize;
        double directionalVolatility = displayFactor * (dirBuy - dirSell) / (dirBuy + dirSell);
        indicatorAsk.addPoint(directionalVolatility);
    }

}
