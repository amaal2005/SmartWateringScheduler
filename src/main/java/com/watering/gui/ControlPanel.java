package com.watering.gui;

import com.watering.data.DataLoader;
import com.watering.model.Plant;
import com.watering.perceptron.Perceptron;
import com.watering.sa.SimulatedAnnealing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ControlPanel extends JPanel {

    private final GardenPanel     gardenPanel;
    private final PlantListPanel  plantListPanel;
    private final PerceptronPanel perceptronPanel;
    private final MainFrame       mainFrame;

    private JButton loadButton;
    private JButton trainButton;   // labelled "Learn Perceptron"
    private JButton predictButton;
    private JButton optimizeButton;
    private JButton resetButton;

    private JTextArea outputArea;

    private JLabel totalPlantsValue;
    private JLabel needWaterValue;
    private JLabel accuracyValue;
    private JLabel pathCostValue;

    private List<Plant> plants;
    private Perceptron  perceptron;

    private double lastAccuracy = 0.0;
    private double lastPathCost = 0.0;

    private final Color panelBg     = new Color(248, 248, 246);
    private final Color boxBg       = new Color(250, 250, 250);
    private final Color borderColor = new Color(214, 214, 214);
    private final Color green       = new Color(109, 158,  31);
    private final Color red         = new Color(224,  79,  76);
    private final Color blue        = new Color( 69, 126, 190);

    public ControlPanel(GardenPanel gardenPanel,
                        PlantListPanel plantListPanel,
                        PerceptronPanel perceptronPanel,
                        MainFrame mainFrame) {
        this.gardenPanel     = gardenPanel;
        this.plantListPanel  = plantListPanel;
        this.perceptronPanel = perceptronPanel;
        this.mainFrame       = mainFrame;
        this.plants          = new ArrayList<>();
        this.perceptron      = new Perceptron(0.1);

        setLayout(new BorderLayout());
        setBackground(panelBg);
        setBorder(new LineBorder(borderColor, 1, true));
        setPreferredSize(new Dimension(340, 700));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(panelBg);
        content.setBorder(new EmptyBorder(18, 18, 18, 18));

        content.add(createSectionTitle("ACTIONS"));
        content.add(Box.createVerticalStrut(10));
        content.add(createActionsPanel());

        content.add(Box.createVerticalStrut(14));
        content.add(createSectionTitle("RESULTS"));
        content.add(Box.createVerticalStrut(10));
        content.add(createResultsPanel());

        content.add(Box.createVerticalStrut(14));
        content.add(createSectionTitle("LOG"));
        content.add(Box.createVerticalStrut(10));
        content.add(createLogPanel());

        add(content, BorderLayout.CENTER);

        gardenPanel.setOutputArea(outputArea);

        addActions();
        updateResults();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  UI builders
    // ═══════════════════════════════════════════════════════════════════════

    private JLabel createSectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.PLAIN, 18));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }

    private JPanel createActionsPanel() {
        JPanel panel = createBoxPanel();
        panel.setLayout(new GridLayout(5, 1, 0, 10));

        loadButton     = createStyledButton("Load data");
        trainButton    = createStyledButton("Learn Perceptron");   // ← renamed
        predictButton  = createStyledButton("Predict all plants");
        optimizeButton = createStyledButton("Optimize path");
        resetButton    = createStyledButton("Reset");

        // highlight the Learn button slightly to draw attention
        trainButton.setBackground(new Color(232, 242, 255));
        trainButton.setForeground(blue);

        panel.add(loadButton);
        panel.add(trainButton);
        panel.add(predictButton);
        panel.add(optimizeButton);
        panel.add(resetButton);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = createBoxPanel();
        panel.setLayout(new GridLayout(4, 2, 0, 10));

        panel.add(createResultLabel("Total plants"));
        totalPlantsValue = createResultValue("0", Color.BLACK);
        panel.add(totalPlantsValue);

        panel.add(createResultLabel("Need water"));
        needWaterValue = createResultValue("0", red);
        panel.add(needWaterValue);

        panel.add(createResultLabel("Accuracy"));
        accuracyValue = createResultValue("0%", green);
        panel.add(accuracyValue);

        panel.add(createResultLabel("Path cost"));
        pathCostValue = createResultValue("0.0", Color.BLACK);
        panel.add(pathCostValue);

        return panel;
    }

    private JScrollPane createLogPanel() {
        outputArea = new JTextArea(20, 22);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        outputArea.setBackground(boxBg);
        outputArea.setForeground(new Color(60, 60, 60));
        outputArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(new LineBorder(borderColor, 1, true));
        scrollPane.getViewport().setBackground(boxBg);
        scrollPane.setPreferredSize(new Dimension(280, 360));
        return scrollPane;
    }

    private JPanel createBoxPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(boxBg);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.PLAIN, 16));
        button.setBackground(new Color(249, 249, 249));
        button.setBorder(new LineBorder(new Color(180, 180, 180), 1, true));
        button.setPreferredSize(new Dimension(220, 44));
        return button;
    }

    private JLabel createResultLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }

    private JLabel createResultValue(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        label.setForeground(color);
        return label;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Action wiring
    // ═══════════════════════════════════════════════════════════════════════

    private void addActions() {
        loadButton    .addActionListener(e -> loadData());
        trainButton   .addActionListener(e -> trainPerceptron());
        predictButton .addActionListener(e -> predictPlants());
        optimizeButton.addActionListener(e -> optimizePath());
        resetButton   .addActionListener(e -> resetAll());
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Business logic
    // ═══════════════════════════════════════════════════════════════════════

    private void loadData() {
        plants = DataLoader.loadPlantsFromCSV("plants_data.csv");

        if (plants.isEmpty()) {
            outputArea.setText("No plants loaded.\n");
            updateResults();
            return;
        }

        for (Plant plant : plants) {
            plant.clearPosition();
            plant.setPredictedNeedsWater(-1);
        }

        lastAccuracy = 0.0;
        lastPathCost = 0.0;

        plantListPanel.setPlants(plants);
        gardenPanel.setPlants(plants);
        gardenPanel.setOptimizedPath(new ArrayList<>());
        gardenPanel.setRandomPath(new ArrayList<>());

        outputArea.setText("");
        outputArea.append("✓ Data loaded successfully.\n");
        outputArea.append("✓ Plants loaded: " + plants.size() + "\n");
        outputArea.append("→ Select plants from the left panel and click the garden to place them.\n");

        updateResults();
    }

    /**
     * Trains the perceptron, collects epoch history, passes it to the
     * PerceptronPanel, then automatically switches to the Perceptron tab.
     */
    private void trainPerceptron() {
        if (plants == null || plants.isEmpty()) {
            outputArea.setText("Please load data first.\n");
            return;
        }

        outputArea.append("\n✓ Training started...\n");

        // Use trainWithHistory so the chart gets populated
        List<Perceptron.EpochRecord> history =
                perceptron.trainWithHistory(plants, 100, outputArea);

        lastAccuracy = perceptron.calculateAccuracy(plants);

        outputArea.append("✓ Training completed.\n");
        outputArea.append("→ Accuracy: "
                + String.format(java.util.Locale.US, "%.2f", lastAccuracy) + "%\n");

        // Push results to the Perceptron tab
        perceptronPanel.showTrainingResults(history, perceptron);

        // Switch to Perceptron tab automatically
        mainFrame.tabbedPane.setSelectedIndex(1);

        plantListPanel.refreshList();
        gardenPanel.repaint();
        updateResults();
    }

    private void predictPlants() {
        if (plants == null || plants.isEmpty()) {
            outputArea.setText("Please load data first.\n");
            return;
        }

        perceptron.predictAll(plants);
        gardenPanel.setPlants(plants);
        plantListPanel.refreshList();

        int count = 0;
        for (Plant plant : plants) {
            if (plant.getPredictedNeedsWater() == 1) count++;
        }

        outputArea.append("\n✓ Predictions complete.\n");
        outputArea.append("→ Plants predicted to need water: " + count + "\n");

        updateResults();
    }

    private void optimizePath() {
        if (plants == null || plants.isEmpty()) {
            outputArea.setText("Please load data first.\n");
            return;
        }

        List<Plant> placedPlants = new ArrayList<>();
        for (Plant plant : plants) {
            if (plant.isPlaced()) placedPlants.add(plant);
        }

        if (placedPlants.isEmpty()) {
            outputArea.append("\nPlease place at least one plant on the garden first.\n");
            return;
        }

        List<Plant> allPlantsThatNeedWater = new ArrayList<>();
        for (Plant plant : plants) {
            if (plant.getPredictedNeedsWater() == 1) allPlantsThatNeedWater.add(plant);
        }

        List<Plant> selectedPlantsToWater = new ArrayList<>();
        for (Plant plant : placedPlants) {
            if (plant.getPredictedNeedsWater() == 1) selectedPlantsToWater.add(plant);
        }

        if (selectedPlantsToWater.isEmpty()) {
            outputArea.append("\nNo placed plants are predicted to need water.\n");
            outputArea.append("Placed plants: " + placedPlants.size() + "\n");
            return;
        }

        List<Plant> randomPath = new ArrayList<>(selectedPlantsToWater);
        Collections.shuffle(randomPath);

        SimulatedAnnealing sa = new SimulatedAnnealing(1000, 0.03);

        double randomCost = sa.calculateCost(randomPath, allPlantsThatNeedWater);

        List<Plant> optimizedOrder = sa.optimize(selectedPlantsToWater, allPlantsThatNeedWater);
        double optimizedCost = sa.calculateCost(optimizedOrder, allPlantsThatNeedWater);
        lastPathCost = optimizedCost;

        gardenPanel.setRandomPath(randomPath);
        gardenPanel.setOptimizedPath(optimizedOrder);

        double improvement = randomCost - optimizedCost;
        double improvementPercent = (randomCost != 0)
                ? (improvement / randomCost) * 100.0 : 0.0;

        outputArea.append("\n✓ SA optimizing...\n");
        outputArea.append("→ Gray line = random initial path.\n");
        outputArea.append("→ Blue dashed line = optimized path.\n");
        outputArea.append("✓ Path optimized.\n");
        outputArea.append("→ Random path cost: "
                + String.format(java.util.Locale.US, "%.2f", randomCost) + "\n");
        outputArea.append("→ Optimized cost: "
                + String.format(java.util.Locale.US, "%.2f", optimizedCost) + "\n");
        outputArea.append("→ Improvement: "
                + String.format(java.util.Locale.US, "%.2f", improvementPercent) + "%\n");

        // Switch back to Garden tab so user sees the path
        mainFrame.tabbedPane.setSelectedIndex(0);

        updateResults();
    }

    private void resetAll() {
        plants     = new ArrayList<>();
        perceptron = new Perceptron(0.1);
        lastAccuracy = 0.0;
        lastPathCost = 0.0;

        plantListPanel.setPlants(plants);
        gardenPanel.setPlants(plants);
        gardenPanel.setOptimizedPath(new ArrayList<>());
        gardenPanel.setRandomPath(new ArrayList<>());

        outputArea.setText("Reset completed.\n");
        updateResults();
    }

    private void updateResults() {
        int totalPlants = plants == null ? 0 : plants.size();

        int needWater = 0;
        if (plants != null) {
            for (Plant plant : plants) {
                if (plant.getPredictedNeedsWater() == 1) needWater++;
            }
        }

        totalPlantsValue.setText(String.valueOf(totalPlants));
        needWaterValue.setText(String.valueOf(needWater));
        accuracyValue.setText(
                String.format(java.util.Locale.US, "%.0f%%", lastAccuracy));
        pathCostValue.setText(
                String.format(java.util.Locale.US, "%.1f", lastPathCost));
    }
}