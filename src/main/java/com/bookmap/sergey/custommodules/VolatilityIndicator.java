package com.bookmap.sergey.custommodules;

import java.awt.Color;

import com.bookmap.sergey.custommodules.utils.SlidingVolumeHandler;

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
public class VolatilityIndicator extends LiquidityTracker implements TradeDataListener, TimeListener {

    private final long volumeInterval = Intervals.INTERVAL_1_MINUTE;
    private final int displayFactor = 100;
    final SlidingVolumeHandler volume = new SlidingVolumeHandler(volumeInterval);
    private long nanoseconds;

    protected void initLines(Api api) {
        line1 = api.registerIndicator("Volatility Absolute", GraphType.BOTTOM, Color.WHITE);
        line2 = api.registerIndicator("Volatility Directional", GraphType.BOTTOM, Color.BLUE);
    }

    @Override
    public void onTimestamp(long t) {
        nanoseconds = t;
    }

    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        volume.onTrade(nanoseconds, tradeInfo.isBidAggressor, size);
    }

    protected void onUpdate() {
        double bidSize = book.getExponentiallyWeightedAverage(true);
        double askSize = book.getExponentiallyWeightedAverage(false);
        if (bidSize == 0 || askSize == 0) {
            return;
        }
        int volumeBuy = volume.getVolume(nanoseconds, true);
        int volumeSell = volume.getVolume(nanoseconds, false);
        double absVolatility = displayFactor * (volumeBuy + volumeSell) / (bidSize + askSize);
        line1.addPoint(absVolatility);

        double dirBuy = volumeBuy / askSize;
        double dirSell = volumeSell / bidSize;
        double directionalVolatility = displayFactor * (dirBuy - dirSell) / (dirBuy + dirSell);
        line2.addPoint(directionalVolatility);
    }

}
