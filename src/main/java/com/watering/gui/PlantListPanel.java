package com.watering.gui;

import com.watering.model.Plant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PlantListPanel extends JPanel {

    private DefaultListModel<Plant> listModel;
    private JList<Plant> plantJList;
    private List<Plant> plants;

    private final Color panelBg = new Color(248, 248, 246);
    private final Color cardBg = new Color(250, 250, 250);
    private final Color borderColor = new Color(215, 215, 215);
    private final Color okGreen = new Color(109, 158, 31);
    private final Color dangerRed = new Color(224, 79, 76);

    public PlantListPanel() {
        this.plants = new ArrayList<>();

        setLayout(new BorderLayout(0, 12));
        setBackground(panelBg);
        setBorder(new LineBorder(borderColor, 1, true));
        setPreferredSize(new Dimension(330, 700));

        JLabel titleLabel = new JLabel("PLANTS (FROM EXCEL)");
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
        titleLabel.setForeground(new Color(60, 60, 60));
        titleLabel.setBorder(new EmptyBorder(18, 18, 0, 18));

        listModel = new DefaultListModel<>();
        plantJList = new JList<>(listModel);
        plantJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        plantJList.setCellRenderer(new PlantCellRenderer());
        plantJList.setBackground(panelBg);
        plantJList.setFixedCellHeight(92);

        JScrollPane scrollPane = new JScrollPane(plantJList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(panelBg);

        JLabel helpLabel = new JLabel("<html><center>select a plant, then click on the garden to place it</center></html>");
        helpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        helpLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        helpLabel.setForeground(new Color(110, 110, 110));
        helpLabel.setBorder(new EmptyBorder(8, 12, 14, 12));

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(helpLabel, BorderLayout.SOUTH);
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
        refreshList();
    }

    public void refreshList() {
        listModel.clear();
        for (Plant plant : plants) {
            listModel.addElement(plant);
        }
    }

    public Plant getSelectedPlant() {
        return plantJList.getSelectedValue();
    }

    public void clearSelection() {
        plantJList.clearSelection();
    }

    private class PlantCellRenderer extends JPanel implements ListCellRenderer<Plant> {

        private final JLabel titleLabel = new JLabel();
        private final JLabel subLabel = new JLabel();

        public PlantCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Plant> list,
                                                      Plant plant,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {

            removeAll();

            JPanel card = new RoundedPanel(18);
            card.setLayout(new BorderLayout(12, 0));
            card.setBackground(cardBg);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(210, 210, 210), 1, true),
                    new EmptyBorder(10, 14, 10, 14)
            ));

            JPanel dotHolder = new JPanel(new GridBagLayout());
            dotHolder.setOpaque(false);

            JPanel dot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (plant.getPredictedNeedsWater() == -1) {
                        g.setColor(new Color(150, 150, 150));
                    } else if (plant.getPredictedNeedsWater() == 1) {
                        g.setColor(dangerRed);
                    } else {
                        g.setColor(okGreen);
                    }
                    g.fillOval(0, 0, 18, 18);
                }
            };
            dot.setPreferredSize(new Dimension(18, 18));
            dot.setOpaque(false);
            dotHolder.add(dot);

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);

            titleLabel.setText("Plant " + (index + 1));
            titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
            titleLabel.setForeground(new Color(40, 40, 40));

            subLabel.setText(plant.getPlantTypeName());
            subLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            subLabel.setForeground(new Color(70, 70, 70));

            textPanel.add(titleLabel);
            textPanel.add(subLabel);

            card.add(dotHolder, BorderLayout.WEST);
            card.add(textPanel, BorderLayout.CENTER);

            setLayout(new BorderLayout());
            setBackground(panelBg);
            add(card, BorderLayout.CENTER);

            if (isSelected) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(130, 170, 220), 2, true),
                        new EmptyBorder(9, 13, 9, 13)
                ));
            }

            return this;
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}