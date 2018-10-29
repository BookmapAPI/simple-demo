package com.bookmap.api.simple.demo.indicators;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import com.bookmap.api.simple.demo.utils.data.VolumeCounter;
import com.bookmap.api.simple.demo.utils.data.VolumeCounter.VolumeCounterType;
import com.bookmap.api.simple.demo.utils.gui.BookmapSettingsPanel;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.CustomModule;
import velox.api.layer1.simplified.CustomSettingsPanelProvider;
import velox.api.layer1.simplified.Indicator;
import velox.api.layer1.simplified.InitialState;
import velox.api.layer1.simplified.IntervalListener;
import velox.api.layer1.simplified.Intervals;
import velox.api.layer1.simplified.TimeListener;
import velox.api.layer1.simplified.TradeDataListener;
import velox.gui.StrategyPanel;

@Layer1SimpleAttachable
@Layer1StrategyName("Volume Tracker")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class VolumeTracker
        implements CustomModule, TradeDataListener, TimeListener, IntervalListener, CustomSettingsPanelProvider {

    protected Indicator indicatorBid;
    protected Indicator indicatorAsk;
    protected final VolumeCounterType defaultVolumeCounterType = VolumeCounterType.EXPONENTIAL;
    protected VolumeCounterType volumeCounterType = defaultVolumeCounterType;
    protected long nanoseconds;
    protected final long defaultInterval = Intervals.INTERVAL_1_MINUTE;
    protected long interval = defaultInterval;
    protected VolumeCounter volumeCounter;

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        init(api);
    }

    protected void init(Api api) {
        volumeCounter = new VolumeCounter(interval, volumeCounterType);
        indicatorBid = api.registerIndicator("Volume Buy", GraphType.BOTTOM);
        indicatorBid.setColor(Color.GREEN);
        indicatorAsk = api.registerIndicator("Volume Sell", GraphType.BOTTOM);
        indicatorAsk.setColor(Color.RED);
    }

    @Override
    public void onTimestamp(long t) {
        nanoseconds = t;
    }

    @Override
    public long getInterval() {
        return Intervals.INTERVAL_50_MILLISECONDS;
    }

    @Override
    public void onInterval() {
        indicatorBid.addPoint(volumeCounter.getVolume(nanoseconds, true));
        indicatorAsk.addPoint(volumeCounter.getVolume(nanoseconds, false));
    }

    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        volumeCounter.onTrade(nanoseconds, tradeInfo.isBidAggressor, size);
    }

    protected void onSettingsChange() {
        indicatorBid.addPoint(Double.NaN);
        indicatorAsk.addPoint(Double.NaN);
        volumeCounter = new VolumeCounter(interval, volumeCounterType);
    }

    @Override
    public void stop() {
    }

    @Override
    public StrategyPanel[] getCustomSettingsPanels() {
        BookmapSettingsPanel panel = new BookmapSettingsPanel("Settings");
        addVolumeTypeSettings(panel);
        addIntervalSettings(panel);
        return new StrategyPanel[] { panel };
    }

    private void addVolumeTypeSettings(final BookmapSettingsPanel panel) {
        JComboBox<String> c = new JComboBox<>(VolumeCounterType.names());
        c.setSelectedItem(volumeCounterType);
        c.setEditable(false);
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (c.getSelectedIndex() != volumeCounterType.ordinal()) {
                    volumeCounterType = VolumeCounterType.values()[c.getSelectedIndex()];
                    onSettingsChange();
                }
            }
        });
        panel.addSettingsItem("Volume Tracker Type:", c);
    }

    private void addIntervalSettings(final BookmapSettingsPanel panel) {
        JComboBox<Integer> c = new JComboBox<>(
                new Integer[] { 1, 2, 3, 5, 10, 15, 30, 60, 120, 180, 300, 600, 900, 1800, 3600 });
        int selected = (int) (interval / 1_000_000_000L);
        c.setSelectedItem(selected);
        c.setEditable(false);
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                long newInterval = 1_000_000_000L * (int) c.getSelectedItem();
                if (newInterval != interval) {
                    interval = newInterval;
                    onSettingsChange();
                }
            }
        });
        panel.addSettingsItem("Time interval [seconds]:", c);
    }
}
