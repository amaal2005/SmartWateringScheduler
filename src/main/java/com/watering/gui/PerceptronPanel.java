package com.watering.gui;

import com.watering.model.Plant;
import com.watering.perceptron.Perceptron;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * PerceptronPanel — shown in the "Perceptron" tab.
 *
 * Layout (BorderLayout):
 *  CENTER  → JFreeChart learning curve (Errors + Accuracy vs Epoch)
 *  EAST    → Test-the-Perceptron panel (3 inputs + Predict button + result)
 *  SOUTH   → thin status bar
 */
public class PerceptronPanel extends JPanel {

    // ── colours matching the rest of the app ──────────────────────────────
    private static final Color BG        = new Color(245, 245, 242);
    private static final Color BOX_BG    = new Color(250, 250, 250);
    private static final Color BORDER_C  = new Color(214, 214, 214);
    private static final Color GREEN     = new Color(109, 158, 31);
    private static final Color RED       = new Color(224, 79,  76);
    private static final Color BLUE      = new Color(69,  126, 190);
    private static final Color TEXT_DARK = new Color(50,  50,  50);
    private static final Color TEXT_MED  = new Color(70,  70,  70);

    // ── chart data series ─────────────────────────────────────────────────
    private final XYSeries errorSeries    = new XYSeries("Loss (Errors per epoch)");
    private final XYSeries accuracySeries = new XYSeries("Accuracy (%)");

    // ── test-panel widgets ────────────────────────────────────────────────
    private JSpinner soilSpinner;
    private JSpinner lastWateredSpinner;
    private JComboBox<String> plantTypeCombo;
    private JLabel predictionResultLabel;

    // ── summary labels shown above the chart ─────────────────────────────
    private JLabel epochsLabel;
    private JLabel finalAccLabel;
    private JLabel convergedLabel;

    // ── status bar at the bottom ──────────────────────────────────────────
    private JLabel statusLabel;

    // ── reference to perceptron (set from ControlPanel after training) ────
    private Perceptron perceptron;

    // ── ─────────────────────────────────────────────────────────────────
    public PerceptronPanel() {
        setLayout(new BorderLayout(14, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildChartArea(),  BorderLayout.CENTER);
        add(buildTestPanel(),  BorderLayout.EAST);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ═════════════════════════════════════════════════════════════════════
    //  PUBLIC API — called from ControlPanel
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Feed the training history into the chart and update summary labels.
     * Called right after trainWithHistory() finishes.
     */
    public void showTrainingResults(List<Perceptron.EpochRecord> history,
                                    Perceptron perceptronRef) {
        this.perceptron = perceptronRef;

        // Clear old data
        errorSeries.clear();
        accuracySeries.clear();

        int lastEpoch = 0;
        double lastAcc = 0;
        boolean converged = false;

        for (Perceptron.EpochRecord r : history) {
            errorSeries.add(r.epoch, r.errors);
            accuracySeries.add(r.epoch, r.accuracy);
            lastEpoch = r.epoch;
            lastAcc   = r.accuracy;
            if (r.errors == 0) converged = true;
        }

        epochsLabel.setText("Epochs run: " + lastEpoch);
        finalAccLabel.setText(String.format("Final accuracy: %.1f%%", lastAcc));
        convergedLabel.setText(converged ? "✓ Converged" : "⚠ Did not converge");
        convergedLabel.setForeground(converged ? GREEN : RED);

        statusLabel.setText("Training complete — " + lastEpoch + " epochs | "
                + String.format(java.util.Locale.US, "%.1f", lastAcc) + "% accuracy");
    }

    // ═════════════════════════════════════════════════════════════════════
    //  BUILD helpers
    // ═════════════════════════════════════════════════════════════════════

    /** LEFT/CENTER: summary row + JFreeChart */
    private JPanel buildChartArea() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(BG);

        wrapper.add(buildSummaryRow(), BorderLayout.NORTH);
        wrapper.add(buildChart(),      BorderLayout.CENTER);

        return wrapper;
    }

    /** Three summary labels in a horizontal row above the chart */
    private JPanel buildSummaryRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        row.setBackground(BG);

        epochsLabel    = summaryLabel("Epochs run: —");
        finalAccLabel  = summaryLabel("Final accuracy: —");
        convergedLabel = summaryLabel("Not trained yet");
        convergedLabel.setForeground(new Color(150, 150, 150));

        row.add(epochsLabel);
        row.add(summaryDivider());
        row.add(finalAccLabel);
        row.add(summaryDivider());
        row.add(convergedLabel);

        return row;
    }

