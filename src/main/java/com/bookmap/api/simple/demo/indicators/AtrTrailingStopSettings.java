package com.bookmap.api.simple.demo.indicators;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.bookmap.api.simple.demo.utils.gui.BookmapSettingsPanel;

import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;
import velox.api.layer1.settings.StrategySettingsVersion;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.CustomModule;
import velox.api.layer1.simplified.CustomSettingsPanelProvider;
import velox.api.layer1.simplified.Indicator;
import velox.api.layer1.simplified.IndicatorModifiable;
import velox.api.layer1.simplified.InitialState;
import velox.api.layer1.simplified.Intervals;
import velox.api.layer1.simplified.LineStyle;
import velox.gui.StrategyPanel;
import velox.gui.colors.ColorsConfigItem;

public abstract class AtrTrailingStopSettings implements CustomModule, CustomSettingsPanelProvider {

    protected abstract void onSettingsUpdated(SettingsName s, Object value);

    public static enum SettingsName {
        MULTIPLIER, BAR_PERIOD, NUM_BARS, UPDATE_CONDITION, SWITCH_CONDITION, RELOAD_CONDITION, ALL
    }

    public static enum UpdateCondition {
        TRADE, BBO, BAR;

        @SuppressWarnings("serial")
        private static Map<UpdateCondition, String> names = new HashMap<UpdateCondition, String>()
        {{
             put(TRADE, "On every trade");
             put(BBO, "On BBO change");
             put(BAR, "On bar close");
        }};

        @Override
        public String toString() {
            return names.get(this);
        }

        public static UpdateCondition fromString(String s) {
            for (Map.Entry<UpdateCondition, String> entry : names.entrySet()) {
                if (entry.getValue().equals(s)) {
                    return entry.getKey();
                }
            }
            throw new IllegalArgumentException("Invalid UpdateCondition name: " + s);
        }
    }

    private static final Color defaultColorBuy = Color.WHITE;
    private static final Color defaultColorSell = Color.WHITE;
    private static final LineStyle defaultLineStyle = LineStyle.SOLID;
    private static final int defaultLineWidth = 3;
    private static final double defaultMultiplier = 2;
    private static final long defaultBarPeriod = Intervals.INTERVAL_1_MINUTE;
    private static final int defaultAtrNumBars = 10;
    private static final UpdateCondition defaultUpdateCondition = UpdateCondition.TRADE;
    private static final int defaultSwitchCondition = 1;

    @StrategySettingsVersion(currentVersion = 1, compatibleVersions = {})
    public static class Settings {
        public Color colorBuy = defaultColorBuy;
        public Color colorSell = defaultColorSell;
        public LineStyle lineStyle = defaultLineStyle;
        public int lineWidth = defaultLineWidth;

        public double multiplier = defaultMultiplier;
        public long barPeriod = defaultBarPeriod;
        public int atrNumBars = defaultAtrNumBars;
        public UpdateCondition updateCondition = defaultUpdateCondition;
        public int switchCondition = defaultSwitchCondition;
        public boolean reloadOnChange = true;
    }

    private final JLabel labelTr = new JLabel();
    private final JLabel labelAtr = new JLabel();

    protected IndicatorModifiable lineBuy;
    protected IndicatorModifiable lineSell;

