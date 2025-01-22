package ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class CreateTablePanel extends JPanel {

    public <T> JPanel createTablePanel(
            String title,
            List<T> data,
            String[] columns,
            Function<T, Object[]> rowMapper,
            Runnable seeAllAction,
            Runnable showLessAction
    ) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding

        // Create the title label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create a custom DefaultTableModel that makes cells uneditable
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make all cells uneditable
                return false;
            }
        };

        // Add only the first 10 items to the table initially
        int limit = Math.min(10, data.size()); // Display up to 10 items
        for (int i = 0; i < limit; i++) {
            T item = data.get(i);
            tableModel.addRow(rowMapper.apply(item)); // Use the rowMapper to convert the item to a row
        }

        // Create the table
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED); // Show vertical scrollbar when needed
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Show horizontal scrollbar when needed

        // Adjust column widths dynamically
        adjustColumnWidths(table);

        // Adjust row height
        table.setRowHeight(30); // Set row height

        // Create the "See All" button
        JButton seeAllButton = new JButton("See All");

        // Create the "Show Less" button
        JButton showLessButton = new JButton("Show Less");
        showLessButton.setVisible(false);

        // Add action listeners
        seeAllButton.addActionListener(e -> {
            tableModel.setRowCount(0); // Clear existing rows
            for (T item : data) {
                tableModel.addRow(rowMapper.apply(item)); // Use the rowMapper to convert the item to a row
            }
            seeAllButton.setVisible(false);
            showLessButton.setVisible(true);
        });

        showLessButton.addActionListener(e -> {
            // Load only the first 10 items into the table
            tableModel.setRowCount(0); // Clear existing rows
            int newLimit = Math.min(10, data.size()); // Display up to 10 items
            for (int i = 0; i < newLimit; i++) {
                T item = data.get(i);
                tableModel.addRow(rowMapper.apply(item)); // Use the rowMapper to convert the item to a row
            }
            showLessButton.setVisible(false);
            seeAllButton.setVisible(true);
        });

        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(seeAllButton);
        buttonPanel.add(showLessButton);

        // Create a panel for the title and buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Add components to the main panel
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void adjustColumnWidths(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Disable auto-resize

        for (int column = 0; column < table.getColumnCount(); column++) {
            int maxWidth = 0;

            // Get the header width
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = table.getTableHeader().getDefaultRenderer();
            }
            Component headerComp = headerRenderer.getTableCellRendererComponent(
                    table, tableColumn.getHeaderValue(), false, false, 0, column);
            maxWidth = headerComp.getPreferredSize().width;

            // Get the maximum width of the data in the column
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component cellComp = table.prepareRenderer(cellRenderer, row, column);
                maxWidth = Math.max(maxWidth, cellComp.getPreferredSize().width);
            }

            // Add some padding to the column width
            maxWidth += 20; // Add 20 pixels of padding

            // Set the column width
            tableColumn.setPreferredWidth(maxWidth);
        }
    }
}