package com.watering.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    private GardenPanel     gardenPanel;
    private ControlPanel    controlPanel;
    private PlantListPanel  plantListPanel;
    private PerceptronPanel perceptronPanel;
    private SAPanel saPanel;

    /** The tabbed pane is package-accessible so ControlPanel can switch tabs */
    JTabbedPane tabbedPane;

    public MainFrame() {
        setTitle("Smart Plant Watering Scheduler - AI Based System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 820);
        setLocationRelativeTo(null);

        try {
            setIconImage(
                    Toolkit.getDefaultToolkit()
                            .getImage(getClass().getResource("/icon.png"))
            );
        } catch (Exception e) {
            // ignore if icon not found
        }

        // ── create panels ─────────────────────────────────────────────────
        gardenPanel     = new GardenPanel();
        plantListPanel  = new PlantListPanel();
        perceptronPanel = new PerceptronPanel();
        saPanel = new SAPanel();

        // ControlPanel needs references to garden + list + perceptron panels
        controlPanel = new ControlPanel(gardenPanel, plantListPanel, perceptronPanel, this);

        gardenPanel.setPlantListPanel(plantListPanel);

        // ── Garden tab: [plantList | garden] ─────────────────────────────
        JPanel gardenTab = new JPanel(new BorderLayout(14, 0));
        gardenTab.setBackground(new Color(245, 245, 242));
        gardenTab.setBorder(new EmptyBorder(0, 0, 0, 0));
        gardenTab.add(plantListPanel, BorderLayout.WEST);
        gardenTab.add(gardenPanel,    BorderLayout.CENTER);

        // ── tabbed pane ───────────────────────────────────────────────────
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 15));
        tabbedPane.addTab("🌿  Garden",      gardenTab);
        tabbedPane.addTab("📈  Perceptron",  perceptronPanel);
        tabbedPane.addTab("🔥  SA Optimizer", saPanel);

        // ── root layout: [tabbedPane | controlPanel] ──────────────────────
        JPanel rootPanel = new JPanel(new BorderLayout(16, 0));
        rootPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        rootPanel.setBackground(new Color(245, 245, 242));
        rootPanel.add(tabbedPane,    BorderLayout.CENTER);
        rootPanel.add(controlPanel,  BorderLayout.EAST);

        setContentPane(rootPanel);
        setVisible(true);
    }
    public SAPanel getSaPanel() { return saPanel; }

}