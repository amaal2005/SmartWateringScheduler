package com.watering.gui;

import com.watering.sa.SimulatedAnnealing.StepRecord;
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
 * SAPanel — shown in the "SA Optimization" tab.
 *
 * Layout:
 *  TOP    → summary row  (steps, swaps, initial cost → final cost)
 *  CENTER → Cost chart (left axis) + Temperature chart (right axis)
 *  SOUTH  → status bar
 */
public class SAPanel extends JPanel {

    // ── colours ───────────────────────────────────────────────────────────
    private static final Color BG       = new Color(245, 245, 242);
    private static final Color BOX_BG   = new Color(250, 250, 250);
    private static final Color BORDER_C = new Color(214, 214, 214);
    private static final Color GREEN    = new Color(109, 158,  31);
    private static final Color RED      = new Color(224,  79,  76);
    private static final Color BLUE     = new Color( 69, 126, 190);
    private static final Color ORANGE   = new Color(220, 130,  30);
    private static final Color TEXT_MED = new Color( 70,  70,  70);

    // ── chart series ──────────────────────────────────────────────────────
    private final XYSeries costSeries   = new XYSeries("Cost");
    private final XYSeries tempSeries   = new XYSeries("Temperature");
    private final XYSeries swapSeries   = new XYSeries("Accepted Swaps");

    // ── summary labels ────────────────────────────────────────────────────
    private JLabel stepsLabel;
    private JLabel swapsLabel;
    private JLabel initCostLabel;
    private JLabel finalCostLabel;
    private JLabel improvLabel;

    // ── status bar ────────────────────────────────────────────────────────
    private JLabel statusLabel;

