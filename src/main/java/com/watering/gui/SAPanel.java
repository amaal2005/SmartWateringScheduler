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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SAPanel extends JPanel {

    private static final Color BG       = new Color(245, 245, 242);
    private static final Color BOX_BG   = new Color(250, 250, 250);
    private static final Color BORDER_C = new Color(214, 214, 214);
    private static final Color GREEN    = new Color(109, 158,  31);
    private static final Color RED      = new Color(224,  79,  76);
    private static final Color BLUE     = new Color( 69, 126, 190);
    private static final Color ORANGE   = new Color(220, 130,  30);
    private static final Color TEXT_MED = new Color( 70,  70,  70);

    private final XYSeries costSeries = new XYSeries("Cost");
    private final XYSeries tempSeries = new XYSeries("Temperature");
    private final XYSeries swapSeries = new XYSeries("Accepted Swaps");

    private JLabel stepsLabel;
    private JLabel swapsLabel;
    private JLabel initCostLabel;
    private JLabel finalCostLabel;
    private JLabel improvLabel;
    private JLabel statusLabel;

    private JTable stepsTable;
    private DefaultTableModel tableModel;

    public SAPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // ── ابني الجدول أول شي ─────────────────────────────────────────
        String[] columns = {"Step", "Order", "Cost", "Temperature", "Accepted"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        stepsTable = new JTable(tableModel);

        // ── بعدين ابني الـ UI ──────────────────────────────────────────
        add(buildSummaryRow(), BorderLayout.NORTH);
        add(buildMainArea(),   BorderLayout.CENTER);
        add(buildStatusBar(),  BorderLayout.SOUTH);
    }

    // ═════════════════════════════════════════════════════════════════════
    //  PUBLIC API
    // ═════════════════════════════════════════════════════════════════════

    public void showResults(List<StepRecord> history,
                            double initialCost, double finalCost) {
        costSeries.clear();
        tempSeries.clear();
        swapSeries.clear();
        tableModel.setRowCount(0);

        if (history == null || history.isEmpty()) {
            statusLabel.setText("No SA history available.");
            return;
        }

        for (StepRecord r : history) {
            costSeries.add(r.step, r.cost);
            tempSeries.add(r.step, r.temperature);
            swapSeries.add(r.step, r.totalSwaps);

            String orderText = String.join(" → ", r.currentOrder);

            tableModel.addRow(new Object[]{
                    r.step,
                    orderText,
                    String.format("%.1f", r.cost),
                    String.format("%.1f", r.temperature),
                    r.accepted ? "✅" : "❌"
            });
        }

        int last = stepsTable.getRowCount() - 1;
        if (last >= 0)
            stepsTable.scrollRectToVisible(stepsTable.getCellRect(last, 0, true));

        StepRecord lastR = history.get(history.size() - 1);
        double improvement = (initialCost > 0)
                ? (initialCost - finalCost) / initialCost * 100.0 : 0.0;

        stepsLabel.setText("Total steps: " + lastR.step);
        swapsLabel.setText("Accepted swaps: " + lastR.totalSwaps);
        initCostLabel.setText(String.format("Initial cost: %.1f", initialCost));
        finalCostLabel.setText(String.format("Final cost: %.1f", finalCost));
        improvLabel.setText(String.format("Improvement: %.1f%%", improvement));
        improvLabel.setForeground(improvement > 0 ? GREEN : RED);

        statusLabel.setText("SA completed — "
                + lastR.step + " steps | "
                + lastR.totalSwaps + " accepted swaps | "
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

    private JSplitPane buildMainArea() {
        // ── style الجدول ──────────────────────────────────────────────
        stepsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        stepsTable.setRowHeight(26);
        stepsTable.setBackground(BOX_BG);
        stepsTable.setGridColor(BORDER_C);
        stepsTable.getTableHeader().setFont(new Font("SansSerif", Font.PLAIN, 13));
        stepsTable.getTableHeader().setBackground(new Color(240, 240, 238));

        stepsTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        stepsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        stepsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        stepsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        stepsTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        // لون الصفوف حسب Accepted
        stepsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, col);
                if (!isSelected) {
                    String accepted = (String) table.getModel().getValueAt(row, 4);
                    if ("✅".equals(accepted)) {
                        setBackground(new Color(240, 248, 235));
                    } else if ("❌".equals(accepted)) {
                        setBackground(new Color(255, 245, 245));
                    } else {
                        setBackground(BOX_BG);
                    }
                }
                return this;
            }
        });

        JScrollPane tableScroll = new JScrollPane(stepsTable);
        tableScroll.setBorder(new LineBorder(BORDER_C, 1, true));

        JLabel tableTitle = new JLabel("  SA Steps Log");
        tableTitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tableTitle.setForeground(TEXT_MED);
        tableTitle.setBorder(new EmptyBorder(0, 0, 6, 0));

        JPanel tablePanel = new JPanel(new BorderLayout(0, 6));
        tablePanel.setBackground(BG);
        tablePanel.add(tableTitle,  BorderLayout.NORTH);
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        // ── الـ charts ────────────────────────────────────────────────
        JPanel chartsPanel = buildChartsArea();

        // ── SplitPane ─────────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                tablePanel, chartsPanel);
        split.setDividerLocation(420);
        split.setResizeWeight(0.35);
        split.setBorder(null);
        split.setBackground(BG);

        return split;
    }

    private JPanel buildChartsArea() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 12));
        panel.setBackground(BG);
        panel.add(buildCostChart());
        panel.add(buildTempSwapChart());
        return panel;
    }

    private ChartPanel buildCostChart() {
        XYSeriesCollection dataset = new XYSeriesCollection(costSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Cost over SA Steps", "Step", "Cost", dataset);
        styleChart(chart);

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer(true, false);
        r.setSeriesPaint(0, BLUE);
        r.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(r);

        styleAxis((NumberAxis) plot.getRangeAxis(),  "Cost", BLUE);
        styleAxis((NumberAxis) plot.getDomainAxis(), "Step", TEXT_MED);

        return wrapChart(chart);
    }

    private ChartPanel buildTempSwapChart() {
        XYSeriesCollection tempDataset = new XYSeriesCollection(tempSeries);
        XYSeriesCollection swapDataset = new XYSeriesCollection(swapSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Temperature Cooling  &  Accepted Swaps",
                "Step", "Temperature", tempDataset);
        styleChart(chart);

        XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer tempR = new XYLineAndShapeRenderer(true, false);
        tempR.setSeriesPaint(0, ORANGE);
        tempR.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(0, tempR);
        styleAxis((NumberAxis) plot.getRangeAxis(0), "Temperature", ORANGE);

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