package com.watering.gui;

import com.watering.model.Plant;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GardenPanel extends JPanel {

    private static final double SCALE_FACTOR = 5.0;
    private static final int OFFSET = 30;
    private static final int PLANT_SIZE = 18;

    private List<Plant> plants;
    private List<Plant> optimizedPath;
    private List<Plant> randomPath;

    public GardenPanel() {
        this.plants = new ArrayList<>();
        this.optimizedPath = new ArrayList<>();
        this.randomPath = new ArrayList<>();
        setBackground(Color.WHITE);
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
        repaint();
    }

    public void setOptimizedPath(List<Plant> optimizedPath) {
        this.optimizedPath = optimizedPath;
        repaint();
    }

    public void setRandomPath(List<Plant> randomPath) {
        this.randomPath = randomPath;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawRandomPath(g2);
        drawOptimizedPath(g2);
        drawPlants(g2);
        drawLegend(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(230, 230, 230));

        for (int x = 0; x < getWidth(); x += 50) {
            g2.drawLine(x, 0, x, getHeight());
        }

        for (int y = 0; y < getHeight(); y += 50) {
            g2.drawLine(0, y, getWidth(), y);
        }
    }

    private void drawPlants(Graphics2D g2) {
        if (plants == null || plants.isEmpty()) {
            return;
        }

        g2.setFont(new Font("Arial", Font.BOLD, 11));

        for (int i = 0; i < plants.size(); i++) {
            Plant plant = plants.get(i);

            int x = (int) (plant.getX() * SCALE_FACTOR) + OFFSET;
            int y = (int) (plant.getY() * SCALE_FACTOR) + OFFSET;

            if (plant.getPredictedNeedsWater() == 1) {
                g2.setColor(Color.RED);
            } else {
                g2.setColor(Color.GREEN.darker());
            }

            g2.fillOval(x, y, PLANT_SIZE, PLANT_SIZE);

            g2.setColor(Color.BLACK);
            g2.drawOval(x, y, PLANT_SIZE, PLANT_SIZE);

            // نوع النبات داخل الدائرة
            String typeLabel = getPlantTypeShort(plant.getPlantType());
            g2.drawString(typeLabel, x + 5, y + 13);

            // رقم النبات خارج الدائرة
            g2.drawString(String.valueOf(i + 1), x + PLANT_SIZE + 2, y + 12);
        }
    }

    private void drawRandomPath(Graphics2D g2) {
        if (randomPath == null || randomPath.size() < 2) {
            return;
        }

        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(2));

        for (int i = 0; i < randomPath.size() - 1; i++) {
            Plant p1 = randomPath.get(i);
            Plant p2 = randomPath.get(i + 1);

            int x1 = (int) (p1.getX() * SCALE_FACTOR) + OFFSET + (PLANT_SIZE / 2);
            int y1 = (int) (p1.getY() * SCALE_FACTOR) + OFFSET + (PLANT_SIZE / 2);

            int x2 = (int) (p2.getX() * SCALE_FACTOR) + OFFSET + (PLANT_SIZE / 2);
            int y2 = (int) (p2.getY() * SCALE_FACTOR) + OFFSET + (PLANT_SIZE / 2);

            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawOptimizedPath(Graphics2D g2) {
        if (optimizedPath == null || optimizedPath.size() < 2) {
            return;
        }

        g2.setColor(Color.BLUE);
        g2.setStroke(new BasicStroke(2));

        for (int i = 0; i < optimizedPath.size() - 1; i++) {
            Plant p1 = optimizedPath.get(i);
            Plant p2 = optimizedPath.get(i + 1);

            int x1 = (int) (p1.getX() * SCALE_FACTOR) + OFFSET + (PLANT_SIZE / 2);
            int y1 = (int) (p1.getY() * SCALE_FACTOR) + OFFSET + (PLANT_SIZE / 2);

            int x2 = (int) (p2.getX() * SCALE_FACTOR) + OFFSET + (PLANT_SIZE / 2);
            int y2 = (int) (p2.getY() * SCALE_FACTOR) + OFFSET + (PLANT_SIZE / 2);

            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawLegend(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.BOLD, 12));

        // Needs water
        g2.setColor(Color.RED);
        g2.fillOval(10, 10, 12, 12);
        g2.setColor(Color.BLACK);
        g2.drawString("Needs Water", 28, 22);

        // Doesn't need water
        g2.setColor(Color.GREEN.darker());
        g2.fillOval(10, 30, 12, 12);
        g2.setColor(Color.BLACK);
        g2.drawString("Doesn't Need Water", 28, 42);

        // Optimized path
        g2.setColor(Color.BLUE);
        g2.drawLine(10, 58, 22, 58);
        g2.setColor(Color.BLACK);
        g2.drawString("Optimized Path", 28, 62);

        // Random path
        g2.setColor(Color.GRAY);
        g2.drawLine(10, 78, 22, 78);
        g2.setColor(Color.BLACK);
        g2.drawString("Random Path", 28, 82);

        // Plant types
        g2.drawString("Plant Types:", 10, 105);
        g2.drawString("C = Cactus", 10, 123);
        g2.drawString("F = Flower", 10, 141);
        g2.drawString("H = Herb", 10, 159);
    }

    private String getPlantTypeShort(int plantType) {
        return switch (plantType) {
            case 0 -> "C";
            case 1 -> "F";
            case 2 -> "H";
            default -> "?";
        };
    }
}