package com.bookmap.sergey.custommodules.guiutils;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import velox.gui.StrategyPanel;

public class BookmapSettingsPanel extends StrategyPanel {

    private static final long serialVersionUID = -2829435001075847741L;

    private static final int xgap = 10;
    private static final int ygap = 15;
    
    private int row = 0;

    public BookmapSettingsPanel(String title) {
        super(title);

        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[] {xgap, 0, xgap, 0, xgap};
        layout.columnWeights = new double[] {0, 1, 0, 1, 0};
        setLayout(layout);
    }

    public void addSettingsItem(Component c) {
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row++;
        constraints.gridwidth = 3;
        constraints.gridx = 1;
        constraints.insets = new Insets(0, 0, ygap, 0);
        constraints.anchor = GridBagConstraints.WEST;
        
        if (row == 1) {
            constraints.insets.top = ygap;
        }
        
        add(c, constraints);
    }

    public void addSettingsItem(String label, Component c) {
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row++;
        constraints.gridwidth = 1;
        constraints.gridx = 1;
        constraints.insets = new Insets(0, 0, ygap, 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        if (row == 1) {
            constraints.insets.top = ygap;
        }
        
        add(new JLabel(label), constraints);
        
        constraints.gridx = 3;
        add(c, constraints);
    }

    public static void main(String[] args) {
        BookmapSettingsPanel panel = new BookmapSettingsPanel("Settings");
        panel.addSettingsItem(new JLabel("short label"));
        for (int i = 0; i < 8; i++) {
            JComponent c = new JButton("Button " + i);
            if (i % 4 == 0) {
                c = new JButton("Button ojouijhuiorehw9uwghbt8i " + i);
            } else if (i % 2 == 0) {
                c = new JButton("" + i);
            }
            panel.addSettingsItem("Settings " + i, c);
        }
        panel.addSettingsItem(new JLabel("long label: pojki0woerjjvrvi9wjijgvi845jg9038g283532984jbgvokivl,opk,op0["));

        JFrame frame = new JFrame("Test settings layout");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