    private JLabel summaryLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 15));
        l.setForeground(TEXT_MED);
        return l;
    }

    private JLabel summaryDivider() {
        JLabel l = new JLabel("|");
        l.setFont(new Font("SansSerif", Font.PLAIN, 15));
        l.setForeground(BORDER_C);
        return l;
    }

    /** The JFreeChart panel — dual Y-axis: errors (left) + accuracy (right) */
    private ChartPanel buildChart() {
        XYSeriesCollection errDataset = new XYSeriesCollection(errorSeries);
        XYSeriesCollection accDataset = new XYSeriesCollection(accuracySeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Perceptron Learning Curve",
                "Epoch",
                "Errors",
                errDataset
        );

        chart.setBackgroundPaint(BOX_BG);
        chart.getTitle().setFont(new Font("SansSerif", Font.PLAIN, 16));
        chart.getTitle().setPaint(TEXT_DARK);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(252, 252, 252));
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlinePaint(BORDER_C);

        // ── renderer for errors (left axis, red) ─────────────────────────
        XYLineAndShapeRenderer errRenderer = new XYLineAndShapeRenderer(true, true);
        errRenderer.setSeriesPaint(0, RED);
        errRenderer.setSeriesStroke(0, new BasicStroke(2.2f));
        errRenderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(0, errRenderer);

        // ── second dataset: accuracy on right axis (blue) ─────────────────
        plot.setDataset(1, accDataset);
        plot.mapDatasetToRangeAxis(1, 1);

        NumberAxis accAxis = new NumberAxis("Accuracy (%)");
        accAxis.setRange(0, 105);
        accAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        accAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 13));
        accAxis.setLabelPaint(BLUE);
        plot.setRangeAxis(1, accAxis);

        XYLineAndShapeRenderer accRenderer = new XYLineAndShapeRenderer(true, true);
        accRenderer.setSeriesPaint(0, BLUE);
        accRenderer.setSeriesStroke(0, new BasicStroke(2.2f));
        accRenderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(1, accRenderer);

        // ── left axis style ───────────────────────────────────────────────
        NumberAxis leftAxis = (NumberAxis) plot.getRangeAxis(0);
        leftAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        leftAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 13));
        leftAxis.setLabelPaint(RED);
        leftAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // ── domain axis style ─────────────────────────────────────────────
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.PLAIN, 13));
        plot.getDomainAxis().setStandardTickUnits(
                org.jfree.chart.axis.NumberAxis.createIntegerTickUnits());

        // ── legend ────────────────────────────────────────────────────────
        chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 13));
        chart.getLegend().setBackgroundPaint(BOX_BG);

        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(BOX_BG);
        cp.setBorder(new LineBorder(BORDER_C, 1, true));
        cp.setMouseWheelEnabled(true);
        return cp;
    }

    /** RIGHT: inputs to manually test the trained perceptron */
    private JPanel buildTestPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG);
        outer.setPreferredSize(new Dimension(250, 0));

        // ── card ──────────────────────────────────────────────────────────
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BOX_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(20, 18, 20, 18)
        ));

        // title
        JLabel title = new JLabel("Test Perceptron");
        title.setFont(new Font("SansSerif", Font.PLAIN, 17));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Enter plant features:");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(new Color(130, 130, 130));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sub);
        card.add(Box.createVerticalStrut(18));

        // ── Soil Moisture ─────────────────────────────────────────────────
        card.add(inputLabel("Soil Moisture (0–100)"));
        card.add(Box.createVerticalStrut(5));
        soilSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 100, 1));
        styleSpinner(soilSpinner);
        card.add(soilSpinner);
        card.add(Box.createVerticalStrut(14));

        // ── Last Watered ──────────────────────────────────────────────────
        card.add(inputLabel("Last Watered (0–48 h)"));
        card.add(Box.createVerticalStrut(5));
        lastWateredSpinner = new JSpinner(new SpinnerNumberModel(24, 0, 48, 1));
        styleSpinner(lastWateredSpinner);
        card.add(lastWateredSpinner);
        card.add(Box.createVerticalStrut(14));

        // ── Plant Type ────────────────────────────────────────────────────
        card.add(inputLabel("Plant Type"));
        card.add(Box.createVerticalStrut(5));
        plantTypeCombo = new JComboBox<>(new String[]{"0 – Cactus", "1 – Flower", "2 – Herb"});
        plantTypeCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        plantTypeCombo.setBackground(Color.WHITE);
        plantTypeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        plantTypeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(plantTypeCombo);
        card.add(Box.createVerticalStrut(20));

        // ── Predict button ────────────────────────────────────────────────
        JButton predictBtn = new JButton("Predict");
        predictBtn.setFocusPainted(false);
        predictBtn.setFont(new Font("SansSerif", Font.PLAIN, 15));
        predictBtn.setBackground(BLUE);
        predictBtn.setForeground(Color.WHITE);
        predictBtn.setBorder(new EmptyBorder(10, 18, 10, 18));
        predictBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        predictBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        predictBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        predictBtn.addActionListener(e -> runPrediction());
        card.add(predictBtn);
        card.add(Box.createVerticalStrut(20));

        // ── divider ───────────────────────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(BORDER_C);
        card.add(sep);
        card.add(Box.createVerticalStrut(16));

        // ── result label ──────────────────────────────────────────────────
        JLabel resultTitle = new JLabel("Prediction result:");
        resultTitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        resultTitle.setForeground(new Color(130, 130, 130));
        resultTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(resultTitle);
        card.add(Box.createVerticalStrut(8));

        predictionResultLabel = new JLabel("—");
        predictionResultLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        predictionResultLabel.setForeground(new Color(150, 150, 150));
        predictionResultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(predictionResultLabel);

        card.add(Box.createVerticalGlue());

        outer.add(card, BorderLayout.NORTH);
        return outer;
    }

    /** SOUTH: thin status bar */
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        bar.setBackground(BG);
        statusLabel = new JLabel("Train the perceptron first, then use the test panel on the right.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(130, 130, 130));
        bar.add(statusLabel);
        return bar;
    }

    // ─── small helpers ─────────────────────────────────────────────────────

    private JLabel inputLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(TEXT_MED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("SansSerif", Font.PLAIN, 14));
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        spinner.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    /** Runs the prediction and updates the result label */
    private void runPrediction() {
        if (perceptron == null) {
            predictionResultLabel.setText("—");
            predictionResultLabel.setForeground(RED);
            statusLabel.setText("⚠  Please train the perceptron first!");
            return;
        }

        int soil      = (Integer) soilSpinner.getValue();
        int lastW     = (Integer) lastWateredSpinner.getValue();
        int plantType = plantTypeCombo.getSelectedIndex(); // 0, 1, or 2

        int result = perceptron.predict(soil, lastW, plantType);

        if (result == 1) {
            predictionResultLabel.setText("Needs Water 💧");
            predictionResultLabel.setForeground(RED);
        } else {
            predictionResultLabel.setText("No Water Needed ✓");
            predictionResultLabel.setForeground(GREEN);
        }

        statusLabel.setText("Tested: soil=" + soil + ", lastWatered=" + lastW
                + "h, type=" + plantTypeCombo.getSelectedItem()
                + "  →  " + (result == 1 ? "NEEDS WATER" : "OK"));
    }
}