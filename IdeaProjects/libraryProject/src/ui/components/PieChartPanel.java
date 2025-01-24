package ui.components;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Map;

public class PieChartPanel extends JPanel {

    public PieChartPanel(Map<String, Integer> data, String title) {
        setLayout(new BorderLayout());

        if (data == null || data.isEmpty()) {
            System.out.println("No data available for pie chart."); // Debugging
            JLabel noDataLabel = new JLabel("No data available.");
            noDataLabel.setHorizontalAlignment(JLabel.CENTER);
            add(noDataLabel, BorderLayout.CENTER);
        } else {
            // Create a dataset for the pie chart
            DefaultPieDataset dataset = new DefaultPieDataset();
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }

            // Create the pie chart
            JFreeChart chart = ChartFactory.createPieChart(
                    title, // Chart title
                    dataset, // Dataset
                    true, // Include legend
                    true,
                    false
            );

            // Customize the pie chart to display numbers on the slices
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                    "{0}: {1} ({2})", // Label format: {0} = category, {1} = value, {2} = percentage
                    NumberFormat.getNumberInstance(), // Format for values
                    NumberFormat.getPercentInstance() // Format for percentages
            ));

            // Create the chart panel
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(400, 300)); // Set preferred size
            add(chartPanel, BorderLayout.CENTER);
        }
    }
}