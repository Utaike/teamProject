package ui;

import Listener.TransactionListener;
import controllers.BookController;
import controllers.MenuController;
import controllers.TransactionController;
import models.Book;
import models.Transaction;
import models.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import ui.components.*;
import ui.components.Menu;
import utils.BorrowBookUtil;
import utils.createStyledButton;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AllBooks extends JPanel implements TransactionListener {
    private static final String FRAME_TYPE_USER = "UserDashboard";
    private static final String FRAME_TYPE_BORROW = "BorrowBookFrame";
    private static final int BOOKS_PER_PAGE = 9; // Adjust based on your layout

    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final User user;
    private final MenuController menuController;
    private final String genre;
    private final List<Book> books;
    private final BookController bookController;
    private JPanel bookListPanel;
    private int currentPage = 0;
    private final Map<Book, BookCard> bookCardMap = new HashMap<>(); // Track BookCard instances
    private final TransactionController transactionController;
    private final String frameType; // "UserDashboard" or "BorrowBookFrame"

    public AllBooks(String genre, List<Book> books, CardLayout cardLayout, JPanel cardPanel, User user,
                    MenuController menuController, BookController bookController, String frameType) {
        this.genre = genre;
        this.books = books;
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.user = user;
        this.menuController = menuController;
        this.bookController = bookController;
        this.transactionController = new TransactionController();
        this.frameType = frameType;

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Header
        add(new Header("Imagine Library", user, this::handleLogout), BorderLayout.NORTH);

        // Main Content
        JPanel mainContent = new JPanel(new BorderLayout());
        String[] menuItems = {"Home", "View Profile", "Borrow Book", "Return Book", "Borrowed Books", "Back to previous", "New Arrivals"};
        String[] iconPaths = {
                "IdeaProjects/libraryProject/src/images/icons/home.png",       // Path to home icon
                "IdeaProjects/libraryProject/src/images/icons/profile.png",    // Path to profile icon
                "IdeaProjects/libraryProject/src/images/icons/avialableBooks.png",      // Path to books icon
                "IdeaProjects/libraryProject/src/images/icons/users.png",      // Path to users icon
                "IdeaProjects/libraryProject/src/images/icons/books.png", // Path to transactions icon
                "IdeaProjects/libraryProject/src/images/icons/arrow.png",      // Path to users icon
                "IdeaProjects/libraryProject/src/images/icons/settings.png"
        };
        mainContent.add(new Menu(menuItems, iconPaths,menuController::handleMenuButtonClick), BorderLayout.WEST);

        mainContent.add(createMainContent(), BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Welcome Label
        JLabel welcomeLabel = new JLabel("Welcome to Imagine Library");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        // Search Bar
        SearchBar searchBar = new SearchBar(bookController, results -> {
            updateBookList(results); // Update the book list with filtered results
        });
        mainPanel.add(searchBar, BorderLayout.NORTH);

        // Book List Panel
        bookListPanel = new JPanel(new GridLayout(0, 3, 10, 10)); // 3 columns, with 10px horizontal and vertical gaps
        bookListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        updateBookListPanel();

        JScrollPane scrollPane = new JScrollPane(bookListPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Pagination Panel
        JPanel paginationPanel = createPaginationPanel();
        mainPanel.add(paginationPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createPaginationPanel() {
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton prevButton = createStyledButton.create("Prev", new Color(90, 160, 255));
        prevButton.addActionListener(e -> navigatePage(-1));
        JButton nextButton = createStyledButton.create("Next", new Color(90, 160, 255));
        nextButton.addActionListener(e -> navigatePage(1));
        paginationPanel.add(prevButton);
        paginationPanel.add(nextButton);
        return paginationPanel;
    }

    private void navigatePage(int direction) {
        int newPage = currentPage + direction;
        if (newPage >= 0 && newPage * BOOKS_PER_PAGE < books.size()) {
            currentPage = newPage;
            updateBookListPanel();
        }
    }

    private void updateBookListPanel() {
        bookListPanel.removeAll();
        int start = currentPage * BOOKS_PER_PAGE;
        int end = Math.min(start + BOOKS_PER_PAGE, books.size());

        for (int i = start; i < end; i++) {
            Book book = books.get(i);
            String buttonText = determineButtonText(book);
            boolean isButtonEnabled = determineButtonEnabled(book);

            BookCard bookCard = new BookCard(
                    book,
                    () -> handleButtonAction(book, buttonText),
                    () -> showBookDetails(book),
                    frameType
            );

            bookCard.setButtonText(buttonText);
            bookCard.setButtonEnabled(isButtonEnabled);

            bookListPanel.add(bookCard);
            bookCardMap.put(book, bookCard);
        }

        bookListPanel.revalidate();
        bookListPanel.repaint();
    }

    private String determineButtonText(Book book) {
        if (!book.isAvailable()) {
            return "Unavailable"; // Show "Unavailable" if the book is not available
        } else if (FRAME_TYPE_BORROW.equals(frameType)) {
            return "Borrow"; // Always show "Borrow" in the BorrowBookFrame
        } else if (FRAME_TYPE_USER.equals(frameType)) {
            return "Read"; // Show "Read" in the UserDashboard
        }
        return ""; // Default label
    }

    private boolean determineButtonEnabled(Book book) {
        if (!book.isAvailable()) {
            return false; // Disable the button if the book is unavailable
        } else if (FRAME_TYPE_BORROW.equals(frameType)) {
            return book.isAvailable(); // Enable button only if the book is available in the BorrowBookFrame
        } else if (FRAME_TYPE_USER.equals(frameType)) {
            return true; // Always enable the button in the UserDashboard
        }
        return false; // Default behavior
    }

    private void handleButtonAction(Book book, String buttonText) {
        if (!book.isAvailable() && "Borrow".equals(buttonText)) {
            JOptionPane.showMessageDialog(this, "This book is currently unavailable.", "Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }
        switch (buttonText) {
            case "Borrow":
                borrowBook(book);
                break;
            case "Read":
                openPDFOrURL(book.getLink());
                break;
            default:
                JOptionPane.showMessageDialog(this, "Action not available.", "Error", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    private void borrowBook(Book book) {
        BorrowBookUtil.borrowBook(book, user, transactionController, this, this);
    }
    // Method to open a PDF file or URL
    private void openPDFOrURL(String path) {
        if (path == null || path.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "The path is empty or invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if the path is a URL
        if (isValidURL(path)) {
            openURL(path); // Open the URL in the default browser
        }
        // Check if the path is a valid PDF file
        else if (isValidPDF(path)) {
            openPDF(path); // Open the PDF file
        }
        // If neither, show an error
        else {
            JOptionPane.showMessageDialog(this, "The path is neither a valid URL nor a valid PDF file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // Helper method to validate if a string is a valid URL
    private boolean isValidURL(String path) {
        try {
            URI uri = new URI(path);
            // Ensure the URI has a scheme (e.g., http, https, ftp)
            return uri.getScheme() != null && (uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https"));
        } catch (URISyntaxException e) {
            return false; // Not a valid URL
        }
    }
    // Helper method to validate if a string is a valid PDF file path
    private boolean isValidPDF(String path) {
        // Ensure the path ends with .pdf and the file exists
        return path.toLowerCase().endsWith(".pdf") && new File(path).exists();
    }
    // Method to open a URL in the default browser
    private void openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url)); // Open the URL
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening the URL: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // Method to open a PDF file
    private void openPDF(String pdfPath) {
        try {
            PDFViewer pdfViewer = new PDFViewer(pdfPath, cardLayout, cardPanel);
            cardPanel.add(pdfViewer, "PDFViewer");
            cardLayout.show(cardPanel, "PDFViewer");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening the PDF file: The file may be corrupted or inaccessible.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void updateBookList(List<Book> results) {
        // Clear the current list of books
        books.clear();

        // Filter the results by genre
        for (Book book : results) {
            if (genre.equals(book.getGenre())) { // Check if the book's genre matches
                books.add(book); // Add the book to the list if it matches
            }
        }
        // Reset the current page to 0 and update the UI
        currentPage = 0;
        updateBookListPanel();
    }

    private void showBookDetails(Book book) {
        String buttonText = determineButtonText(book);
        boolean isButtonEnabled = determineButtonEnabled(book);

        BookDetails detailsScreen = new BookDetails(book, cardLayout, cardPanel, user, menuController, bookController, frameType);
        cardPanel.add(detailsScreen, "BookDetails");
        cardLayout.show(cardPanel, "BookDetails");
    }

    private void handleLogout() {
        cardLayout.show(cardPanel, "Login");
        JOptionPane.showMessageDialog(this, "Logged out successfully!", "Logout", JOptionPane.INFORMATION_MESSAGE);
    }
    @Override
    public void onReturnSuccess(Transaction transaction) {}
    @Override
    public void onReturnFailure(String errorMessage) {}
    @Override
    public void onRejection(Transaction transaction) {}

    @Override
    public void onAdminApproval(Transaction transaction) {
        // Show success message
        JOptionPane.showMessageDialog(this, "Borrowing request successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        // Refresh the UI
        updateBookListPanel();
    }
}