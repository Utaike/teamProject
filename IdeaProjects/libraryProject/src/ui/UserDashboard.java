package ui;

import controllers.MenuController;
import models.User;
import models.Book;
import controllers.BookController;
import org.apache.pdfbox.pdmodel.PDDocument; // For handling PDF files
import ui.components.*;
import ui.components.Menu;
import utils.createStyledButton;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.ImageLoader.loadImageIcon;

public class UserDashboard extends JPanel {
    // Instance variables
    private final User user; // Current logged-in user
    private final BookController bookController; // Controller for managing books
    private final CardLayout cardLayout; // Layout manager for switching panels
    private final JPanel cardPanel; // Main panel that holds all other panels
    private final MenuController menuController; // Controller for handling menu actions
    private final Map<Book, BookCard> bookCardMap = new HashMap<>(); // Track BookCard instances

    // Constructor
    public UserDashboard(User user, CardLayout cardLayout, JPanel cardPanel, BookController bookController) {
        this.user = user;
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.bookController = bookController;
        this.menuController = new MenuController(this.user, cardLayout, cardPanel);

        // Set the layout of the dashboard to BorderLayout
        setLayout(new BorderLayout());

        // Add the header to the top of the dashboard
        add(new Header("Imagine Library", user.getName(), this::handleLogout), BorderLayout.NORTH);

        // Create the main content panel
        JPanel mainContent = new JPanel(new BorderLayout());

        // Define menu items and add the menu to the left side of the dashboard
        String[] menuItems = {"Home", "View profile", "Borrow Book", "Return Book", "Borrowed books", "Back to previous", "New Arrivals"};
        mainContent.add(new Menu(menuItems, menuController::handleMenuButtonClick), BorderLayout.WEST);

        // Add the main content (search bar and book list) to the center of the dashboard
        mainContent.add(createMainContent(), BorderLayout.CENTER);

        // Add the main content panel to the dashboard
        add(mainContent, BorderLayout.CENTER);
    }

    // Method to create the main content panel (search bar and book list)
    private JPanel createMainContent() {
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BorderLayout());
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel to hold the search bar and book list
        JPanel bookContentPanel = new JPanel();
        bookContentPanel.setLayout(new BoxLayout(bookContentPanel, BoxLayout.Y_AXIS));
        bookContentPanel.setBackground(Color.WHITE);

        // Fetch all books and group them by genre
        Map<String, List<Book>> booksByGenre = groupBooksByGenre(bookController.getAllBooks());

        // Add genre sections to the book content panel
        for (Map.Entry<String, List<Book>> entry : booksByGenre.entrySet()) {
            JPanel genrePanel = createGenreSection(entry.getKey(), entry.getValue());
            bookContentPanel.add(genrePanel);
        }

        // Wrap the book content in a scroll pane for vertical scrolling
        JScrollPane scrollPane = new JScrollPane(bookContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Create and add the search bar at the top of the main content
        SearchBar searchBar = new SearchBar(bookController, results -> {
            // Clear and refresh the book content panel with search results
            bookContentPanel.removeAll();
            if (results.isEmpty()) {
                JLabel noResultsLabel = new JLabel("No books found for the given search criteria.");
                noResultsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                noResultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
                bookContentPanel.add(noResultsLabel);
            } else {
                Map<String, List<Book>> searchResultsByGenre = groupBooksByGenre(results);
                for (Map.Entry<String, List<Book>> entry : searchResultsByGenre.entrySet()) {
                    JPanel genrePanel = createGenreSection(entry.getKey(), entry.getValue());
                    bookContentPanel.add(genrePanel);
                }
            }
            bookContentPanel.revalidate();
            bookContentPanel.repaint();
        });

        mainContent.add(searchBar, BorderLayout.NORTH);
        mainContent.add(scrollPane, BorderLayout.CENTER);

        return mainContent;
    }

    // Helper method to group books by genre
    private Map<String, List<Book>> groupBooksByGenre(List<Book> books) {
        Map<String, List<Book>> booksByGenre = new HashMap<>();
        for (Book book : books) {
            booksByGenre.computeIfAbsent(book.getGenre(), genre -> new ArrayList<>()).add(book);
        }
        return booksByGenre;
    }

