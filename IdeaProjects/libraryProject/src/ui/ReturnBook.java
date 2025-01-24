package ui;

import controllers.*;
import models.*;
import ui.components.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import Listener.TransactionListener;
import ui.components.Menu;
import utils.createStyledButton;

/**
 * The `ReturnBook` class represents the UI panel for returning books.
 * It implements the `TransactionListener` interface to handle return success and failure events.
 */
public class ReturnBook extends JPanel implements TransactionListener {
    private final TransactionController transactionController; // Manages transaction-related operations
    private final CardLayout cardLayout; // Manages navigation between panels
    private final JPanel cardPanel; // Contains all panels for navigation
    private final User user; // Current logged-in user
    private final MenuController menuController; // Handles menu actions
    private final BookController bookController; // Manages book-related operations
    private JTable transactionTable; // Displays transactions in a table

    /**
     * Constructor for the `ReturnBook` class.
     *
     * @param cardLayout The CardLayout used for navigation.
     * @param cardPanel  The JPanel containing all cards for navigation.
     * @param user       The current logged-in user.
     */
    public ReturnBook(CardLayout cardLayout, JPanel cardPanel, User user) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.user = user;
        this.menuController = new MenuController(user, cardLayout, cardPanel);
        this.transactionController = new TransactionController();
        this.bookController = new BookController();

