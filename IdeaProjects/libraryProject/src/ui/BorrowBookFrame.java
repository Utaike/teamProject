package ui;

import utils.BorrowBookUtil;
import utils.ImageLoader;
import utils.createStyledButton;
import controllers.BookController;
import controllers.MenuController;
import controllers.TransactionController;
import Listener.TransactionListener;
import models.Book;
import models.Transaction;
import models.User;
import ui.components.BookCard;
import ui.components.Header;
import ui.components.Menu;
import ui.components.SearchBar;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BorrowBookFrame extends JPanel implements TransactionListener {
    private final User user;
    private final BookController bookController;
    private final TransactionController transactionController;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final MenuController menuController;
    private final Map<Book, BookCard> bookCardMap = new HashMap<>(); // Track BookCard instances
    private JPanel bookContentPanel; // Panel to hold the search bar and book list

    public BorrowBookFrame(User user, CardLayout cardLayout, JPanel cardPanel, BookController bookController, TransactionController transactionController) {
        this.user = user;
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.bookController = bookController;
        this.menuController = new MenuController(this.user, cardLayout, cardPanel);
        this.transactionController = transactionController;

        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240)); // Light gray background
        add(new Header("Imagine Library", user, this::handleLogout), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(240, 240, 240));
        String[] menuItems = {"Home", "View profile", "Borrow Book", "Return Book", "Borrowed books", "Back to previous", "New Arrivals"};
        mainContent.add(new Menu(menuItems, menuController::handleMenuButtonClick), BorderLayout.WEST);
        mainContent.add(createMainContent(), BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createMainContent() {
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(240, 240, 240));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel to hold the search bar and book list
        bookContentPanel = new JPanel();
        bookContentPanel.setLayout(new BoxLayout(bookContentPanel, BoxLayout.Y_AXIS));
        bookContentPanel.setBackground(new Color(240, 240, 240));

        // Fetch all books and group them by genre
        Map<String, List<Book>> booksByGenre = groupAvailableBooksByGenre(bookController.getAllBooks());

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
                Map<String, List<Book>> searchResultsByGenre = groupAvailableBooksByGenre(results);
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

    private Map<String, List<Book>> groupAvailableBooksByGenre(List<Book> books) {
        Map<String, List<Book>> booksByGenre = new HashMap<>();
        for (Book book : books) {
            if (book.isAvailable()) {
                booksByGenre.computeIfAbsent(book.getGenre(), genre -> new ArrayList<>()).add(book);
            }
        }
        return booksByGenre;
    }

    private JPanel createGenreSection(String genre, List<Book> books) {
        JPanel genrePanel = new JPanel(new BorderLayout());
        genrePanel.setBackground(new Color(240, 240, 240));
        genrePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Create a panel for the genre label and "View All" button
        JPanel genreHeaderPanel = new JPanel(new BorderLayout());
        genreHeaderPanel.setBackground(new Color(240, 240, 240));

        JLabel genreLabel = new JLabel(genre);
        genreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        genreLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        genreHeaderPanel.add(genreLabel, BorderLayout.WEST);

        // Create and add the "View All" button
        JButton viewAllButton = createStyledButton.create("View All", new Color(90, 160, 255));
        viewAllButton.addActionListener(_ -> {
            // Navigate to the AllBooks frame with the selected genre and books
            AllBooks allBooksFrame = new AllBooks(genre, books, cardLayout, cardPanel, user, menuController, bookController, "BorrowBookFrame");
            cardPanel.add(allBooksFrame, "AllBooks");
            cardLayout.show(cardPanel, "AllBooks");
        });
        genreHeaderPanel.add(viewAllButton, BorderLayout.EAST);

        genrePanel.add(genreHeaderPanel, BorderLayout.NORTH);

        JPanel bookListPanel = new JPanel();
        bookListPanel.setLayout(new BoxLayout(bookListPanel, BoxLayout.X_AXIS));
        bookListPanel.setBackground(new Color(240, 240, 240));

        int booksPerPage = 6;
        int totalPages = (int) Math.ceil((double) books.size() / booksPerPage);
        final int[] currentPage = {0};

        Runnable updateBooks = () -> {
            bookListPanel.removeAll();
            int start = currentPage[0] * booksPerPage;
            int end = Math.min(start + booksPerPage, books.size());
            for (int i = start; i < end; i++) {
                Book book = books.get(i);
                BookCard bookCard = new BookCard(book, () -> borrowBook(book), () -> showBookDetails(book), "BorrowBookFrame");
                bookListPanel.add(bookCard);
                bookCardMap.put(book, bookCard); // Track the BookCard
            }
            bookListPanel.revalidate();
            bookListPanel.repaint();
        };

        updateBooks.run();

        JPanel paginationPanel = new JPanel();
        paginationPanel.setBackground(new Color(240, 240, 240));
        JButton prevButton = createStyledButton.create("Prev", new Color(90, 160, 255));
        JButton nextButton = createStyledButton.create("Next", new Color(90, 160, 255));
        paginationPanel.add(prevButton);

        for (int i = 0; i < totalPages; i++) {
            JButton pageButton = createStyledButton.create(String.valueOf(i + 1), new Color(90, 160, 255));
            int pageIndex = i;
            pageButton.addActionListener(_ -> {
                currentPage[0] = pageIndex;
                updateBooks.run();
            });
            paginationPanel.add(pageButton);
        }

        paginationPanel.add(nextButton);

        prevButton.addActionListener(_ -> {
            if (currentPage[0] > 0) {
                currentPage[0]--;
                updateBooks.run();
            }
        });

        nextButton.addActionListener(_ -> {
            if (currentPage[0] < totalPages - 1) {
                currentPage[0]++;
                updateBooks.run();
            }
        });

        genrePanel.add(bookListPanel, BorderLayout.CENTER);
        genrePanel.add(paginationPanel, BorderLayout.SOUTH);

        return genrePanel;
    }

    private void borrowBook(Book book) {
        BorrowBookUtil.borrowBook(book, user, transactionController, this, this);
    }

    private void showBookDetails(Book book) {
        // Pass the frame type to BookDetails
        BookDetails detailsScreen = new BookDetails(book, cardLayout, cardPanel, user, menuController, bookController, "BorrowBookFrame");
        cardPanel.add(detailsScreen, "BookDetails");
        cardLayout.show(cardPanel, "BookDetails");
    }

    private void handleLogout() {
        cardLayout.show(cardPanel, "Login");
        JOptionPane.showMessageDialog(this, "Logged out successfully!", "Logout", JOptionPane.INFORMATION_MESSAGE);
    }
    @Override
    public void onAdminApproval(Transaction transaction) {
        // Get the borrowed book
        Book borrowedBook = bookController.getBooksByID(transaction.getBookId());

        // Remove the BookCard from the UI
        BookCard bookCard = bookCardMap.get(borrowedBook);
        if (bookCard != null) {
            Container parent = bookCard.getParent();
            if (parent != null) {
                parent.remove(bookCard);
                parent.revalidate();
                parent.repaint();
            }

            // Remove the book from the bookCardMap
            bookCardMap.remove(borrowedBook);
        }
    }
    @Override
    public void onReturnSuccess(Transaction transaction) {}
    @Override
    public void onReturnFailure(String errorMessage) {}
    @Override
    public void onRejection(Transaction transaction) {}
}