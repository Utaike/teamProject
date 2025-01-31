
package ui;

import controllers.AdminController;
import controllers.AdminMenuController;
import controllers.TransactionController;
import models.Book;
import models.Transaction;
import models.User;
import ui.components.*;
import ui.components.Menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends JPanel {
    private final User user;
    private final AdminController adminController;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final CardLayout innerCardLayout;
    private final JPanel innerCardPanel;



    // Constants for colors, fonts, and padding
    private static final Color CARD_BACKGROUND = new Color(230, 230, 250);
    private static final Color CARD_HOVER_BACKGROUND = new Color(210, 210, 230);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font VALUE_FONT = new Font("Arial", Font.PLAIN, 24);
    private static final int PADDING = 20;

    public AdminDashboard(User user, AdminController adminController, CardLayout cardLayout, JPanel cardPanel) {
        this.user = user;
        this.adminController = adminController;
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
//        this.adminMenuController = new AdminMenuController(this.user, adminController, cardLayout, cardPanel);

        setLayout(new BorderLayout());
        JPanel header =(new Header("Imagine Library", user, this::handleLogout));

        add(header, BorderLayout.NORTH);

        JPanel sideBar=createSidebar();
        add(sideBar,BorderLayout.WEST);
        innerCardLayout =new CardLayout();
        innerCardPanel=new JPanel(innerCardLayout);
        add(innerCardPanel,BorderLayout.CENTER);
        initializeMainContentPanels();

    }
    private void initializeMainContentPanels() {
        // Add panels to the main content area
        innerCardPanel.add(createHomePanel(), "Home");
        innerCardPanel.add(createManageUsersPanel(), "ManageUsers");
        innerCardPanel.add(createManageTransactionPanel(),"ManageTransactions");
        innerCardPanel.add(createBooksPanel(),"ManageBooks");
        innerCardLayout.show(innerCardPanel, "Home");
    }
    private JPanel createSidebar() {
        JPanel sideBar = new JPanel();
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create the AdminMenuController with navigation callbacks
        AdminMenuController adminMenuController = new AdminMenuController(
                user,
                adminController,
                () -> {
                    innerCardLayout.show(innerCardPanel, "Home");
                    refreshStatsCards(); // Force refresh when navigating to Home
                },
                () -> innerCardLayout.show(innerCardPanel, "ManageBooks"), // Navigate to Manage Books
                () -> innerCardLayout.show(innerCardPanel, "ManageUsers"), // Navigate to Manage Users
                () -> innerCardLayout.show(innerCardPanel, "ManageTransactions") // Navigate to Borrowed History
        );
        String[] menuItems = {"Home", "View Profile", "Manage Books", "Manage Users", "Manage Transactions"};
        String[] iconPaths = {
                "IdeaProjects/libraryProject/src/images/icons/home.png",       // Path to home icon
                "IdeaProjects/libraryProject/src/images/icons/profile.png",    // Path to profile icon
                "IdeaProjects/libraryProject/src/images/icons/avialableBooks.png",      // Path to books icon
                "IdeaProjects/libraryProject/src/images/icons/users.png",      // Path to users icon
                "IdeaProjects/libraryProject/src/images/icons/books.png" // Path to transactions icon
        };
        Menu menu =new Menu(menuItems,iconPaths,adminMenuController::handleMenuButtonClick);
        sideBar.add(menu);
        return sideBar;
    }
    public void refreshStatsCards() {
        SwingUtilities.invokeLater(() -> {
            // Remove the old Home panel
            innerCardPanel.removeAll();

            // Recreate and re-add all panels (including the new Home panel)
            initializeMainContentPanels();

            // Force the layout to update
            innerCardPanel.revalidate();
            innerCardPanel.repaint();
        });
    }


    private void handleLogout() {
        cardLayout.show(cardPanel, "Login");

        // Get the parent window and disable resizing
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;

            // Set the size of the JFrame to the desired dimensions for the login panel
            frame.setSize(1000, 700); // Set this to your desired size for the login panel

            // Make the JFrame non-resizable
            frame.setResizable(false);

            // Optionally, you can also center the frame
            frame.setLocationRelativeTo(null);
        }
        JOptionPane.showMessageDialog(this, "Logged out successfully!", "Logout", JOptionPane.INFORMATION_MESSAGE);
    }
    private JPanel createBooksPanel(){
        return new ManageBooksPanel(adminController,this);
    }
    private JPanel createManageUsersPanel(){

        return new ManageUsersPanel(adminController,this);
    }
    private JPanel createManageTransactionPanel(){
        return new ManageTransactionPanel(adminController);
    }

    private JPanel createHomePanel() {
        JPanel homePale = new JPanel(new BorderLayout());
        homePale.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        homePale.setName("Home");
        // Create a panel for the cards (top part)
        JPanel statsPanel = createStatsPanel();
        homePale.add(statsPanel, BorderLayout.NORTH);

        // Create a tabbed pane to organize the graphs and borrowing requests
        JTabbedPane tabbedPane = new JTabbedPane();

        // Add the graphs panel to the first tab
        JPanel graphPanel = new JPanel(new GridLayout(1, 3, 10, 10)); // 1 row, 3 columns, with 10px gaps

        // Add the registration graph panel
        Map<LocalDate, Integer> registrationsPerDay = adminController.getRegisterPerDay();
        System.out.println("Registrations per day in home panel: " + registrationsPerDay); // Debugging
        JPanel registrationGraphPanel = new RegistrationGraphPanel(registrationsPerDay);
        registrationGraphPanel.setPreferredSize(new Dimension(400, 300)); // Set fixed size
        graphPanel.add(registrationGraphPanel);

        // Add the role distribution pie chart panel
        Map<String, Integer> roleDistribution = adminController.getRoleDistribution();
        System.out.println("Role distribution in home panel: " + roleDistribution); // Debugging
        JPanel rolePieChartPanel = new PieChartPanel(roleDistribution, "User Role Distribution");
        rolePieChartPanel.setPreferredSize(new Dimension(400, 300)); // Set fixed size
        graphPanel.add(rolePieChartPanel);

        // Add the book status pie chart panel
        int borrowedBooks = adminController.totalBorrowedBooks();
        int availableBooks = adminController.totalAvailableBooks();
        System.out.println("Borrowed books: " + borrowedBooks + ", Available books: " + availableBooks); // Debugging
        JPanel bookPieChartPanel = new BookPieChartPanel(borrowedBooks, availableBooks);
        bookPieChartPanel.setPreferredSize(new Dimension(400, 300)); // Set fixed size
        graphPanel.add(bookPieChartPanel);

        // Add the graphs panel to the first tab
        tabbedPane.addTab("Statistics", graphPanel);

        // Add the borrowing requests panel to the second tab
        BorrowingRequestsPanel borrowingRequestsPanel = new BorrowingRequestsPanel(adminController, new TransactionController());
        tabbedPane.addTab("Borrowing Requests", borrowingRequestsPanel);

        // Add the tabbed pane to the center of the main content
        homePale.add(tabbedPane, BorderLayout.CENTER);

        return homePale;
    }
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Fetch data from the AdminController
        int totalUsers = adminController.totalUsers();
        int totalBooks = adminController.totalBooks();
        int totalGenre = adminController.totalGenre();
        int totalAvailableBooks = adminController.totalAvailableBooks();
        int totalBorrowedBooks = adminController.totalBorrowedBooks();

        // Create statistical cards
        statsPanel.add(createStatCard("Total Users", String.valueOf(totalUsers), scaleIcon("IdeaProjects/libraryProject/src/images/icons/users.png", 50, 50), () -> showPanel("UserTable", createUserTablePanel())));
        statsPanel.add(createStatCard("Total Books", String.valueOf(totalBooks), scaleIcon("IdeaProjects/libraryProject/src/images/icons/books.png", 50, 50), () -> showPanel("BookTable", createBookTablePanel())));
        statsPanel.add(createStatCard("Total Genres", String.valueOf(totalGenre), scaleIcon("IdeaProjects/libraryProject/src/images/icons/books.png", 50, 50), () -> showPanel("GenreTable", createGenreTablePanel())));
        statsPanel.add(createStatCard("Total Available Books", String.valueOf(totalAvailableBooks), scaleIcon("IdeaProjects/libraryProject/src/images/icons/avialableBooks.png", 50, 50), () -> showPanel("AvailableBookTable", createAvailableBooksTablePanel())));
        statsPanel.add(createStatCard("Total Borrowed Books", String.valueOf(totalBorrowedBooks), scaleIcon("IdeaProjects/libraryProject/src/images/icons/avialableBooks.png", 50, 50), () -> showPanel("BorrowedBooksTable", createBorrowedTablePanel())));

        return statsPanel;
    }
    private void showPanel(String panelName, JPanel panel) {
        innerCardPanel.add(panel, panelName);
        innerCardLayout.show(innerCardPanel, panelName);
    }

    private JPanel createUserTablePanel() {
        List<User> users = adminController.allUsers();
        String[] userColumns = {"Name", "Email", "Role","Register Date"};

        CreateTablePanel tablePanelCreator = new CreateTablePanel();
        return tablePanelCreator.createTablePanel(
                "Users",
                users,
                userColumns,
                user -> new Object[]{user.getName(), user.getEmail(), user.getRole(),user.getRegisterDate()}, // rowMapper for User
                () -> innerCardLayout.show(innerCardPanel, "Home"), // seeAllAction
                () -> innerCardLayout.show(innerCardPanel, "Home")  // showLessAction
        );
    }

    private JPanel createBookTablePanel() {
        List<Book> books = adminController.allBooks();
        String[] bookColumns = {"ID", "Title", "Author", "Genre","Description","Status"};

        CreateTablePanel tablePanelCreator = new CreateTablePanel();
        return tablePanelCreator.createTablePanel(
                "Books",
                books,
                bookColumns,
                book -> new Object[]{book.getId(), book.getTitle(), book.getAuthor(), book.getGenre(),book.getDescription(),book.isAvailable()}, // rowMapper for Book
                () -> innerCardLayout.show(innerCardPanel, "Home"), // seeAllAction
                () -> innerCardLayout.show(innerCardPanel, "Home")  // showLessAction
        );
    }

    private JPanel createGenreTablePanel() {
        // Fetch genre counts from the AdminService
        Map<String, Integer> genreCounts = adminController.allGenres();

        // Convert the map to a list of rows for the table
        List<String[]> genreData = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : genreCounts.entrySet()) {
            genreData.add(new String[]{entry.getKey(), String.valueOf(entry.getValue())});
        }

        // Define the table columns
        String[] genreColumns = {"Genre", "Total Books"};

        // Create the table panel
        CreateTablePanel tablePanelCreator = new CreateTablePanel();
        return tablePanelCreator.createTablePanel(
                "Genres",
                genreData,
                genreColumns,
                genreRow -> genreRow, // rowMapper for Genre
                () -> innerCardLayout.show(innerCardPanel, "Home"), // seeAllAction
                () -> innerCardLayout.show(innerCardPanel, "Home")  // showLessAction
        );
    }

    private JPanel createAvailableBooksTablePanel() {
        // Fetch available books data from the AdminController (if available)
        List<Book> availableBooks = adminController.allAvailableBooks();
        String[] availableBooksColumns = {"ID", "Title", "Author", "Genre"};

        CreateTablePanel tablePanelCreator = new CreateTablePanel();
        return tablePanelCreator.createTablePanel(
                "Available Books",
                availableBooks,
                availableBooksColumns,
                book -> new Object[]{book.getId(), book.getTitle(), book.getAuthor(), book.getGenre()}, // rowMapper for Book
                () -> innerCardLayout.show(innerCardPanel, "Home"), // seeAllAction
                () -> innerCardLayout.show(innerCardPanel, "Home")  // showLessAction
        );
    }
    private JPanel createBorrowedTablePanel(){
        List<Transaction> borrowedBooks=adminController.allBorrowedBooks();
        CreateTablePanel createTablePanel=new CreateTablePanel();
        String[] borrowedBooksColumns = {"ID","Email","Book's ID","BorrowDate","DueDate","ReturnDate","Status"};
//        String bookTitle = adminController.getBookTitle(borrowedBooks.getBookId());
        return createTablePanel.createTablePanel(
                "Borrowed Books",
                borrowedBooks,
                borrowedBooksColumns,
                transaction -> new Object[]{transaction.getId(),transaction.getUserEmail(),transaction.getBookId(),transaction.getBorrowDate(),transaction.getDueDate(),transaction.getReturnDate(),transaction.isReturned()},
                ()->innerCardLayout.show(innerCardPanel,"Home"),
                ()->innerCardLayout.show(innerCardPanel,"Home")
                );
    }
    private ImageIcon scaleIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        Image image = icon.getImage(); // Get the image from the icon
        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH); // Resize image
        return new ImageIcon(scaledImage); // Return the scaled image as ImageIcon
    }

    private JPanel createStatCard(String title, String value, ImageIcon icon, Runnable onClickAction) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); // Vertical layout
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Change cursor to hand when hovering
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the icon horizontally

        // Create the title label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the title horizontally

        // Create the value label
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(VALUE_FONT);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the value horizontally

        // Add components to the card
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10)); // Add spacing between icon and title
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10)); // Add spacing between title and value
        card.add(valueLabel);

        // Add MouseListener for hover and click effects
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClickAction.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(CARD_HOVER_BACKGROUND); // Change background on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_BACKGROUND); // Restore original background
            }
        });

        return card;
    }


}
