package com.watering.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private GardenPanel gardenPanel;
    private ControlPanel controlPanel;

    public MainFrame() {
        setTitle("Smart Plant Watering Scheduler - AI Based System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1200, 750);
        setLocationRelativeTo(null);

        try {
            setIconImage(
                    Toolkit.getDefaultToolkit()
                            .getImage(getClass().getResource("/icon.png"))
            );
        } catch (Exception e) {
            // ignore if icon not found
        }

        gardenPanel = new GardenPanel();
        controlPanel = new ControlPanel(gardenPanel);

        controlPanel.setPreferredSize(new Dimension(300, 750));

        add(gardenPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        setVisible(true);
    }
}