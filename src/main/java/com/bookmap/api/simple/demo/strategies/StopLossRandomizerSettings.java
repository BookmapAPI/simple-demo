package com.bookmap.api.simple.demo.strategies;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.bookmap.api.simple.demo.utils.gui.BookmapSettingsPanel;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.settings.StrategySettingsVersion;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.CustomModule;
import velox.api.layer1.simplified.CustomSettingsPanelProvider;
import velox.api.layer1.simplified.InitialState;
import velox.gui.StrategyPanel;

@Layer1SimpleAttachable
@Layer1StrategyName("Stop Loss Randomizer")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class StopLossRandomizerSettings implements CustomModule, CustomSettingsPanelProvider {

    @StrategySettingsVersion(currentVersion = 1, compatibleVersions = {})
    public static class Settings {
/*        
        public static class Range {
            public int distance;
            public int closer;
            public int farther;

            public Range(int distance, int closer, int farther) {
                this.distance = distance;
                this.closer = closer;
                this.farther = farther;
            }
        }
        */
        public int numRows = 3;

        //public ArrayList<Range> items = new ArrayList<>();
    }

    private Api api;
    private Settings settings;

    BookmapSettingsPanel panel;
    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        this.api = api;
        settings = api.getSettings(Settings.class);
    }

    @Override
    public StrategyPanel[] getCustomSettingsPanels() {
        updatePanel();
        return new StrategyPanel[] { panel };
    }

    private void updatePanel() {
        panel = new BookmapSettingsPanel("Stop Loss randomization range");
        panel.addSettingsItem(new JLabel("Original"), new JLabel("Closer"), new JLabel("Further"));
        for (int i = 0; i < settings.numRows; i++) {
            JComboBox<Integer> c1 = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5 });
            JComboBox<Integer> c2 = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5 });
            JComboBox<Integer> c3 = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5 });
            panel.addSettingsItem(c1, c2, c3);
        }
        JButton c = new JButton("Add");
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                settings.numRows++;
                updatePanel();
                panel.getParent().repaint();
                panel.getParent().revalidate();
            }
        });
        panel.addSettingsItem(new JLabel(""), new JLabel(""), c);
    }

    @Override
    public void stop() {
        api.setSettings(settings);
    }
}
