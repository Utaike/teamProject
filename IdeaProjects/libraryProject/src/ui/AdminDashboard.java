
package ui;

import controllers.AdminController;
import controllers.AdminMenuController;
import models.Book;
import models.User;
import models.Visitor;
import ui.components.CreateTablePanel;
import ui.components.Header;
import ui.components.Menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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

        // Add the header
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
                () -> innerCardLayout.show(innerCardPanel, "Home"), // Navigate to Home
                () -> innerCardLayout.show(innerCardPanel, "ManageBooks"), // Navigate to Manage Books
                () -> innerCardLayout.show(innerCardPanel, "ManageUsers"), // Navigate to Manage Users
                () -> innerCardLayout.show(innerCardPanel, "BorrowedHistory") // Navigate to Borrowed History
        );

        // Add buttons to the sidebar
        String[] menuItems = {"Home", "View Profile", "Manage Books", "Manage Users", "Borrowed History"};
        Menu menu =new Menu(menuItems,adminMenuController::handleMenuButtonClick);
        sideBar.add(menu);
        return sideBar;
    }


    private void handleLogout() {
        cardLayout.show(cardPanel, "Login");
        JOptionPane.showMessageDialog(this, "Logged out successfully!", "Logout", JOptionPane.INFORMATION_MESSAGE); // Show logout message
    }
    private JPanel createManageUsersPanel(){
        return new ManageUsersPanel(adminController);
    }

    private JPanel createHomePanel() {
        // Load icons (replace with your actual image paths)
        ImageIcon usersIcon = scaleIcon("src/images/icons/users.png",50,50);
        ImageIcon booksIcon = scaleIcon("src/images/icons/books.png",50,50);
        ImageIcon genresIcon = scaleIcon("src/images/icons/books.png",50,50);
        ImageIcon availableBooksIcon = scaleIcon("src/images/icons/avialableBooks.png",50,50);
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)); // Padding

        // Create a panel for the cards (top part)
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 10)); // 2x2 grid layout
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0)); // Add bottom margin

        // Fetch data from the AdminController
        int totalUsers = adminController.totalUsers();
        int totalBooks = adminController.totalBooks();
        int totalGenre = adminController.totalGenre();
        int totalAvailableBooks = adminController.totalAvailableBooks();

        int totalBorrowedBooks =adminController.totalBorrowedBooks();
        List<User> users = adminController.allUsers();
        List<Book> books = adminController.allBooks();

        // Create statistical cards with click actions
        statsPanel.add((createStatCard("Total Users", String.valueOf(totalUsers), usersIcon, () -> {
            JPanel userTablePanel= createUserTablePanel();
            innerCardPanel.add(userTablePanel,"UserTable");
            innerCardLayout.show(innerCardPanel, "UserTable");
        })));

        statsPanel.add((createStatCard("Total Books", String.valueOf(totalBooks), booksIcon, () -> {
            JPanel bookTablePanel=createBookTablePanel();
            innerCardPanel.add(bookTablePanel,"BookTable");
            innerCardLayout.show(innerCardPanel, "BookTable");
        })));

        statsPanel.add((createStatCard("Total Genres", String.valueOf(totalGenre), genresIcon, () -> {
            JPanel genreTable= createGenreTablePanel();
            innerCardPanel.add(genreTable,"GenreTable");
            innerCardLayout.show(innerCardPanel, "GenreTable");
        })));

        statsPanel.add((createStatCard("Total Available Books", String.valueOf(totalAvailableBooks), availableBooksIcon, () -> {
            JPanel availableBookTable=createAvailableBooksTablePanel();
            innerCardPanel.add(availableBookTable,"AvailableBookTable");
            innerCardLayout.show(innerCardPanel, "AvailableBookTable");
        })));
        statsPanel.add((createStatCard("Total Borrowed Books", String.valueOf(totalBorrowedBooks), availableBooksIcon, () -> {
            JPanel borrowedBooksTable = new CreateTablePanel();
            innerCardPanel.add(borrowedBooksTable,"BorrowedBooksTable");
            innerCardLayout.show(cardPanel, "BorrowedBooksTable");
        })));
        mainContent.add(statsPanel, BorderLayout.NORTH);
        return mainContent;
    }

    private JPanel createUserTablePanel() {
        List<User> users = adminController.allUsers();
        String[] userColumns = {"Name", "Email", "Role"};

        CreateTablePanel tablePanelCreator = new CreateTablePanel();
        return tablePanelCreator.createTablePanel(
                "Users",
                users,
                userColumns,
                user -> new Object[]{user.getName(), user.getEmail(), user.getRole()}, // rowMapper for User
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