    protected Settings settings;
    protected Api api;

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        this.api = api;
        settings = api.getSettings(Settings.class);
        lineBuy = api.registerIndicatorModifiable("ATR TS Buy", GraphType.PRIMARY);
        setVisualProperties(lineBuy);
        lineSell = api.registerIndicatorModifiable("ATR TS Sell", GraphType.PRIMARY);
        setVisualProperties(lineSell);
    }

    private void setVisualProperties(final Indicator indicator) {
        indicator.setLineStyle(settings.lineStyle);
        indicator.setWidth(settings.lineWidth);
    }

    protected void addPoint(boolean isBuy, double value) {
        (isBuy ? lineBuy : lineSell).addPoint(value);
    }

    protected void onAtrUpdated(double tr, double atr) {
        labelTr.setText(String.format("%.3f", tr));
        labelAtr.setText(String.format("%.3f", atr));
    }

    @Override
    public void stop() {
        api.setSettings(settings);
    }

    @Override
    public StrategyPanel[] getCustomSettingsPanels() {
        StrategyPanel p1 = getStyleSettingsPanel();
        StrategyPanel p2 = getParametersSettingsPanel();
        StrategyPanel p3 = getStatisticsPanel();
        return new StrategyPanel[] { p1, p2, p3 };
    }

    private StrategyPanel getStyleSettingsPanel() {
        BookmapSettingsPanel panel = new BookmapSettingsPanel("Indicators style settings");
        addColorsSettings(panel);
        addLineStyleSettings(panel);
        addLineWidthSettings(panel);
        return panel;
    }

    private void addColorsSettings(final BookmapSettingsPanel panel) {
        panel.addSettingsItem("Buy Trailing Stop color:", createColorsConfigItem(true));
        panel.addSettingsItem("Sell Trailing Stop color:", createColorsConfigItem(false));
    }

    private ColorsConfigItem createColorsConfigItem(boolean isBuy) {
        Consumer<Color> c = new Consumer<Color>() {

            @Override
            public void accept(Color color) {
                if (isBuy) {
                    settings.colorBuy = color;
                    lineBuy.setColor(settings.colorBuy);
                } else {
                    settings.colorSell = color;
                    lineSell.setColor(settings.colorSell);
                }
            }
        };
        Color color = isBuy ? settings.colorBuy : settings.colorSell;
        Color defaultColor = isBuy ? defaultColorBuy : defaultColorSell;
        return new ColorsConfigItem(color, defaultColor, c);
    }

    private void addLineStyleSettings(final BookmapSettingsPanel panel) {
        String[] lineStyles = Stream.of(LineStyle.values()).map(Object::toString).toArray(String[]::new);
        JComboBox<String> c = new JComboBox<>(lineStyles);
        setAlignment(c);
        c.setSelectedItem(settings.lineStyle.toString());
        c.setEditable(false);
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = c.getSelectedIndex();
                if (idx != settings.lineStyle.ordinal()) {
                    settings.lineStyle = LineStyle.values()[idx];
                    lineBuy.setLineStyle(settings.lineStyle);
                    lineSell.setLineStyle(settings.lineStyle);
                }
            }
        });
        panel.addSettingsItem("Line type:", c);
    }

    private void addLineWidthSettings(final BookmapSettingsPanel panel) {
        JComboBox<Integer> c = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5 });
        setAlignment(c);
        c.setSelectedItem(settings.lineWidth);
        c.setEditable(false);
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int newLineWidth = (int) c.getSelectedItem();
                if (newLineWidth != settings.lineWidth) {
                    settings.lineWidth = newLineWidth;
                    lineBuy.setWidth(settings.lineWidth);
                    lineSell.setWidth(settings.lineWidth);
                }
            }
        });
        panel.addSettingsItem("Line width:", c);
    }

    private StrategyPanel getParametersSettingsPanel() {
        BookmapSettingsPanel panel = new BookmapSettingsPanel("Settings");
        addMultiplierSettings(panel);
        addBarPeriodSettings(panel);
        addAtrNumBars(panel);
        addUpdateCondition(panel);
        addSwitchCondition(panel);
        addReloadCondition(panel);
        addResetButton(panel);
        return panel;
    }

    private void addMultiplierSettings(final BookmapSettingsPanel panel) {
        JComboBox<Double> c = new JComboBox<>(new Double[] { 0.5, 0.75, 1.0, 1.5, 2.0, 2.618, 3.0, 3.5, 5.0, 7.5, 10.0 });
        setAlignment(c);
        c.setSelectedItem(settings.multiplier);
        c.setEditable(true);
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                onSettingsUpdated(SettingsName.MULTIPLIER, c.getSelectedItem());
            }
        });
        panel.addSettingsItem("Multiplier:", c);
    }

    private void addBarPeriodSettings(final BookmapSettingsPanel panel) {
        JComboBox<Integer> c = new JComboBox<>(
                new Integer[] { 1, 2, 3, 5, 10, 15, 30, 60, 120, 180, 300, 600, 900, 1800, 3600 });
        setAlignment(c);
        int selected = (int) (settings.barPeriod / 1_000_000_000L);
        c.setSelectedItem(selected);
        c.setEditable(false);
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                onSettingsUpdated(SettingsName.BAR_PERIOD, c.getSelectedItem());
            }
        });
        panel.addSettingsItem("Bar period (reloads if changed!) [sec]:", c);
    }

    private void addAtrNumBars(final BookmapSettingsPanel panel) {
        JComboBox<Integer> c = new JComboBox<>(new Integer[] { 5, 10, 20, 50, 100 });
        setAlignment(c);
        c.setSelectedItem(settings.atrNumBars);
        c.setEditable(false);
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                onSettingsUpdated(SettingsName.NUM_BARS, c.getSelectedItem());
            }
        });
        panel.addSettingsItem("Number of Bars:", c);
    }

    private void addUpdateCondition(final BookmapSettingsPanel panel) {
        String[] updateConditions = Stream.of(UpdateCondition.values()).map(Object::toString).toArray(String[]::new);
        JComboBox<String> c = new JComboBox<>(updateConditions);
        setAlignment(c);
        c.setSelectedItem(settings.updateCondition.toString());
        c.setEditable(false);
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                onSettingsUpdated(SettingsName.UPDATE_CONDITION, c.getSelectedItem());
            }
        });
        panel.addSettingsItem("Update conditions:", c);
    }

    private void addSwitchCondition(final BookmapSettingsPanel panel) {
        JComboBox<Integer> c = new JComboBox<>(new Integer[] { 0, 1, 2, 3, 4, 5}) ;
        setAlignment(c);
        c.setSelectedItem(settings.switchCondition);
        c.setEditable(false);
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                onSettingsUpdated(SettingsName.SWITCH_CONDITION, c.getSelectedItem());
            }
        });
        panel.addSettingsItem("TS switch condition [ticks crossed]:", c);
    }
    
    private void addReloadCondition(final BookmapSettingsPanel panel) {
        JComboBox<Boolean> c = new JComboBox<>(new Boolean[] { true, false}) ;
        setAlignment(c);
        c.setSelectedItem(settings.reloadOnChange);
        c.setEditable(false);
        c.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                onSettingsUpdated(SettingsName.RELOAD_CONDITION, c.getSelectedItem());
            }
        });
        panel.addSettingsItem("Reload if changed:", c);
    }

    private void addResetButton(final BookmapSettingsPanel panel) {
        JButton c = new JButton("Restore");
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                settings.multiplier = defaultMultiplier;
                settings.barPeriod = defaultBarPeriod;
                settings.atrNumBars = defaultAtrNumBars;
                settings.updateCondition = defaultUpdateCondition;
                settings.switchCondition = defaultSwitchCondition;
                onSettingsUpdated(SettingsName.ALL, null);
            }
        });
        panel.addSettingsItem("", c);
    }

    private BookmapSettingsPanel getStatisticsPanel() {
        BookmapSettingsPanel panel = new BookmapSettingsPanel("Current values");
        panel.addSettingsItem("Last True Range (multiplied) [ticks]:", labelTr);
        panel.addSettingsItem("Average True Range [ticks]:", labelAtr);
        return panel;
    }

    private void setAlignment(final JComboBox<?> c) {
        ((JLabel)c.getRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }
}
