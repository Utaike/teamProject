package ui;

import controllers.BookController;
import controllers.MenuController;
import controllers.TransactionController;
import models.Book;
import models.Transaction;
import models.User;
import ui.components.Header;
import ui.components.Menu;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class ViewTransactionHistory extends JPanel {
    private final TransactionController transactionController;
    private final CardLayout cardLayout; // Layout manager for switching panels
    private final JPanel cardPanel; // Main panel that holds all other panels
    private final User user; // Current logged-in user
    private final MenuController menuController;

    // Constructor
    public ViewTransactionHistory(CardLayout cardLayout, JPanel cardPanel, User user) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.user = user;
        this.menuController = new MenuController(user, cardLayout, cardPanel);
        this.transactionController = new TransactionController();

        // Set the layout of the book details panel to BorderLayout
        setLayout(new BorderLayout());

        // Add the header to the top of the dashboard
        add(new Header("Imagine Library", user, this::handleLogout), BorderLayout.NORTH);

        // Create the main content panel
        JPanel mainContent = new JPanel(new BorderLayout());

        // Define menu items and add the menu to the left side of the dashboard
        String[] menuItems = {"Home", "View profile", "Borrow Book", "Return Book", "Borrowed books", "Back to previous", "New Arrivals"};
        String[] iconPaths = {
                "IdeaProjects/libraryProject/src/images/icons/home.png",       // Path to home icon
                "IdeaProjects/libraryProject/src/images/icons/profile.png",    // Path to profile icon
                "IdeaProjects/libraryProject/src/images/icons/avialableBooks.png",      // Path to books icon
                "IdeaProjects/libraryProject/src/images/icons/users.png",      // Path to users icon
                "IdeaProjects/libraryProject/src/images/icons/books.png", // Path to transactions icon
                "IdeaProjects/libraryProject/src/images/icons/arrow.png",      // Path to users icon
                "IdeaProjects/libraryProject/src/images/icons/settings.png"
        };
        mainContent.add(new Menu(menuItems,iconPaths, menuController::handleMenuButtonClick), BorderLayout.WEST);

        // Add the main content (search bar and book list) to the center of the dashboard
        mainContent.add(createMainContent(), BorderLayout.CENTER);

        // Add the main content panel to the dashboard
        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Add a label for the title
        JLabel headerLabel = new JLabel("Borrow History", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Customize font
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Add padding
        mainPanel.add(headerLabel, BorderLayout.NORTH); // Add the label at the top

        // Fetch transaction history for the current user
        List<Transaction> transactions = transactionController.getTransactionsByUser(user.getEmail());

        // Define column names for the JTable
        String[] columnNames = {"NO.", "Name", "Transaction's id.", "Book's id", "Book's Title", "Issued date", "Due date", "Status"};

        // Create a DefaultTableModel to hold the data
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        int rowID = 0;
        // Populate the table model with transaction data
        for (Transaction transaction : transactions) {
            rowID ++;
            Object[] rowData = {
                    rowID,
                    user.getName(), // Name
                    transaction.getId(), // NO. (Transaction ID as String)
                    transaction.getBookId(), // Book's id (String)
                    getBookTitle(transaction.getBookId()), // Book's Title
                    transaction.getBorrowDate(), // Issued date
                    transaction.getDueDate(), // Due date
                    transaction.isReturned() ? "Returned" : "Pending" // Status
            };
            tableModel.addRow(rowData);
        }

        // Create a JTable with the populated table model
        JTable transactionTable = new JTable(tableModel);

        // Customize the table appearance
        customizeTable(transactionTable);

        // Add the table to a JScrollPane for scrolling
        JScrollPane scrollPane = new JScrollPane(transactionTable);

        // Add the scroll pane to the main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }


    // Helper method to customize the table appearance
    private void customizeTable(JTable table) {
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Name
        table.getColumnModel().getColumn(1).setPreferredWidth(50);  // NO.
        table.getColumnModel().getColumn(2).setPreferredWidth(50);  // Book's id
        table.getColumnModel().getColumn(3).setPreferredWidth(300); // Book's Title
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Issued date
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Due date
        table.getColumnModel().getColumn(6).setPreferredWidth(80);  // Status

        // Center-align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Enable row striping (alternate row colors)
        table.setRowHeight(30); // Set row height
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230)); // Light gray grid lines
        table.setIntercellSpacing(new Dimension(10, 10)); // Add padding between cells

        // Customize table header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14)); // Bold font for header
        header.setBackground(new Color(90, 160, 255)); // Blue background for header
        header.setForeground(Color.WHITE); // White text for header
    }

    // Helper method to get the book title by book ID
    private String getBookTitle(String bookId) {
        Book book = new BookController().getBooksByID(bookId);
        return book != null ? book.getTitle() : "Unknown";
    }

    // Method to handle logout
    private void handleLogout() {
        cardLayout.show(cardPanel, "Login"); // Switch to the login panel
        JOptionPane.showMessageDialog(this, "Logged out successfully!", "Logout", JOptionPane.INFORMATION_MESSAGE); // Show logout message
    }
}