package com.watering.gui;

import com.watering.model.Plant;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GardenPanel extends JPanel {

    private static final double SCALE_FACTOR = 5.0;
    private static final int OFFSET = 40;
    private static final int HEADER_HEIGHT = 70;
    private static final int PLANT_SIZE = 34;

    private final Color bgColor = new Color(235, 240, 232);
    private final Color gridColor = new Color(208, 214, 205);
    private final Color pathBlue = new Color(69, 126, 190);
    private final Color okGreen = new Color(109, 158, 31);
    private final Color dangerRed = new Color(224, 79, 76);
    private final Color textGray = new Color(80, 80, 80);
    private final Color randomGray = new Color(145, 145, 145);

    private List<Plant> plants;
    private List<Plant> optimizedPath;
    private List<Plant> randomPath;

    private PlantListPanel plantListPanel;
    private JTextArea outputArea;

    public GardenPanel() {
        this.plants = new ArrayList<>();
        this.optimizedPath = new ArrayList<>();
        this.randomPath = new ArrayList<>();

        setBackground(bgColor);
        setBorder(new LineBorder(new Color(215, 215, 215), 1, true));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleGardenClick(e.getX(), e.getY());
            }
        });
    }

    public void setPlantListPanel(PlantListPanel plantListPanel) {
        this.plantListPanel = plantListPanel;
    }

    public void setOutputArea(JTextArea outputArea) {
        this.outputArea = outputArea;
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
        return new Dimension(820, 680);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawHeader(g2);
        drawGrid(g2);
        drawRandomPath(g2);
        drawOptimizedPath(g2);
        drawPlants(g2);
        drawBottomHint(g2);
    }

    private void handleGardenClick(int pixelX, int pixelY) {
        if (plants == null || plants.isEmpty()) {
            return;
        }

        Plant clickedPlant = findPlantAt(pixelX, pixelY);
        if (clickedPlant != null) {
            showPlantInfo(clickedPlant);
            return;
        }

        if (plantListPanel == null) {
            return;
        }

        Plant selectedPlant = plantListPanel.getSelectedPlant();
        if (selectedPlant == null) {
            JOptionPane.showMessageDialog(this,
                    "Select a plant from the left panel first.",
                    "No Plant Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (selectedPlant.isPlaced()) {
            JOptionPane.showMessageDialog(this,
                    "This plant is already placed.",
                    "Already Placed",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double logicalX = Math.max(0, (pixelX - OFFSET) / SCALE_FACTOR);
        double logicalY = Math.max(0, (pixelY - HEADER_HEIGHT) / SCALE_FACTOR);

        selectedPlant.setPosition(logicalX, logicalY);

        if (outputArea != null) {
            outputArea.append("✓ Placed " + selectedPlant.getPlantTypeName()
                    + " at (" + String.format(java.util.Locale.US, "%.1f", logicalX)
                    + ", "
                    + String.format(java.util.Locale.US, "%.1f", logicalY) + ")\n");
        }

        plantListPanel.refreshList();
        plantListPanel.clearSelection();
        repaint();
    }

    private Plant findPlantAt(int pixelX, int pixelY) {
        for (Plant plant : plants) {
            if (!plant.isPlaced()) {
                continue;
            }

            int x = (int) (plant.getX() * SCALE_FACTOR) + OFFSET;
            int y = (int) (plant.getY() * SCALE_FACTOR) + HEADER_HEIGHT;

            Rectangle r = new Rectangle(x, y, PLANT_SIZE, PLANT_SIZE);
            if (r.contains(pixelX, pixelY)) {
                return plant;
            }
        }
        return null;
    }

    private void showPlantInfo(Plant plant) {
        String status;
        if (plant.getPredictedNeedsWater() == -1) {
            status = "Not predicted yet";
        } else if (plant.getPredictedNeedsWater() == 1) {
            status = "Needs water";
        } else {
            status = "OK — no water needed";
        }

        String info = "Plant " + getPlantIndex(plant)
                + "\nType: " + plant.getPlantTypeName()
                + "\nSoil moisture: " + plant.getSoilMoisture()
                + "\nLast watered: " + plant.getLastWatered() + " h ago"
                + "\nPredicted status: " + status
                + "\nPosition: (" + String.format(java.util.Locale.US, "%.1f", plant.getX())
                + ", " + String.format(java.util.Locale.US, "%.1f", plant.getY()) + ")";

        JOptionPane.showMessageDialog(this, info, "Plant Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void drawHeader(Graphics2D g2) {
        g2.setColor(new Color(250, 250, 248));
        g2.fillRect(0, 0, getWidth(), 70);

        g2.setColor(new Color(210, 210, 210));
        g2.drawLine(0, 70, getWidth(), 70);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g2.setColor(new Color(50, 50, 50));
        g2.drawString("Garden", 26, 30);

        int topY = 28;
        int bottomY = 55;

        // Row 1: dots
        int x1 = 220;

        drawLegendDot(g2, new Color(150, 150, 150), x1, topY - 10);
        g2.setColor(new Color(50, 50, 50));
        g2.drawString("not predicted", x1 + 18, topY);

        drawLegendDot(g2, dangerRed, x1 + 170, topY - 10);
        g2.setColor(new Color(50, 50, 50));
        g2.drawString("needs water", x1 + 188, topY);

        drawLegendDot(g2, okGreen, x1 + 345, topY - 10);
        g2.setColor(new Color(50, 50, 50));
        g2.drawString("ok", x1 + 363, topY);

        // Row 2: lines
        int x2 = 320;

        g2.setStroke(new BasicStroke(3f));
        g2.setColor(pathBlue);
        g2.drawLine(x2, bottomY - 5, x2 + 30, bottomY - 5);
        g2.setColor(new Color(50, 50, 50));
        g2.drawString("optimized path", x2 + 40, bottomY);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(randomGray);
        g2.drawLine(x2 + 220, bottomY - 5, x2 + 250, bottomY - 5);
        g2.setColor(new Color(50, 50, 50));
        g2.drawString("random path", x2 + 260, bottomY);
    }

    private void drawLegendDot(Graphics2D g2, Color color, int x, int y) {
        g2.setColor(color);
        g2.fillOval(x, y, 14, 14);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(gridColor);

        for (int x = 0; x < getWidth(); x += 70) {
            g2.drawLine(x, HEADER_HEIGHT, x, getHeight());
        }

        for (int y = HEADER_HEIGHT; y < getHeight(); y += 70) {
            g2.drawLine(0, y, getWidth(), y);
        }
    }

    private void drawPlants(Graphics2D g2) {
        if (plants == null || plants.isEmpty()) {
            return;
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));

        for (int i = 0; i < plants.size(); i++) {
            Plant plant = plants.get(i);

            if (!plant.isPlaced()) {
                continue;
            }

            int x = (int) (plant.getX() * SCALE_FACTOR) + OFFSET;
            int y = (int) (plant.getY() * SCALE_FACTOR) + HEADER_HEIGHT;

            Color fillColor;
            if (plant.getPredictedNeedsWater() == -1) {
                fillColor = new Color(150, 150, 150); // neutral gray
            } else if (plant.getPredictedNeedsWater() == 1) {
                fillColor = dangerRed;
            } else {
                fillColor = okGreen;
            }

            g2.setColor(fillColor);
            g2.fillOval(x, y, PLANT_SIZE, PLANT_SIZE);

            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x, y, PLANT_SIZE, PLANT_SIZE);

            g2.setColor(Color.WHITE);
            String typeShort = plant.getPlantTypeShort();
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (PLANT_SIZE - fm.stringWidth(typeShort)) / 2;
            int ty = y + ((PLANT_SIZE - fm.getHeight()) / 2) + fm.getAscent() - 1;
            g2.drawString(typeShort, tx, ty);

            g2.setColor(textGray);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString("P" + (i + 1), x + 6, y + PLANT_SIZE + 18);

            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        }
    }

    private void drawRandomPath(Graphics2D g2) {
        if (randomPath == null || randomPath.size() < 2) {
            return;
        }

        g2.setColor(randomGray);
        g2.setStroke(new BasicStroke(2f));

        for (int i = 0; i < randomPath.size() - 1; i++) {
            Plant p1 = randomPath.get(i);
            Plant p2 = randomPath.get(i + 1);

            int x1 = (int) (p1.getX() * SCALE_FACTOR) + OFFSET + (PLANT_SIZE / 2);
            int y1 = (int) (p1.getY() * SCALE_FACTOR) + HEADER_HEIGHT + (PLANT_SIZE / 2);

            int x2 = (int) (p2.getX() * SCALE_FACTOR) + OFFSET + (PLANT_SIZE / 2);
            int y2 = (int) (p2.getY() * SCALE_FACTOR) + HEADER_HEIGHT + (PLANT_SIZE / 2);

            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawOptimizedPath(Graphics2D g2) {
        if (optimizedPath == null || optimizedPath.size() < 2) {
            return;
        }

        Stroke dashed = new BasicStroke(
                3f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND,
                0,
                new float[]{8f, 6f},
                0
        );

        g2.setColor(pathBlue);
        g2.setStroke(dashed);

        for (int i = 0; i < optimizedPath.size() - 1; i++) {
            Plant p1 = optimizedPath.get(i);
            Plant p2 = optimizedPath.get(i + 1);

            int x1 = (int) (p1.getX() * SCALE_FACTOR) + OFFSET + (PLANT_SIZE / 2);
            int y1 = (int) (p1.getY() * SCALE_FACTOR) + HEADER_HEIGHT + (PLANT_SIZE / 2);

            int x2 = (int) (p2.getX() * SCALE_FACTOR) + OFFSET + (PLANT_SIZE / 2);
            int y2 = (int) (p2.getY() * SCALE_FACTOR) + HEADER_HEIGHT + (PLANT_SIZE / 2);

            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawBottomHint(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.setColor(new Color(120, 120, 120));
        g2.drawString("click any plant to see its features", getWidth() - 280, getHeight() - 20);
    }

    private int getPlantIndex(Plant plant) {
        for (int i = 0; i < plants.size(); i++) {
            if (plants.get(i) == plant) {
                return i + 1;
            }
        }
        return 0;
    }
}