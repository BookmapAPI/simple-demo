package com.bookmap.sergey.custommodules.guiutils;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import velox.gui.StrategyPanel;

public class SettingsPanelBase extends StrategyPanel {

    private static final long serialVersionUID = -2829435001075847741L;

    private static final int xgap = 10;
    private static final int ygap = 15;

    public SettingsPanelBase(String title) {
        super(title);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createRigidArea(new Dimension(0, ygap)));
    }

    public void addSettingsRow(Component c) {
        JPanel panel = createPanel(c);
        panel.add(Box.createRigidArea(new Dimension(xgap, 0)));
        add(panel);
        add(Box.createRigidArea(new Dimension(0, ygap)));
    }

    public void addSettingsRow(String label, Component c) {
        JPanel panel = createPanel(new JLabel(label));
        panel.add(c);
        panel.add(Box.createRigidArea(new Dimension(xgap, 0)));
        add(panel);
        add(Box.createRigidArea(new Dimension(0, ygap)));
    }

    private JPanel createPanel(Component c) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(Box.createRigidArea(new Dimension(xgap, 0)));
        panel.add(c);
        panel.add(Box.createHorizontalGlue());
        return panel;
    }

    public static void main(String[] args) {
        SettingsPanelBase panel = new SettingsPanelBase("Settings");
        panel.addSettingsRow(new JLabel("short label"));
        for (int i = 0; i < 5; i++) {
            JComponent c = new JButton("Button " + i);
            panel.addSettingsRow("Settings " + i, c);
        }
        panel.addSettingsRow(new JLabel("long label: pojki0woerjjvrvi9wjijgvi845jg9038g283532984jbgvokivl,opk,op0["));

        JFrame frame = new JFrame("Test settings layout");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
