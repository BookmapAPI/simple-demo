package com.bookmap.api.simple.demo.indicators;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.tuple.ImmutablePair;
import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.Bar;
import velox.api.layer1.simplified.BboListener;
import velox.api.layer1.simplified.HistoricalModeListener;
import velox.api.layer1.simplified.InitialState;
import velox.api.layer1.simplified.IntervalListener;
import velox.api.layer1.simplified.Intervals;
import velox.api.layer1.simplified.ReceiveHistory;
import velox.api.layer1.simplified.SnapshotEndListener;
import velox.api.layer1.simplified.TimeListener;
import velox.api.layer1.simplified.TradeDataListener;

@ReceiveHistory(shouldReceiveHistory = true)
@Layer1SimpleAttachable
@Layer1StrategyName("ATR Trailing Stop")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class AtrTrailingStop extends AtrTrailingStopSettings
        implements TradeDataListener, BboListener, IntervalListener
        , SnapshotEndListener
        , HistoricalModeListener
        , TimeListener        
        {

    private final static long defaultRecordingBarPeriod = Intervals.INTERVAL_1_SECOND;

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
    private long t;

    private Bar bar = new Bar();
    private Bar minimalIntervalBar = new Bar();
    private final Object lock = new Object();
    boolean isReloaded;
    long intervalNumber;
    
    private List<ImmutablePair <Long, Bar>> bars = new LinkedList<>();
    private List<ImmutablePair <Long, BboEvent>> bbos = new LinkedList<>();
    
    private static class BboEvent{
        public final int bidPrice;
        public final int askPrice;
        
        public BboEvent(int bidPrice, int askPrice) {
            super();
            this.bidPrice = bidPrice;
            this.askPrice = askPrice;
        }
    }
    
    private static class StorageUnit {
        long t;
        double buyPrice;
        double sellPrice;
        double lastTradePrice;
        int lastBidPrice;
        int lastAskPrice;
        double tr;
        double atr;
        
        public StorageUnit(long t, double buyPrice, double sellPrice, double lastTradePrice, int lastBidPrice,
                int lastAskPrice, double tr, double atr) {
            super();
            this.t = t;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.lastTradePrice = lastTradePrice;
            this.lastBidPrice = lastBidPrice;
            this.lastAskPrice = lastAskPrice;
            this.tr = tr;
            this.atr = atr;
        }
    }

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        super.initialize(alias, info, api, initialState);
        lastTradePrice = fixPrice(initialState.getLastTradePrice());
        lastBidPrice = lastAskPrice = (int) Math.round(lastTradePrice);
        onAtrUpdated(tr, atr);
    }

    @Override
    public long getInterval() {
        return defaultRecordingBarPeriod;
    }

    @Override
    public void onInterval() {
        synchronized (lock) {
            bars.add(new ImmutablePair<Long, Bar>(t, getSemiClone(minimalIntervalBar)));
            bbos.add(new ImmutablePair<Long, BboEvent>(t, new BboEvent(lastBidPrice, lastAskPrice)));
            minimalIntervalBar.startNext();

            long divider = settings.barPeriod / 1_000_000_000L;
            
            if (intervalNumber % divider == 0) {
                double preCalculatedTr = settings.multiplier * (bar.getHigh() - bar.getLow());
                tr = Double.isNaN(preCalculatedTr) ? defaultTr : preCalculatedTr;
                double preCalculatedAtr = ((settings.atrNumBars - 1) * atr + tr) / settings.atrNumBars;
                atr = Double.isNaN(preCalculatedAtr) ? defaultAtr : preCalculatedAtr;

                bar.startNext();
                onAtrUpdated(tr, atr);

                if (settings.updateCondition == UpdateCondition.BAR) {
                    onUpdateTriggered();
                }
            }
        }
        intervalNumber++;
    }

    @Override
    public void onBbo(int bidPrice, int bidSize, int askPrice, int askSize) {
        synchronized (lock) {
            boolean updated = (bidPrice != lastBidPrice) || (askPrice != lastAskPrice);
                lastBidPrice = bidPrice;
                lastAskPrice = askPrice;

            if (updated && settings.updateCondition == UpdateCondition.BBO) {
                onUpdateTriggered();
            }
        }
    }

    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        synchronized (lock) {
            double pricePips = fixPrice(price);
            bar.addTrade(tradeInfo.isBidAggressor, size, pricePips);
            minimalIntervalBar.addTrade(tradeInfo.isBidAggressor, size, pricePips);
            
            if (lastTradePrice != pricePips) {
                lastTradePrice = pricePips;
                if (settings.updateCondition == UpdateCondition.TRADE) {
                    onUpdateTriggered();
                }
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
                reloadIfshould();
            }
            break;
        case NUM_BARS:
            int newAtrNumBars = (int) value;
            if (newAtrNumBars != settings.atrNumBars) {
                settings.atrNumBars = newAtrNumBars;
                onSettingsUpdated();
            }
            break;
        case RELOAD_CONDITION:
            boolean reloadOnChange = (boolean) value;
            if (reloadOnChange  != settings.reloadOnChange) {
                settings.reloadOnChange = (boolean) value;
            }
            break;
        case UPDATE_CONDITION:
            UpdateCondition newUpdateCondition = UpdateCondition.fromString((String) value);
            if (newUpdateCondition != settings.updateCondition) {
                settings.updateCondition = newUpdateCondition;
                onSettingsUpdated();
                reloadIfshould();
            }
            break;
        case SWITCH_CONDITION:
            int newSwitchCondition = (int) value;
            if (newSwitchCondition != settings.switchCondition) {
                settings.switchCondition = newSwitchCondition;
                onSettingsUpdated();
            }
        case ALL:
            reloadIfshould();
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

    @Override
    public void onSnapshotEnd() {
        onSettingsUpdated();
    }

    @Override
    public void onRealtimeStart() {

    }

    @Override
    public void onTimestamp(long t) {
        this.t = t;
    }
    
    private void reloadIfshould() {
        if (settings.reloadOnChange) {
            synchronized (lock) {
                isReloaded = true;

                // store fields
                StorageUnit unit = new StorageUnit(t, buyPrice, sellPrice, lastTradePrice, lastBidPrice, lastAskPrice, tr, atr);

                resetInstanceFields();

                int size = bars.size();
                clearHistoricalIntervals(size);
                processHistoricalData(size);

                // load fields
                restoreFields(unit);
                isReloaded = false;
            }
        }
    }

    private Bar getSemiClone(Bar bar) {
        Bar semiClone = new Bar();
        semiClone.setOpen(bar.getOpen());
        semiClone.setHigh(bar.getHigh());
        semiClone.setLow(bar.getLow());
        semiClone.setClose(bar.getClose());
        return semiClone;
    }
    
    private void resetInstanceFields() {
        buyPrice = Double.NaN;
        sellPrice = Double.NaN;
        lastTradePrice = Double.NaN;
        lastAskPrice = 0;
        lastBidPrice = 0;
        tr = defaultTr;
        atr = defaultAtr;
    }
    
    private void restoreFields(StorageUnit unit) {
        t = unit.t;
        buyPrice = unit.buyPrice;
        sellPrice = unit.sellPrice;
        lastTradePrice = unit.lastTradePrice;
        lastBidPrice = unit.lastBidPrice;
        lastAskPrice = unit.lastAskPrice;
        tr = unit.tr;
        atr = unit.atr;
    }
    
    private void clearHistoricalIntervals(int size) {
        lineBuy.clear(bars.get(0).getLeft(), bars.get(size - 1).getLeft());
        lineSell.clear(bars.get(0).getLeft(), bars.get(size - 1).getLeft());
    }
    
    private void processHistoricalData(int size) {
        boolean tradeUpdated = false;
        boolean bboUpdated = false;
        Bar reloadBar = new Bar();

        for (int i = 0; i < size; i++) {
            t = bars.get(i).getLeft();
            Bar ohlc = bars.get(i).getRight();

            reloadBar.addTrade(true, 1, ohlc.getHigh());
            reloadBar.addTrade(true, 1, ohlc.getLow());
            double pricePips = ohlc.getClose();

            if (lastTradePrice != pricePips) {
                lastTradePrice = pricePips;
                tradeUpdated = true;
            }

            BboEvent bbo = bbos.get(i).getRight();
            int askPrice = bbo.askPrice;
            int bidPrice = bbo.bidPrice;
            bboUpdated = (bidPrice != lastBidPrice) || (askPrice != lastAskPrice);

            lastBidPrice = bidPrice;
            lastAskPrice = askPrice;

            if (settings.updateCondition == UpdateCondition.TRADE && tradeUpdated) {
                onUpdateTriggered();
            } else if (settings.updateCondition == UpdateCondition.BBO && bboUpdated) {
                onUpdateTriggered();
            } else if ((i + 1) % (settings.barPeriod / 1_000_000_000L) == 0) {
                double preCalculatedTr = settings.multiplier * (reloadBar.getHigh() - reloadBar.getLow());
                tr = Double.isNaN(preCalculatedTr) ? defaultTr : preCalculatedTr;
                double preCalculatedAtr = ((settings.atrNumBars - 1) * atr + tr) / settings.atrNumBars;
                atr = Double.isNaN(preCalculatedAtr) ? defaultAtr : preCalculatedAtr;

                onAtrUpdated(tr, atr);

                if (settings.updateCondition == UpdateCondition.BAR) {
                    onUpdateTriggered();
                }
            }
            tradeUpdated = false;
            bboUpdated = false;
        }
    }

    @Override
    protected void addPoint(boolean isBuy, double value) {
        if (isReloaded) {
            (isBuy ? lineBuy : lineSell).addPoint(t, value);
        } else {
            (isBuy ? lineBuy : lineSell).addPoint(value);
        }
    }
}
