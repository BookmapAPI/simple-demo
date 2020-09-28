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
import velox.api.layer1.simplified.SnapshotEndListener;
import velox.api.layer1.simplified.TimeListener;
import velox.api.layer1.simplified.TradeDataListener;

@Layer1SimpleAttachable
@Layer1StrategyName("ATR Trailing Stop")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class AtrTrailingStop extends AtrTrailingStopSettings
        implements TradeDataListener, BboListener, IntervalListener,
        SnapshotEndListener, HistoricalModeListener, TimeListener {

    private static final long defaultRecordingBarPeriod = Intervals.INTERVAL_1_SECOND;

    private static enum Trend {
        Up, Down, Undefined
    }

    private static final double defaultTr = 5;
    private static final double defaultAtr = defaultTr;

    protected double tr = defaultTr;
    protected double atr = defaultAtr;

    private double buyPrice = Double.NaN;
    private double sellPrice = Double.NaN;
    private double lastTradePrice = Double.NaN;
    private int lastBidPrice;
    private int lastAskPrice;
    private long timestamp;

    private Bar indivisibleBar = new Bar();
    private Bar compositeBar = new Bar();
    private Bar bboBidBar = new Bar();
    private Bar bboAskBar = new Bar();
    private Bar tbar = new Bar();
    
    private final Object lock = new Object();
    private long intervalNumber;
    
    private List<ImmutablePair <Long, Bar>> bars = new LinkedList<>();
    private List<ImmutablePair <Long, Bar>> bids = new LinkedList<>();
    private List<ImmutablePair <Long, Bar>> asks = new LinkedList<>();
    
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
            bars.add(new ImmutablePair<Long, Bar>(timestamp, getSemiClone(indivisibleBar)));
            bids.add(new ImmutablePair<Long, Bar>(timestamp, getSemiClone(bboBidBar)));
            asks.add(new ImmutablePair<Long, Bar>(timestamp, getSemiClone(bboAskBar)));
            
            indivisibleBar.startNext();
            bboBidBar.startNext();
            bboAskBar.startNext();

            if (!bars.isEmpty()) {
                updateAndOrTriggerTrades(bars.size() - 1);
                updateAndOrTriggerBars(bars.size());
                updateAndOrTriggerBbo(bars.size() - 1);
            }
        }
        intervalNumber++;
    }
    
    private void updateAndOrTriggerBars(int i) {
        if (isTimeToUpdate()) {
            int k = (int) (settings.barPeriod / 1_000_000_000L);
            List<ImmutablePair <Long, Bar>> granularBars  = bars.subList(i - Math.min(k, i), i);
            
            for (ImmutablePair <Long, Bar> pair : granularBars) {
                compositeBar.addTrade(true, 1, pair.right.getHigh());
                compositeBar.addTrade(true, 1, pair.right.getLow());
            }

            double preCalculatedTr = settings.multiplier * (compositeBar.getHigh() - compositeBar.getLow());
            tr = Double.isNaN(preCalculatedTr) ? defaultTr : preCalculatedTr;
            double preCalculatedAtr = ((settings.atrNumBars - 1) * atr + tr) / settings.atrNumBars;
            atr = Double.isNaN(preCalculatedAtr) ? defaultAtr : preCalculatedAtr;

            compositeBar.startNext();
            onAtrUpdated(tr, atr);
            
            if (settings.updateCondition == UpdateCondition.BAR) {
                onUpdateTriggered();
            }
        }
    }
    
    private void updateAndOrTriggerTrades(int i) {
        tbar = bars.get(i).getRight();

        if (!Double.isNaN(tbar.getClose())) {
            if (tbar.getHigh() > Math.max(tbar.getOpen(), tbar.getClose())) {
                updateLastTrades(tbar.getHigh());
            }
            if (tbar.getLow() < Math.min(tbar.getOpen(), tbar.getClose())) {
                updateLastTrades(tbar.getLow());
            }
            updateLastTrades(tbar.getClose());
        }
    }
    
    private void updateAndOrTriggerBbo(int i) {
        Bar askBar = asks.get(i).getRight();
        Bar bidBar = bids.get(i).getRight();

        checkBboUpdates(askBar.getHigh(), bidBar.getLow());
        checkBboUpdates(askBar.getClose(), bidBar.getClose());
    }
    
    private void checkBboUpdates(double askPrice, double bidPrice) {
        boolean updated = (bidPrice!= lastBidPrice || askPrice!= lastAskPrice);
        if (updated) {
            lastBidPrice = (int) bidPrice;
            lastAskPrice = (int) askPrice;
            
            if (settings.updateCondition == UpdateCondition.BBO) {
                onUpdateTriggered();
            }
        }
    }
   
    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        synchronized (lock) {
            double pricePips = fixPrice(price);
            indivisibleBar.addTrade(tradeInfo.isBidAggressor, size, pricePips);
        }
    }
    
    @Override
    public void onBbo(int bidPrice, int bidSize, int askPrice, int askSize) {
        synchronized (lock) {
            bboBidBar.addTrade(false, 1, bidPrice);
            bboAskBar.addTrade(false, 1, askPrice);
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
            }
            break;
        case NUM_BARS:
            int newAtrNumBars = (int) value;
            if (newAtrNumBars != settings.atrNumBars) {
                settings.atrNumBars = newAtrNumBars;
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
            }
            break;
        case SWITCH_CONDITION:
            int newSwitchCondition = (int) value;
            if (newSwitchCondition != settings.switchCondition) {
                settings.switchCondition = newSwitchCondition;
            }
            break;
        case ALL:
        }
        
        if (!settingsName.equals(SettingsName.RELOAD_CONDITION)) {
            reloadIfshould();
        }
    }
    
    private void reloadIfshould() {
        if (settings.reloadOnChange) {
            synchronized (lock) {
                resetInstanceFields();
                int size = bars.size();
                clearHistoricalIntervals(size);
                processHistoricalData(size);
            }
        }
    }
    
    private void resetInstanceFields() {
        buyPrice = Double.NaN;
        sellPrice = Double.NaN;
        lastTradePrice = Double.NaN;
        lastAskPrice = 0;
        lastBidPrice = 0;
        
        tr = defaultTr;
        atr = defaultAtr;
        timestamp = 0;
        
        indivisibleBar = new Bar();
        compositeBar = new Bar();
        bboBidBar = new Bar();
        bboAskBar = new Bar();
        tbar = new Bar();
        
        intervalNumber = 0;
    }
    
    private void clearHistoricalIntervals(int size) {
        lineBuy.clear(bars.get(0).getLeft(), bars.get(size - 1).getLeft());
        lineSell.clear(bars.get(0).getLeft(), bars.get(size - 1).getLeft());
    }
    
    private void processHistoricalData(int size) {
        for (int i = 0; i < size; i++) {
            this.timestamp = bars.get(i).getLeft();
            
            if (!bars.isEmpty()) {
                updateAndOrTriggerTrades(i);
                updateAndOrTriggerBars(i + 1);
                updateAndOrTriggerBbo(i);
            }
            intervalNumber++;
        }
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
        this.timestamp = t;
    }
    
    private Bar getSemiClone(Bar bar) {
        Bar semiClone = new Bar();
        semiClone.setOpen(bar.getOpen());
        semiClone.setHigh(bar.getHigh());
        semiClone.setLow(bar.getLow());
        semiClone.setClose(bar.getClose());
        return semiClone;
    }
    
    @Override
    protected void addPoint(boolean isBuy, double value) {
        (isBuy ? lineBuy : lineSell).addPoint(timestamp, value);
    }
    
    private boolean isTimeToUpdate() {
        long divider = settings.barPeriod / 1_000_000_000L;
        return intervalNumber % divider == 0 ? true : false;
    }
    
    private void updateLastTrades(double price) {
        double pricePips = fixPrice(price);

        if (lastTradePrice != pricePips) {
            lastTradePrice = pricePips;
            if (settings.updateCondition == UpdateCondition.TRADE) {
                onUpdateTriggered();
            }
        }
    }
}