        initializeUI(); // Initialize the UI components
    }

    /**
     * Initializes the UI components for the ReturnBook panel.
     */
    private void initializeUI() {
        setLayout(new BorderLayout()); // Use BorderLayout for the main panel

        // Add the header with the library name, user name, and logout handler
        add(new Header("Imagine Library", user, this::handleLogout), BorderLayout.NORTH);

        // Create the main content panel
        JPanel mainContent = new JPanel(new BorderLayout());
        String[] menuItems = {"Home", "View profile", "Borrow Book", "Return Book", "Borrowed books", "Back to previous", "New Arrivals"};
        mainContent.add(new Menu(menuItems, menuController::handleMenuButtonClick), BorderLayout.WEST);

        // Add the transaction table (with the return button) to the main content
        mainContent.add(createTransactionTablePanel(), BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER); // Add the main content to the panel
    }

    /**
     * Creates the transaction table panel with the "Return Selected Books" button at the bottom.
     *
     * @return A JPanel containing the transaction table and the return button.
     */
    private JPanel createTransactionTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding

        // Add a title label above the table
        JLabel titleLabel = new JLabel("Return Book");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Create and add the transaction table to a scroll pane
        transactionTable = createTransactionTable();
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create and style the "Return Selected Books" button
        JButton returnButton = createStyledButton.create("Return Selected Books", new Color(90, 160, 255));
        returnButton.addActionListener(e -> handleReturnButtonClick()); // Add click handler

        // Create a panel to center the button
        JPanel buttonPanel = new JPanel(new GridBagLayout()); // Use GridBagLayout for precise centering
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0); // Add padding above and below the button
        buttonPanel.add(returnButton, gbc);

        panel.add(buttonPanel, BorderLayout.SOUTH); // Add the button panel to the bottom of the main panel

        return panel;
    }

    /**
     * Creates and configures the transaction table.
     *
     * @return A JTable displaying the transaction data.
     */
    private JTable createTransactionTable() {
        // Define column names for the table
        String[] columnNames = {"Select", "Transaction's id", "Book's id", "Book's Title", "Issued date", "Due date", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Only the "Select" column is editable (for checkboxes)
            }
        };

        populateTableModel(tableModel); // Populate the table with data

        JTable table = new JTable(tableModel);

        // Set the checkbox renderer and editor for the "Select" column
        table.getColumnModel().getColumn(0).setCellRenderer(new CheckBoxRenderer());
        table.getColumnModel().getColumn(0).setCellEditor(new CheckBoxEditor(new JCheckBox()));

        customizeTable(table); // Customize the table appearance

        return table;
    }

    /**
     * Populates the table model with transaction data.
     *
     * @param tableModel The DefaultTableModel to populate with transaction data.
     */
    private void populateTableModel(DefaultTableModel tableModel) {
        List<Transaction> transactions = transactionController.getTransactionsByUser(user.getEmail());
        for (Transaction transaction : transactions) {
            // Exclude returned and rejected transactions
            if (!transaction.isReturned() && !"REJECTED".equals(transaction.getStatus())) {
                Object[] rowData = {
                        false, // Checkbox for selection
                        transaction.getId(),
                        transaction.getBookId(),
                        getBookTitle(transaction.getBookId()), // Fetch book title by ID
                        transaction.getBorrowDate(),
                        transaction.getDueDate(),
                        transaction.isReturned() ? "Returned" : "Pending"
                };
                tableModel.addRow(rowData); // Add the row to the table model
            }
        }
    }
    /**
     * Customizes the appearance of the table.
     *
     * @param table The JTable to customize.
     */
    private void customizeTable(JTable table) {
        table.setRowHeight(30); // Set row height
        table.setShowGrid(true); // Show grid lines
        table.setGridColor(new Color(230, 230, 230)); // Set grid color
        table.setIntercellSpacing(new Dimension(10, 10)); // Set spacing between cells

        // Center-align all columns except the "Select" column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Customize the table header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(90, 160, 255));
        header.setForeground(Color.WHITE);
    }

    /**
     * Handles the "Return Selected Books" button click.
     */
    private void handleReturnButtonClick() {
        DefaultTableModel model = (DefaultTableModel) transactionTable.getModel();
        boolean atLeastOneSelected = false;
        List<Book> selectedBooks = new ArrayList<>(); // List to store selected books

        // Iterate through the table rows to find selected books
        for (int row = 0; row < model.getRowCount(); row++) {
            boolean isSelected = (boolean) model.getValueAt(row, 0); // Check if the checkbox is selected
            if (isSelected) {
                atLeastOneSelected = true;
                String bookId = (String) model.getValueAt(row, 2); // Get book ID from the selected row
                Book book = bookController.getBooksByID(bookId);
                if (book == null) {
                    JOptionPane.showMessageDialog(this, "Book not found for ID: " + bookId, "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                selectedBooks.add(book); // Add the selected book to the list
            }
        }

        // Show an error if no books are selected
        if (!atLeastOneSelected) {
            JOptionPane.showMessageDialog(this, "Please select at least one book to return.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show confirmation dialog for returning all selected books (ONCE)
        showReturnConfirmationDialog(selectedBooks);
    }

    /**
     * Shows a confirmation dialog for returning multiple books.
     *
     * @param selectedBooks The list of books to be returned.
     */
    private void showReturnConfirmationDialog(List<Book> selectedBooks) {
        JDialog dialog = new JDialog();
        dialog.setUndecorated(true); // Remove window decorations
        dialog.setSize(400, 200); // Set dialog size
        dialog.setLocationRelativeTo(this); // Center the dialog
        dialog.setModal(true); // Make the dialog modal

        // Create the main panel for the dialog
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 160, 255), 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Add a title label
        JLabel titleLabel = new JLabel("Confirm Return");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add a message label with the number of selected books
        JLabel messageLabel = new JLabel("<html><center>Are you sure you want to return<br>" + selectedBooks.size() + " selected books?</center></html>");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create a button panel for "Confirm" and "Cancel" buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        // Create and style the "Confirm" button
        JButton confirmButton = createStyledButton.create("Confirm", new Color(90, 160, 255));
        confirmButton.addActionListener(_ -> {
            dialog.dispose(); // Close the dialog
            boolean allSuccessful = true;
            int successfulReturns = 0;
            int failedReturns = 0;

            // Attempt to return all selected books in a batch
            for (Book book : selectedBooks) {
                boolean success = transactionController.returnBook(book, user, this);
                if (success) {
                    successfulReturns++;
                } else {
                    failedReturns++;
                    allSuccessful = false;
                }
            }

            // Show a summary of the return operation
            if (allSuccessful) {
                JOptionPane.showMessageDialog(this, "All selected books returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showReturnSummaryDialog(selectedBooks.size(), successfulReturns, failedReturns);
            }
            refreshTable(); // Refresh the table to reflect the updated status
        });

        // Create and style the "Cancel" button
        JButton cancelButton = createStyledButton.create("Cancel", Color.GRAY);
        cancelButton.addActionListener(_ -> dialog.dispose()); // Close the dialog

        // Add buttons to the button panel
        buttonPanel.add(confirmButton);
        buttonPanel.add(Box.createHorizontalStrut(10)); // Add spacing between buttons
        buttonPanel.add(cancelButton);

        // Add components to the main panel
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(messageLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);

        dialog.add(mainPanel); // Add the main panel to the dialog
        dialog.setVisible(true); // Show the dialog
    }

    /**
     * Shows a summary dialog with the results of the return operation.
     *
     * @param totalBooks        The total number of books selected.
     * @param successfulReturns The number of books successfully returned.
     * @param failedReturns     The number of books that failed to return.
     */
    private void showReturnSummaryDialog(int totalBooks, int successfulReturns, int failedReturns) {
        String message = "<html><center>"
                + "Total books selected: " + totalBooks + "<br>"
                + "Books returned successfully: " + successfulReturns + "<br>"
                + "Books failed to return: " + failedReturns + "<br>"
                + "</center></html>";

        JOptionPane.showMessageDialog(this, message, "Return Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows an error dialog with the given message.
     *
     * @param message The error message to display.
     */
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    @Override
    public void onReturnSuccess(Transaction transaction) {}
    @Override
    public void onReturnFailure(String errorMessage) {
        showErrorDialog(errorMessage); // Show the error message
    }
    @Override
    public void onRejection(Transaction transaction) {}

    @Override
    public void onAdminApproval(Transaction transaction) {}

    /**
     * Retrieves the book title by book ID.
     *
     * @param bookId The ID of the book.
     * @return The title of the book, or "Unknown" if the book is not found.
     */
    private String getBookTitle(String bookId) {
        Book book = bookController.getBooksByID(bookId);
        return book != null ? book.getTitle() : "Unknown";
    }

    /**
     * Handles the logout action.
     */
    private void handleLogout() {
        cardLayout.show(cardPanel, "Login"); // Navigate to the login screen
        JOptionPane.showMessageDialog(this, "Logged out successfully!", "Logout", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Refreshes the table after returning a book.
     */
    public void refreshTable() {
        DefaultTableModel model = (DefaultTableModel) transactionTable.getModel();
        model.setRowCount(0); // Clear the table
        populateTableModel(model); // Repopulate the table with updated data
        transactionTable.revalidate(); // Refresh the table UI
        transactionTable.repaint();
    }
}

/**
 * Custom renderer for checkboxes in the table.
 */
class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
    public CheckBoxRenderer() {
        setHorizontalAlignment(JLabel.CENTER); // Center the checkbox horizontally
        setVerticalAlignment(JLabel.CENTER);   // Center the checkbox vertically
        setOpaque(true); // Make the checkbox opaque to avoid rendering issues
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setSelected((boolean) value); // Set the checkbox state based on the cell value

        // Set background and foreground colors based on selection
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        return this;
    }
}

/**
 * Custom editor for checkboxes in the table.
 */
class CheckBoxEditor extends DefaultCellEditor {
    public CheckBoxEditor(JCheckBox checkBox) {
        super(checkBox);
        checkBox.setHorizontalAlignment(JLabel.CENTER); // Center the checkbox horizontally
        checkBox.setVerticalAlignment(JLabel.CENTER);   // Center the checkbox vertically
        checkBox.setOpaque(true); // Make the checkbox opaque to avoid rendering issues
    }
}