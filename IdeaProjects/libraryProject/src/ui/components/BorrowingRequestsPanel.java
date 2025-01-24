package ui.components;

import controllers.AdminController;
import controllers.BookController;
import controllers.TransactionController;
import models.Book;
import models.Transaction;
import Listener.TransactionListener;
import utils.createStyledButton;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowingRequestsPanel extends JPanel implements TransactionListener {
    private final AdminController adminController;
    private final TransactionController transactionController;
    private final BookController bookController;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField searchField;

    public BorrowingRequestsPanel(AdminController adminController, TransactionController transactionController) {
        this.adminController = adminController;
        this.transactionController = transactionController;
        this.bookController = new BookController();
        setLayout(new BorderLayout());

        // Search field for filtering requests
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by User Email or Book Title");
        searchField.addActionListener(e -> refreshTable()); // Refresh table on Enter key
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Initialize the table and table model
        String[] columns = {"Select", "Row No.", "User Email", "Book ID", "Book Title", "Request Date", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 6; // Only the "Select" and "Actions" columns are editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : Object.class; // "Select" column uses checkboxes
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40); // Set row height for better spacing

        // Center align text in all columns except the "Select" column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Set custom renderer and editor for the "Actions" column
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(this));

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Add buttons for bulk actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton acceptSelectedButton = createStyledButton.create("Accept Selected", new Color(0, 128, 0));
        JButton rejectSelectedButton = createStyledButton.create("Reject Selected", new Color(220, 20, 60));
        JButton acceptAllButton = createStyledButton.create("Accept All", new Color(0, 128, 0));
        JButton rejectAllButton = createStyledButton.create("Reject All", new Color(220, 20, 60));

        // Add action listeners to buttons
        acceptSelectedButton.addActionListener(e -> handleAcceptSelectedRequests());
        rejectSelectedButton.addActionListener(e -> handleRejectSelectedRequests());
        acceptAllButton.addActionListener(e -> handleAcceptAllRequests());
        rejectAllButton.addActionListener(e -> handleRejectAllRequests());

        // Add buttons to the panel
        buttonPanel.add(acceptSelectedButton);
        buttonPanel.add(rejectSelectedButton);
        buttonPanel.add(acceptAllButton);
        buttonPanel.add(rejectAllButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data into the table
        refreshTable();
    }

    /**
     * Refreshes the table with the latest pending borrowing requests.
     * Filters the results based on the search text.
     */
    private void refreshTable() {
        List<Transaction> pendingRequests = transactionController.getPendingRequests();
        String searchText = searchField.getText().toLowerCase();
        tableModel.setRowCount(0); // Clear existing rows

        int rowNumber = 1;
        for (Transaction transaction : pendingRequests) {
            // Fetch the book title using the book ID
            Book book = bookController.getBooksByID(transaction.getBookId());
            String bookTitle = (book != null) ? book.getTitle() : "Unknown Book";

            // Filter based on search text
            if (searchText.isEmpty() || transaction.getUserEmail().toLowerCase().contains(searchText) || bookTitle.toLowerCase().contains(searchText)) {
                tableModel.addRow(new Object[]{
                        false, // Checkbox for selection
                        rowNumber++, // Row number
                        transaction.getUserEmail(),
                        transaction.getBookId(), // Book ID
                        bookTitle,
                        transaction.getBorrowDate(),
                        transaction // Pass the transaction object for the "Actions" column
                });
            }
        }
    }

    /**
     * Handles accepting selected borrowing requests.
     */
    private void handleAcceptSelectedRequests() {
        List<Transaction> selectedTransactions = getSelectedTransactions();
        if (selectedTransactions.isEmpty()) {
            showSweetAlert("Warning", "No requests selected!", "warning");
            return;
        }

        // Show a confirmation dialog
        int confirm = showSweetConfirm("Confirm Approval", "Are you sure you want to approve the selected requests?");
        if (confirm != JOptionPane.YES_OPTION) return;

        // Show a loading indicator
        JDialog loadingDialog = createLoadingDialog("Processing selected requests...");
        loadingDialog.setVisible(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (Transaction transaction : selectedTransactions) {
                    transactionController.approveBorrowingRequest(transaction.getId(), BorrowingRequestsPanel.this);
                }
                return null;
            }

            @Override
            protected void done() {
                loadingDialog.dispose(); // Close the loading dialog
                refreshTable();
                showSweetAlert("Success", "Selected requests approved successfully!", "success");
            }
        };
        worker.execute();
    }

    /**
     * Handles rejecting selected borrowing requests.
     */
    private void handleRejectSelectedRequests() {
        List<Transaction> selectedTransactions = getSelectedTransactions();
        if (selectedTransactions.isEmpty()) {
            showSweetAlert("Warning", "No requests selected!", "warning");
            return;
        }

        // Show a confirmation dialog
        int confirm = showSweetConfirm("Confirm Rejection", "Are you sure you want to reject the selected requests?");
        if (confirm != JOptionPane.YES_OPTION) return;

        // Show a loading indicator
        JDialog loadingDialog = createLoadingDialog("Processing selected requests...");
        loadingDialog.setVisible(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (Transaction transaction : selectedTransactions) {
                    transactionController.rejectBorrowingRequest(transaction.getId(), BorrowingRequestsPanel.this);
                }
                return null;
            }

            @Override
            protected void done() {
                loadingDialog.dispose(); // Close the loading dialog
                refreshTable();
                showSweetAlert("Success", "Selected requests rejected successfully!", "success");
            }
        };
        worker.execute();
    }

    /**
     * Handles accepting all borrowing requests.
     */
    private void handleAcceptAllRequests() {
        List<Transaction> allPendingRequests = transactionController.getPendingRequests();
        if (allPendingRequests.isEmpty()) {
            showSweetAlert("Warning", "No requests to accept!", "warning");
            return;
        }

        // Show a confirmation dialog
        int confirm = showSweetConfirm("Confirm Approval", "Are you sure you want to approve all requests?");
        if (confirm != JOptionPane.YES_OPTION) return;

        // Show a loading indicator
        JDialog loadingDialog = createLoadingDialog("Processing all requests...");
        loadingDialog.setVisible(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (Transaction transaction : allPendingRequests) {
                    transactionController.approveBorrowingRequest(transaction.getId(), BorrowingRequestsPanel.this);
                }
                return null;
            }

            @Override
            protected void done() {
                loadingDialog.dispose(); // Close the loading dialog
                refreshTable();
                showSweetAlert("Success", "All requests approved successfully!", "success");
            }
        };
        worker.execute();
    }

    /**
     * Handles rejecting all borrowing requests.
     */
    private void handleRejectAllRequests() {
        List<Transaction> allPendingRequests = transactionController.getPendingRequests();
        if (allPendingRequests.isEmpty()) {
            showSweetAlert("Warning", "No requests to reject!", "warning");
            return;
        }

        // Show a confirmation dialog
        int confirm = showSweetConfirm("Confirm Rejection", "Are you sure you want to reject all requests?");
        if (confirm != JOptionPane.YES_OPTION) return;

        // Show a loading indicator
        JDialog loadingDialog = createLoadingDialog("Processing all requests...");
        loadingDialog.setVisible(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (Transaction transaction : allPendingRequests) {
                    transactionController.rejectBorrowingRequest(transaction.getId(), BorrowingRequestsPanel.this);
                }
                return null;
            }

            @Override
            protected void done() {
                loadingDialog.dispose(); // Close the loading dialog
                refreshTable();
                showSweetAlert("Success", "All requests rejected successfully!", "success");
            }
        };
        worker.execute();
    }

    /**
     * Retrieves the selected transactions from the table.
     *
     * @return A list of selected transactions.
     */
    private List<Transaction> getSelectedTransactions() {
        List<Transaction> selectedTransactions = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) tableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                selectedTransactions.add((Transaction) tableModel.getValueAt(i, 6)); // Get the transaction object from the "Actions" column
            }
        }
        return selectedTransactions;
    }

    /**
     * Shows a Sweet Alert-like confirmation dialog.
     *
     * @param title   The title of the dialog.
     * @param message The message to display.
     * @return The user's choice (YES_OPTION or NO_OPTION).
     */
    private int showSweetConfirm(String title, String message) {
        Object[] options = {"Yes", "No"};
        return JOptionPane.showOptionDialog(
                this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]
        );
    }

    /**
     * Shows a Sweet Alert-like message dialog.
     *
     * @param title   The title of the dialog.
     * @param message The message to display.
     * @param type    The type of dialog ("success", "error", "warning").
     */
    private void showSweetAlert(String title, String message, String type) {
        int messageType;
        switch (type) {
            case "success" -> messageType = JOptionPane.INFORMATION_MESSAGE;
            case "error" -> messageType = JOptionPane.ERROR_MESSAGE;
            case "warning" -> messageType = JOptionPane.WARNING_MESSAGE;
            default -> messageType = JOptionPane.PLAIN_MESSAGE;
        }
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    /**
     * Creates a loading dialog with a message.
     *
     * @param message The message to display in the loading dialog.
     * @return The loading dialog.
     */
    private JDialog createLoadingDialog(String message) {
        JDialog loadingDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Loading", true);
        loadingDialog.setLayout(new BorderLayout());
        loadingDialog.setSize(300, 100);
        loadingDialog.setLocationRelativeTo(this);

        JLabel loadingLabel = new JLabel(message, SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.BOLD, 14));
        loadingDialog.add(loadingLabel, BorderLayout.CENTER);

        return loadingDialog;
    }

    @Override
    public void onReturnSuccess(Transaction transaction) {
        // Not needed for this panel
    }

    @Override
    public void onReturnFailure(String errorMessage) {
        // Not needed for this panel
    }

    @Override
    public void onRejection(Transaction transaction) {
        refreshTable();
        showSweetAlert("Success", "Request rejected successfully!", "success");
    }

    @Override
    public void onAdminApproval(Transaction transaction) {
        refreshTable();
        showSweetAlert("Success", "Request approved successfully!", "success");
    }

    // Custom Button Renderer
    private static class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton acceptButton;
        private final JButton rejectButton;

        public ButtonRenderer() {
            setLayout(new GridBagLayout()); // Use GridBagLayout for better control
            acceptButton = createStyledButton.create("Accept", new Color(0, 128, 0));
            rejectButton = createStyledButton.create("Reject", new Color(220, 20, 60));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 5, 0, 5); // Add some padding between buttons

            gbc.gridx = 0;
            gbc.gridy = 0;
            add(acceptButton, gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            add(rejectButton, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Custom Button Editor
    private static class ButtonEditor extends DefaultCellEditor {
        private final JPanel panel;
        private final JButton acceptButton;
        private final JButton rejectButton;
        private Transaction currentTransaction;
        private final BorrowingRequestsPanel borrowingRequestsPanel;

        public ButtonEditor(BorrowingRequestsPanel borrowingRequestsPanel) {
            super(new JCheckBox());
            this.borrowingRequestsPanel = borrowingRequestsPanel;

            panel = new JPanel(new GridBagLayout()); // Use GridBagLayout for better control
            acceptButton = createStyledButton.create("Accept", new Color(0, 128, 0));
            rejectButton = createStyledButton.create("Reject", new Color(220, 20, 60));

            // Add action listeners
            acceptButton.addActionListener(e -> {
                borrowingRequestsPanel.handleAcceptRequest(currentTransaction);
                fireEditingStopped();
            });

            rejectButton.addActionListener(e -> {
                borrowingRequestsPanel.handleRejectRequest(currentTransaction);
                fireEditingStopped();
            });

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 5, 0, 5); // Add some padding between buttons

            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(acceptButton, gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            panel.add(rejectButton, gbc);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            table.setFillsViewportHeight(true); // Ensure the table fills the viewport height
            currentTransaction = (Transaction) value;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentTransaction;
        }
    }

    public void handleAcceptRequest(Transaction transaction) {
        // Show a loading indicator
        JDialog loadingDialog = createLoadingDialog("Processing request...");
        loadingDialog.setVisible(true);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return transactionController.approveBorrowingRequest(transaction.getId(), BorrowingRequestsPanel.this);
            }

            @Override
            protected void done() {
                loadingDialog.dispose(); // Close the loading dialog
                try {
                    boolean success = get();
                    if (success) {
                        onAdminApproval(transaction); // Notify success
                    } else {
                        // Notify the user of failure directly
                        JOptionPane.showMessageDialog(BorrowingRequestsPanel.this, "Failed to approve the request. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    // Notify the user of the error
                    JOptionPane.showMessageDialog(BorrowingRequestsPanel.this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Handles rejecting a single borrowing request.
     *
     * @param transaction The transaction to reject.
     */
    public void handleRejectRequest(Transaction transaction) {
        // Show a loading indicator
        JDialog loadingDialog = createLoadingDialog("Processing request...");
        loadingDialog.setVisible(true);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return transactionController.rejectBorrowingRequest(transaction.getId(), BorrowingRequestsPanel.this);
            }

            @Override
            protected void done() {
                loadingDialog.dispose(); // Close the loading dialog
                try {
                    boolean success = get();
                    if (success) {
                        onRejection(transaction); // Notify success
                    } else {
                        // Notify the user of failure directly
                        JOptionPane.showMessageDialog(BorrowingRequestsPanel.this, "Failed to reject the request. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    // Notify the user of the error
                    JOptionPane.showMessageDialog(BorrowingRequestsPanel.this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}