    // Method to create a genre section with books and pagination
    private JPanel createGenreSection(String genre, List<Book> books) {
        JPanel genrePanel = new JPanel(new BorderLayout());
        genrePanel.setBackground(Color.WHITE);

        // Create a panel for the genre title and the "View" button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);

        // Add the genre title to the left side of the title panel
        JLabel genreLabel = new JLabel(genre);
        genreLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Set font and size
        genreLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Add padding
        titlePanel.add(genreLabel, BorderLayout.WEST);

        // Add the "View" button to the right side of the title panel
        JButton viewButton = createStyledButton.create("View All", new Color(90, 160, 255));
        viewButton.addActionListener(e -> {
            AllBooks allBooks = new AllBooks(genre, books, cardLayout, cardPanel, user, menuController, bookController, "UserDashboard");
            cardPanel.add(allBooks, "AllBooks");
            cardLayout.show(cardPanel, "AllBooks");
        });
        titlePanel.add(viewButton, BorderLayout.EAST);

        // Add the title panel to the top of the genre panel
        genrePanel.add(titlePanel, BorderLayout.NORTH);

        // Create a panel to hold the list of books
        JPanel bookListPanel = new JPanel();
        bookListPanel.setLayout(new BoxLayout(bookListPanel, BoxLayout.X_AXIS)); // Horizontal layout
        bookListPanel.setBackground(Color.WHITE);

        // Pagination variables
        int booksPerPage = 6; // Number of books to display per page
        int totalPages = (int) Math.ceil((double) books.size() / booksPerPage); // Calculate total pages
        final int[] currentPage = {0}; // Track the current page

        // Function to update the displayed books based on the current page
        Runnable updateBooks = () -> {
            bookListPanel.removeAll(); // Clear the current books
            int start = currentPage[0] * booksPerPage; // Calculate the start index
            int end = Math.min(start + booksPerPage, books.size()); // Calculate the end index
            for (int i = start; i < end; i++) {
                Book book = books.get(i);
                // Create the BookCard with separate actions for Read and View Details
                BookCard bookCard = new BookCard(
                        book, // Pass the book object
                        () -> openPDF(book.getLink()), // Read action: open PDF
                        () -> showBookDetails(book),       // View Details action: navigate to book details
                        "UserDashboard" // Pass the frame type
                );

                bookListPanel.add(bookCard); // Add the book card to the panel
                bookCardMap.put(book, bookCard); // Track the BookCard in the map
            }
            bookListPanel.revalidate(); // Refresh the panel
            bookListPanel.repaint(); // Repaint the panel
        };

        // Display the initial set of books
        updateBooks.run();

        // Create pagination controls (Prev, page numbers, Next)
        JPanel paginationPanel = new JPanel();
        paginationPanel.setBackground(Color.white);
        JButton prevButton = createStyledButton.create("Prev", new Color(90, 160, 255));
        JButton nextButton = createStyledButton.create("Next", new Color(90, 160, 255));
        paginationPanel.add(prevButton);

        // Add page number buttons (e.g., 1, 2, 3, etc.)
        for (int i = 0; i < totalPages; i++) {
            JButton pageButton = createStyledButton.create(String.valueOf(i + 1), new Color(90, 160, 255)); // Page numbers start from 1
            int pageIndex = i; // Store the page index for the action listener
            pageButton.addActionListener(e -> {
                currentPage[0] = pageIndex; // Update the current page
                updateBooks.run(); // Refresh the book list
            });
            paginationPanel.add(pageButton);
        }

        paginationPanel.add(nextButton);

        // Add action listeners for the Prev and Next buttons
        prevButton.addActionListener(e -> {
            if (currentPage[0] > 0) {
                currentPage[0]--; // Go to the previous page
                updateBooks.run(); // Refresh the book list
            }
        });

        nextButton.addActionListener(e -> {
            if (currentPage[0] < totalPages - 1) {
                currentPage[0]++; // Go to the next page
                updateBooks.run(); // Refresh the book list
            }
        });

        // Add the book list panel and pagination controls to the genre panel
        genrePanel.add(bookListPanel, BorderLayout.CENTER);
        genrePanel.add(paginationPanel, BorderLayout.SOUTH);

        return genrePanel;
    }

    // Method to show book details
    private void showBookDetails(Book book) {
        // Pass the frame type to BookDetails
        BookDetails detailsScreen = new BookDetails(book, cardLayout, cardPanel, user, menuController, bookController, "UserDashboard");
        cardPanel.add(detailsScreen, "BookDetails");
        cardLayout.show(cardPanel, "BookDetails");
    }

    // Method to handle logout
    private void handleLogout() {
        cardLayout.show(cardPanel, "Login"); // Switch to the login panel
        JOptionPane.showMessageDialog(this, "Logged out successfully!", "Logout", JOptionPane.INFORMATION_MESSAGE); // Show logout message
    }

    // Method to open a PDF file
    private void openPDF(String pdfPath) {
        // Check if the PDF path is valid
        if (pdfPath == null || pdfPath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "The PDF file path is empty or invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if the PDF file exists
        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists() || !pdfFile.isFile()) {
            JOptionPane.showMessageDialog(this, "The PDF file does not exist or is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Load the PDF document to check if it's valid
            PDDocument document = PDDocument.load(pdfFile);
            document.close();

            // Open the PDF viewer
            PDFViewer pdfViewer = new PDFViewer(pdfPath, cardLayout, cardPanel);
            cardPanel.add(pdfViewer, "PDFViewer");
            cardLayout.show(cardPanel, "PDFViewer"); // Switch to the PDF viewer panel

        } catch (IOException e) {
            // Handle errors when opening the PDF
            JOptionPane.showMessageDialog(this, "Error opening the PDF file: The file may be corrupted or inaccessible.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}