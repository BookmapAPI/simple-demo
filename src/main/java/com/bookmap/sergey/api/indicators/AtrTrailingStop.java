package com.bookmap.sergey.api.indicators;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.Bar;
import velox.api.layer1.simplified.BboListener;
import velox.api.layer1.simplified.InitialState;
import velox.api.layer1.simplified.IntervalListener;
import velox.api.layer1.simplified.TradeDataListener;

@Layer1SimpleAttachable
@Layer1StrategyName("ATR Trailing Stop")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class AtrTrailingStop extends AtrTrailingStopSettings
        implements TradeDataListener, BboListener, IntervalListener {

    private static enum Trend {
        Up, Down, Undefined
    }

    private final static double defaultTr = 5;
    private final static double defaultAtr = defaultTr;

    protected double tr = defaultTr;
    protected double atr = defaultAtr;

    private double buyPrice = Double.NaN;
    private double sellPrice = Double.NaN;
    private double lastTradePrice = Double.NaN;
    private int lastBidPrice;
    private int lastAskPrice;

    private Bar bar = new Bar();

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        super.initialize(alias, info, api, initialState);
        lastTradePrice = fixPrice(initialState.getLastTradePrice());
        lastBidPrice = lastAskPrice = (int) Math.round(lastTradePrice);
        onAtrUpdated(tr, atr);
        onSettingsUpdated();
    }

    @Override
    public long getInterval() {
        return settings.barPeriod;
    }

    @Override
    public void onInterval() {
        tr = settings.multiplier * (bar.getHigh() - bar.getLow());
        atr = ((settings.atrNumBars - 1) * atr + tr) / settings.atrNumBars;
        bar.startNext();
        onAtrUpdated(tr, atr);
        if (settings.updateCondition == UpdateCondition.BAR) {
            onUpdateTriggered();
        }
    }

    @Override
    public void onBbo(int bidPrice, int bidSize, int askPrice, int askSize) {
        boolean updated = (bidPrice != lastBidPrice) || (askPrice != lastAskPrice);
        lastBidPrice = bidPrice;
        lastAskPrice = askPrice;
        if (updated && settings.updateCondition == UpdateCondition.BBO) {
            onUpdateTriggered();
        }
    }

    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        double pricePips = fixPrice(price);
        bar.addTrade(tradeInfo.isBidAggressor, size, pricePips);
        if (lastTradePrice != pricePips) {
            lastTradePrice = pricePips;
            if (settings.updateCondition == UpdateCondition.TRADE) {
                onUpdateTriggered();
            }
        }
    }

    private void onUpdateTriggered() {
        Trend trend = getTrend();
        double buyPriceDefault = getDefaultPrice(true);
        double sellPriceDefault = getDefaultPrice(false);
        switch (trend) {
        case Up:
            if (sellPrice < sellPriceDefault) {
                setSellPrice(sellPriceDefault);
            } else if (sellPrice >= lastTradePrice + settings.switchCondition) {
                setDefaultBuyPrice();
            }
            break;
        case Down:
            if (buyPrice > buyPriceDefault) {
                setBuyPrice(buyPriceDefault);
            } else if (buyPrice <= lastTradePrice - settings.switchCondition) {
                setDefaultSellPrice();
            }
            break;
        case Undefined:
            setDefaultBuyPrice();
        }
    }

    private void onSettingsUpdated() {
        Trend trend = getTrend();
        switch (trend) {
        case Up:
            setDefaultSellPrice();
            break;
        case Down:
        case Undefined:
            setDefaultBuyPrice();
        }
        onAtrUpdated(tr, atr);
    }

    protected void onSettingsUpdated(SettingsName settingsName, Object value) {
        switch (settingsName) {
        case MULTIPLIER:
            double newMultiplier = (double) value;
            if (newMultiplier != settings.multiplier) {
                atr = atr * newMultiplier / settings.multiplier;
                settings.multiplier = newMultiplier;
                onAtrUpdated(tr, atr);
            }
            break;
        case BAR_PERIOD:
            long newBarPeriod = 1_000_000_000L * (int) value;
            if (newBarPeriod != settings.barPeriod) {
                settings.barPeriod = newBarPeriod;
                api.reload();
            }
            break;
        case NUM_BARS:
            int newAtrNumBars = (int) value;
            if (newAtrNumBars != settings.atrNumBars) {
                settings.atrNumBars = newAtrNumBars;
                onSettingsUpdated();
            }
            break;
        case UPDATE_CONDITION:
            UpdateCondition newUpdateCondition = UpdateCondition.fromString((String) value);
            if (newUpdateCondition != settings.updateCondition) {
                settings.updateCondition = newUpdateCondition;
                onSettingsUpdated();
            }
            break;
        case SWITCH_CONDITION:
            int newSwitchCondition = (int) value;
            if (newSwitchCondition != settings.switchCondition) {
                settings.switchCondition = newSwitchCondition;
                onSettingsUpdated();
            }
        case ALL:
            api.reload();
        }
        onSettingsUpdated();
    }

    private Trend getTrend() {
        return !Double.isNaN(buyPrice) ? Trend.Down : (!Double.isNaN(sellPrice) ? Trend.Up : Trend.Undefined);
    }

    private double getDefaultPrice(boolean isBuy) {
        return fixPrice(isBuy ? lastTradePrice + atr : lastTradePrice - atr);
    }

    private double fixPrice(double price) {
        return Double.isNaN(price) ? price : Math.round(price);
    }

    private void setDefaultBuyPrice() {
        setSellPrice(Double.NaN);
        setBuyPrice(getDefaultPrice(true));
    }

    private void setDefaultSellPrice() {
        setBuyPrice(Double.NaN);
        setSellPrice(getDefaultPrice(false));
    }

    private void setBuyPrice(double price) {
        buyPrice = price;
        addPoint(true, price);
        if (!Double.isNaN(sellPrice)) {
            sellPrice = Double.NaN;
            addPoint(false, Double.NaN);
        }
    }

    private void setSellPrice(double price) {
        sellPrice = price;
        addPoint(false, price);
        if (!Double.isNaN(buyPrice)) {
            buyPrice = Double.NaN;
            addPoint(true, Double.NaN);
        }
    }

    public static String barToString(long nanoseconds, final Bar bar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String t = sdf.format(new Date(nanoseconds / 1_000_000L));
        String info = String.format("onBar time: %s. OHLC: %.0f, %.0f, %.0f, %.0f. Volume Buy Sell: %d, %d", t,
                bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getVolumeBuy(), bar.getVolumeSell());
        return info;
    }
}
