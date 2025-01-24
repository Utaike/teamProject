package ui;

import controllers.AdminController;
import models.Transaction;
import models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ManageTransactionPanel extends JPanel{
    private final AdminController adminController;
    private final DefaultTableModel tableModel;
    private final JTable transactionTable;
    private JComboBox<String > filterComboBox;
    private JTextField searchField;



    public ManageTransactionPanel(AdminController adminController) {
        this.adminController = adminController;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tableModel = new DefaultTableModel(new String[]{"id","userEmail","bookId","Title","borrowDate","dueDate","returnDate","isBorrow","Edit","Delete"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8 || column == 9; // Only allow interaction with buttons
            }
        };
        transactionTable= new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);

        transactionTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        transactionTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox(), "Edit"));
        transactionTable.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        transactionTable.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox(), "Delete"));
        add(scrollPane, BorderLayout.CENTER);

        add(createTopPanel(), BorderLayout.NORTH);
        refreshTransactionTable();

    }
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by name, email, or book");
        filterComboBox = new JComboBox<>(new String[]{"All", "Borrow", "Return"});
        filterComboBox.setToolTipText("Filter by transaction type");
        filterComboBox.addActionListener(e -> filterTable()); // Refresh table when filter changes

        JButton addButton = new JButton("Add Transaction");
        addButton.setToolTipText("Click to add a new Transaction");
        addButton.addActionListener(e -> showAddTransactionForm());

        searchField.getDocument().addDocumentListener(new SearchListener(searchField));

        // Add components to the top panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterComboBox);

        topPanel.add(filterPanel, BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(addButton, BorderLayout.EAST);

        return topPanel;
    }
    private void filterTable() {
        String query = searchField.getText().toLowerCase();
        String selectedFilter = (String) filterComboBox.getSelectedItem(); // Get selected filter

        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0); // Clear the table

            List<Transaction> transactions = adminController.allTransactions();
            for (Transaction transaction : transactions) {
                // Apply search filter
                boolean matchesSearch = transaction.getId().toLowerCase().contains(query) ||
                        transaction.getUserEmail().toLowerCase().contains(query) ||
                        transaction.getBookId().toLowerCase().contains(query);

                // Apply transaction type filter
                boolean matchesFilter = true;
                if (selectedFilter.equals("Borrow")) {
                    matchesFilter = transaction.isBorrow(); // Show only borrow transactions
                } else if (selectedFilter.equals("Return")) {
                    matchesFilter = !transaction.isBorrow(); // Show only return transactions
                }

                // Add the transaction to the table if it matches both filters
                if (matchesSearch && matchesFilter) {
                    String bookTitle = adminController.getBookTitle(transaction.getBookId()); // Get book title
                    tableModel.addRow(new Object[]{
                            transaction.getId(),
                            transaction.getUserEmail(),
                            transaction.getBookId(),
                            bookTitle, // Include book title
                            transaction.getBorrowDate(),
                            transaction.getDueDate(),
                            transaction.getReturnDate(),
                            transaction.isBorrow(),
                            "Edit", // Include Edit button
                            "Delete" // Include Delete button
                    });
                }
            }
        });
    }
    private void refreshTransactionTable() {
        tableModel.setRowCount(0);
        for (Transaction transaction : adminController.allTransactions()) {
            String bookTitle = adminController.getBookTitle(transaction.getBookId());
            tableModel.addRow(new Object[]{
                    transaction.getId(),
                    transaction.getUserEmail(),
                    transaction.getBookId(),
                    bookTitle,
                    transaction.getBorrowDate(),
                    transaction.getDueDate(),
                    transaction.getReturnDate(),
                    transaction.isBorrow(),
                    "Edit",
                    "Delete"
            });
        }
    }
    private void showAddTransactionForm() {
        JTextField bookIdField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField borrowDateField = new JTextField();
        JTextField dueDateField =new JTextField();

        // Create the form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.add(new JLabel("Book ID:"));
        formPanel.add(bookIdField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Borrow Date(Borrow Dated(YYYY-MM-DD))"));
        formPanel.add(borrowDateField);
        formPanel.add(new JLabel("Due Date(YYYY-MM-DD)"));


        // Show the dialog
        int result = JOptionPane.showConfirmDialog(this, formPanel, "Add User", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            // Validate input fields
            if (bookIdField.getText().isEmpty() || emailField.getText().isEmpty() ||
                    borrowDateField.getText().isEmpty()||dueDateField.getText().isEmpty() ) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String bookID = bookIdField.getText();
            String email = emailField.getText();
            LocalDate dueDate = LocalDate.parse(dueDateField.getText());
            LocalDate borrowDate =  LocalDate.parse(borrowDateField.getText());


            boolean isAdded = adminController.addTransaction(email,bookID,borrowDate,dueDate);

            if (isAdded) {
                JOptionPane.showMessageDialog(this, "Transaction added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTransactionTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add transaction.  may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteTransaction(String id) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this transaction?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            boolean isDeleted = adminController.deleteTransaction(id);

            if (isDeleted) {
                JOptionPane.showMessageDialog(this, "Transaction deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTransactionTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete transaction.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editTransaction(String transactionId) {
        // Get the transaction by ID
        Transaction transaction = adminController.getTransactionById(transactionId);
        if (transaction == null) {
            JOptionPane.showMessageDialog(this, "Transaction not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create input fields for the form and pre-fill them with the transaction details
        JTextField bookIdField = new JTextField(transaction.getBookId());
        JTextField userEmailField = new JTextField(transaction.getUserEmail());
        JTextField borrowDateField = new JTextField(transaction.getBorrowDate().toString());
        JTextField dueDateField = new JTextField(transaction.getDueDate().toString());
        JTextField returnDateField = new JTextField(transaction.getReturnDate() != null ? transaction.getReturnDate().toString() : "");

        // Create the form panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.add(new JLabel("Book ID:"));
        formPanel.add(bookIdField);
        formPanel.add(new JLabel("User Email:"));
        formPanel.add(userEmailField);
        formPanel.add(new JLabel("Borrow Date (YYYY-MM-DD):"));
        formPanel.add(borrowDateField);
        formPanel.add(new JLabel("Due Date (YYYY-MM-DD):"));
        formPanel.add(dueDateField);


        // Show the dialog
        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                "Edit Transaction",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        // If the user clicks "OK", process the input
        if (result == JOptionPane.OK_OPTION) {
            // Validate input fields
            if (bookIdField.getText().isEmpty() || userEmailField.getText().isEmpty() ||
                    borrowDateField.getText().isEmpty() || dueDateField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "All fields are required!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            try {
                // Parse input values
                String bookId = bookIdField.getText();
                String userEmail = userEmailField.getText();
                LocalDate borrowDate = LocalDate.parse(borrowDateField.getText());
                LocalDate dueDate = LocalDate.parse(dueDateField.getText());
                LocalDate returnDate = returnDateField.getText().isEmpty() ? null : LocalDate.parse(returnDateField.getText());

                // Update the transaction
                transaction.setBookId(bookId);
                transaction.setUserEmail(userEmail);
                transaction.setBorrowDate(borrowDate);
                transaction.setDueDate(dueDate);
                transaction.setReturnDate(returnDate);

                // Save the updated transaction
                boolean isUpdated = adminController.updateTransaction(transaction);

                // Show success or error message
                if (isUpdated) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Transaction updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    refreshTransactionTable(); // Refresh the table to show the updated transaction
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Failed to update transaction.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid date format. Please use YYYY-MM-DD.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // Custom Button Renderer and Editor classes (as shown earlier)
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private String label;
        private JButton button;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox, String label) {
            super(checkBox);
            this.label = label;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            button.setText(label);
            isPushed = true;
            return button;
        }


        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int selectedRow = transactionTable.convertRowIndexToModel(transactionTable.getEditingRow());
                String transactionId = (String) tableModel.getValueAt(selectedRow, 0);

                if (label.equals("Edit")) {
                    // Handle edit action
                    editTransaction(transactionId);
                } else if (label.equals("Delete")) {
                    // Handle delete action
                    deleteTransaction(transactionId);
                }
            }
            isPushed = false;
            return label;
        }
    }
    private class SearchListener implements javax.swing.event.DocumentListener {
        private final JTextField searchField;

        public SearchListener(JTextField searchField) {
            this.searchField = searchField;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            filterTable();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            filterTable();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            filterTable();
        }


    }


}
