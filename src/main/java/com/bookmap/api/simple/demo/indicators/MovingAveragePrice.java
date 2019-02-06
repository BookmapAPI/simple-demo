package com.bookmap.api.simple.demo.indicators;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import com.bookmap.api.simple.demo.utils.data.MovingAverage;
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
import velox.api.layer1.simplified.TradeDataListener;
import velox.gui.StrategyPanel;

@Layer1SimpleAttachable
@Layer1StrategyName("Moving Average Price")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class MovingAveragePrice
        implements CustomModule, TradeDataListener, IntervalListener, CustomSettingsPanelProvider {

    private double lastTradePrice = Double.NaN;
    private Indicator indicator;

    private final long defaultPeriodSeconds = 30;
    private long periodSeconds = defaultPeriodSeconds;

    private final long interval = Intervals.INTERVAL_50_MILLISECONDS;
    private MovingAverage ma;
    private Api api;

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        indicator = api.registerIndicator("Moving Average", GraphType.PRIMARY);
        indicator.setColor(Color.WHITE);
        this.lastTradePrice = initialState.getLastTradePrice();
        this.api = api;
        ma = new MovingAverage(getNumPeriods());
    }

    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        lastTradePrice = price;
    }

    @Override
    public long getInterval() {
        return Intervals.INTERVAL_100_MILLISECONDS;
    }

    @Override
    public void onInterval() {
        if (!Double.isNaN(lastTradePrice)) {
            double price = ma.update(lastTradePrice);
            indicator.addPoint(price);
        }
    }

    @Override
    public void stop() {
    }

    private long getNumPeriods() {
        return (1_000_000_000L * periodSeconds) / interval;
    }

    @Override
    public StrategyPanel[] getCustomSettingsPanels() {
        BookmapSettingsPanel panel = new BookmapSettingsPanel("Settings");
        addPeriodSettings(panel);
        return new StrategyPanel[] { panel };
    }

    private void onSettingsUpdated() {
        ma.updatePeriod(getNumPeriods());
        api.reload();
    }

    private void addPeriodSettings(final BookmapSettingsPanel panel) {
        JComboBox<Integer> c = new JComboBox<>(
                new Integer[] { 1, 2, 3, 5, 10, 15, 30, 60, 120, 180, 300, 600, 900, 1800, 3600, 7200, 14400 });
        int selected = (int) (periodSeconds);
        c.setSelectedItem(selected);
        c.setEditable(false);
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int newPeriodSeconds = (int) c.getSelectedItem();
                if (newPeriodSeconds != periodSeconds) {
                    periodSeconds = newPeriodSeconds;
                    onSettingsUpdated();
                }
            }
        });
        panel.addSettingsItem("Period [seconds]:", c);
    }
}
