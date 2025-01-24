package ui.components;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;

public class RegistrationGraphPanel extends JPanel {

    public RegistrationGraphPanel(Map<LocalDate, Integer> registrationsPerDay) {
        setLayout(new BorderLayout()); // Use BorderLayout for proper resizing

        if (registrationsPerDay == null || registrationsPerDay.isEmpty()) {
            JLabel noDataLabel = new JLabel("No registration data available.");
            noDataLabel.setHorizontalAlignment(JLabel.CENTER);
            add(noDataLabel, BorderLayout.CENTER);
        } else {
            // Create a dataset for the bar chart
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<LocalDate, Integer> entry : registrationsPerDay.entrySet()) {
                dataset.addValue(entry.getValue(), "Registrations", entry.getKey().toString());
            }

            // Create the bar chart
            JFreeChart chart = ChartFactory.createBarChart(
                    "Daily User Registrations", // Chart title
                    "Date",                    // X-axis label
                    "Number of Registrations", // Y-axis label
                    dataset,                   // Data
                    PlotOrientation.VERTICAL,
                    true,                      // Include legend
                    true,
                    false
            );

            // Configure the Y-axis to display integers only
            CategoryPlot plot = chart.getCategoryPlot();
            NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
            yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); // Force integer values

            // Adjust bar width and spacing
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setMaximumBarWidth(0.1); // Set bar width (0.1 = 10% of available space)
            renderer.setItemMargin(0.2); // Add 20% space between bars

            // Create the chart panel
            ChartPanel chartPanel = new ChartPanel(chart) {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(400, 300); // Set initial preferred size
                }
            };
            chartPanel.setMouseWheelEnabled(true); // Allow zooming with the mouse wheel
            add(chartPanel, BorderLayout.CENTER); // Add to the center to allow resizing
        }
    }
}