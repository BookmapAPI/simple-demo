package com.bookmap.api.simple.demo.utils.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

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

    public void addSettingsItem(Component left, Component center, Component right) {
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row++;
        constraints.gridwidth = 1;
        constraints.gridx = 1;
        constraints.insets = new Insets(0, 0, ygap, 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        if (row == 1) {
            constraints.insets.top = ygap;
        }
        
        add(left, constraints);
        
        constraints.gridx = 2;
        constraints.insets = new Insets(0, xgap, ygap, 0);
        add(center, constraints);
        
        constraints.gridx = 3;
        constraints.insets = new Insets(0, xgap, ygap, 0);
        add(right, constraints);
    }

    protected static void testSettingsPanel() {
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
    
    protected static void testExp() {
        int n = 100;
        double[] data = new double[n * 100];
        Random r = new Random();
        for (int i = 0; i < data.length; i++) {
            data[i] = r.nextDouble();
        }
        double s1 = 0;
        for (int i = 0; i < n; i++) {
            s1 += data[i]; 
        }
        double s2 = 0;
        for (int i = 0; i < data.length; i++) {
            s2 += data[i] * Math.exp(-i * Math.log(2) / n);
        }
        s2 *= Math.log(2);
        System.out.println(String.format("sum: %.4f, exp: %.4f", s1, s2));
    }
    
    public static void main(String[] args) {
        //testSettingsPanel();
        testExp();
    }
}
