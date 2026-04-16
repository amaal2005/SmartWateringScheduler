package com.watering.gui;

import com.watering.data.DataLoader;
import com.watering.model.Plant;
import com.watering.perceptron.Perceptron;
import com.watering.sa.SimulatedAnnealing;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ControlPanel extends JPanel {

    private GardenPanel gardenPanel;

    private JButton loadButton;
    private JButton trainButton;
    private JButton predictButton;
    private JButton optimizeButton;
    private JButton resetButton;

    private JTextArea outputArea;

    private List<Plant> plants;
    private Perceptron perceptron;

    public ControlPanel(GardenPanel gardenPanel) {
        this.gardenPanel = gardenPanel;
        this.plants = new ArrayList<>();
        this.perceptron = new Perceptron(0.1);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Control Panel"));
        setPreferredSize(new Dimension(280, 700));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 1, 10, 10));

        loadButton = new JButton("Load Data");
        trainButton = new JButton("Train Perceptron");
        predictButton = new JButton("Predict Plants");
        optimizeButton = new JButton("Optimize Path");
        resetButton = new JButton("Reset");

        buttonPanel.add(loadButton);
        buttonPanel.add(trainButton);
        buttonPanel.add(predictButton);
        buttonPanel.add(optimizeButton);
        buttonPanel.add(resetButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        addActions();
    }

    private void addActions() {
        loadButton.addActionListener(e -> loadData());
        trainButton.addActionListener(e -> trainPerceptron());
        predictButton.addActionListener(e -> predictPlants());
        optimizeButton.addActionListener(e -> optimizePath());
        resetButton.addActionListener(e -> resetAll());
    }

    private void loadData() {
        plants = DataLoader.loadPlantsFromCSV("plants_data.csv");

        if (plants.isEmpty()) {
            outputArea.setText("No plants loaded.\n");
            return;
        }

        assignGridPositions(plants);
        gardenPanel.setPlants(plants);
        gardenPanel.setOptimizedPath(new ArrayList<>());
        gardenPanel.setRandomPath(new ArrayList<>());

        outputArea.setText("Data loaded successfully.\n");
        outputArea.append("Plants loaded: " + plants.size() + "\n");
        outputArea.append("Positions assigned in grid layout.\n");
    }

    private void trainPerceptron() {
        if (plants == null || plants.isEmpty()) {
            outputArea.setText("Please load data first.\n");
            return;
        }

        outputArea.append("\n--- Training ---\n");
        perceptron.train(plants, 100, outputArea);
        double accuracy = perceptron.calculateAccuracy(plants);

        outputArea.append("Training completed.\n");
        outputArea.append("Accuracy: " + String.format(java.util.Locale.US, "%.2f", accuracy) + "%\n");
    }

    private void predictPlants() {
        if (plants == null || plants.isEmpty()) {
            outputArea.setText("Please load data first.\n");
            return;
        }

        perceptron.predictAll(plants);
        gardenPanel.setPlants(plants);

        int count = 0;
        for (Plant plant : plants) {
            if (plant.getPredictedNeedsWater() == 1) {
                count++;
            }
        }

        outputArea.append("\n--- Prediction ---\n");
        outputArea.append("Prediction completed.\n");
        outputArea.append("Plants predicted to need water: " + count + "\n");
    }

    private void optimizePath() {
        if (plants == null || plants.isEmpty()) {
            outputArea.setText("Please load data first.\n");
            return;
        }

        List<Plant> allPlantsThatNeedWater = new ArrayList<>();
        for (Plant plant : plants) {
            if (plant.getPredictedNeedsWater() == 1) {
                allPlantsThatNeedWater.add(plant);
            }
        }

        if (allPlantsThatNeedWater.isEmpty()) {
            outputArea.append("\n--- Optimization ---\n");
            outputArea.append("No plants predicted to need water.\n");
            return;
        }

        List<Plant> selectedPlantsToWater = new ArrayList<>(allPlantsThatNeedWater);

        List<Plant> randomPath = new ArrayList<>(selectedPlantsToWater);
        Collections.shuffle(randomPath);

        SimulatedAnnealing sa = new SimulatedAnnealing(1000, 0.03);

        double randomCost = sa.calculateCost(randomPath, allPlantsThatNeedWater);

        List<Plant> optimizedOrder = sa.optimize(selectedPlantsToWater, allPlantsThatNeedWater);
        double optimizedCost = sa.calculateCost(optimizedOrder, allPlantsThatNeedWater);

        gardenPanel.setRandomPath(randomPath);
        gardenPanel.setOptimizedPath(optimizedOrder);

        double improvement = randomCost - optimizedCost;
        double improvementPercent = 0.0;
        if (randomCost != 0) {
            improvementPercent = (improvement / randomCost) * 100.0;
        }

        outputArea.append("\n--- Optimization ---\n");
        outputArea.append("Path optimization completed.\n");
        outputArea.append("Plants that need water: " + allPlantsThatNeedWater.size() + "\n");
        outputArea.append("Plants selected to water: " + selectedPlantsToWater.size() + "\n");
        outputArea.append("Random path cost: " + String.format(java.util.Locale.US, "%.2f", randomCost) + "\n");
        outputArea.append("Optimized path cost: " + String.format(java.util.Locale.US, "%.2f", optimizedCost) + "\n");
        outputArea.append("Improvement: " + String.format(java.util.Locale.US, "%.2f", improvement) + "\n");
        outputArea.append("Improvement percentage: " + String.format(java.util.Locale.US, "%.2f", improvementPercent) + "%\n");
    }

    private void resetAll() {
        plants = new ArrayList<>();
        perceptron = new Perceptron(0.1);

        gardenPanel.setPlants(plants);
        gardenPanel.setOptimizedPath(new ArrayList<>());
        gardenPanel.setRandomPath(new ArrayList<>());

        outputArea.setText("Reset completed.\n");
    }

    private void assignGridPositions(List<Plant> plants) {
        int cols = 10;
        int spacing = 8;

        for (int i = 0; i < plants.size(); i++) {
            int row = i / cols;
            int col = i % cols;

            double x = 5 + col * spacing;
            double y = 5 + row * spacing;

            plants.get(i).setPosition(x, y);
        }
    }
}