    // ─────────────────────────────────────────────────────────────────────
    public SAPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildSummaryRow(), BorderLayout.NORTH);
        add(buildChartsArea(), BorderLayout.CENTER);
        add(buildStatusBar(),  BorderLayout.SOUTH);
    }

    // ═════════════════════════════════════════════════════════════════════
    //  PUBLIC API — called from ControlPanel after optimizeWithHistory()
    // ═════════════════════════════════════════════════════════════════════

    public void showResults(List<StepRecord> history,
                            double initialCost, double finalCost) {
        costSeries.clear();
        tempSeries.clear();
        swapSeries.clear();

        if (history == null || history.isEmpty()) {
            statusLabel.setText("No SA history available.");
            return;
        }

        for (StepRecord r : history) {
            costSeries.add(r.step, r.cost);
            tempSeries.add(r.step, r.temperature);
            swapSeries.add(r.step, r.totalSwaps);
        }

        StepRecord last = history.get(history.size() - 1);
        double improvement = (initialCost > 0)
                ? (initialCost - finalCost) / initialCost * 100.0 : 0.0;

        stepsLabel.setText("Total steps: " + last.step);
        swapsLabel.setText("Accepted swaps: " + last.totalSwaps);
        initCostLabel.setText(String.format("Initial cost: %.1f", initialCost));
        finalCostLabel.setText(String.format("Final cost: %.1f", finalCost));
        improvLabel.setText(String.format("Improvement: %.1f%%", improvement));
        improvLabel.setForeground(improvement > 0 ? GREEN : RED);

        statusLabel.setText("SA completed — "
                + last.step + " steps | "
                + last.totalSwaps + " accepted swaps | "
                + String.format("%.1f%%", improvement) + " improvement");
    }

    // ═════════════════════════════════════════════════════════════════════
    //  BUILD helpers
    // ═════════════════════════════════════════════════════════════════════

    private JPanel buildSummaryRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        row.setBackground(BG);

        stepsLabel     = summaryLabel("Total steps: —");
        swapsLabel     = summaryLabel("Accepted swaps: —");
        initCostLabel  = summaryLabel("Initial cost: —");
        finalCostLabel = summaryLabel("Final cost: —");
        improvLabel    = summaryLabel("Improvement: —");

        row.add(stepsLabel);
        row.add(divider());
        row.add(swapsLabel);
        row.add(divider());
        row.add(initCostLabel);
        row.add(divider());
        row.add(finalCostLabel);
        row.add(divider());
        row.add(improvLabel);

        return row;
    }

    /**
     * Two charts stacked vertically:
     *  TOP    → Cost over steps  (blue line)
     *  BOTTOM → Temperature + Cumulative swaps (orange + green)
     */
    private JPanel buildChartsArea() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 12));
        panel.setBackground(BG);

        panel.add(buildCostChart());
        panel.add(buildTempSwapChart());

        return panel;
    }

    // ── Cost chart ────────────────────────────────────────────────────────
    private ChartPanel buildCostChart() {
        XYSeriesCollection dataset = new XYSeriesCollection(costSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Cost over SA Steps",
                "Step",
                "Cost",
                dataset
        );
        styleChart(chart);

        XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer(true, false);
        r.setSeriesPaint(0, BLUE);
        r.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(r);

        styleAxis((NumberAxis) plot.getRangeAxis(), "Cost", BLUE);
        styleAxis((NumberAxis) plot.getDomainAxis(), "Step", TEXT_MED);

        return wrapChart(chart);
    }

    // ── Temperature + Swaps chart ─────────────────────────────────────────
    private ChartPanel buildTempSwapChart() {
        XYSeriesCollection tempDataset = new XYSeriesCollection(tempSeries);
        XYSeriesCollection swapDataset = new XYSeriesCollection(swapSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Temperature Cooling  &  Accepted Swaps",
                "Step",
                "Temperature",
                tempDataset
        );
        styleChart(chart);

        XYPlot plot = chart.getXYPlot();

        // left axis → temperature (orange)
        XYLineAndShapeRenderer tempR = new XYLineAndShapeRenderer(true, false);
        tempR.setSeriesPaint(0, ORANGE);
        tempR.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(0, tempR);
        styleAxis((NumberAxis) plot.getRangeAxis(0), "Temperature", ORANGE);

        // right axis → cumulative swaps (green)
        plot.setDataset(1, swapDataset);
        plot.mapDatasetToRangeAxis(1, 1);

        NumberAxis swapAxis = new NumberAxis("Accepted Swaps");
        swapAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        swapAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 13));
        swapAxis.setLabelPaint(GREEN);
        swapAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.setRangeAxis(1, swapAxis);

        XYLineAndShapeRenderer swapR = new XYLineAndShapeRenderer(true, false);
        swapR.setSeriesPaint(0, GREEN);
        swapR.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(1, swapR);

        styleAxis((NumberAxis) plot.getDomainAxis(), "Step", TEXT_MED);

        return wrapChart(chart);
    }

    // ── status bar ────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        bar.setBackground(BG);
        statusLabel = new JLabel("Run 'Optimize path' first to see SA visualization.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(130, 130, 130));
        bar.add(statusLabel);
        return bar;
    }

    // ── small helpers ─────────────────────────────────────────────────────

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(BOX_BG);
        chart.getTitle().setFont(new Font("SansSerif", Font.PLAIN, 15));
        chart.getTitle().setPaint(new Color(50, 50, 50));
        chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 12));
        chart.getLegend().setBackgroundPaint(BOX_BG);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(252, 252, 252));
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlinePaint(BORDER_C);
    }

    private void styleAxis(NumberAxis axis, String label, Color labelColor) {
        axis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        axis.setLabelFont(new Font("SansSerif", Font.PLAIN, 13));
        axis.setLabelPaint(labelColor);
    }

    private ChartPanel wrapChart(JFreeChart chart) {
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(BOX_BG);
        cp.setBorder(new LineBorder(BORDER_C, 1, true));
        cp.setMouseWheelEnabled(true);
        return cp;
    }

    private JLabel summaryLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        l.setForeground(TEXT_MED);
        return l;
    }

    private JLabel divider() {
        JLabel l = new JLabel("|");
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        l.setForeground(BORDER_C);
        return l;
    }
}