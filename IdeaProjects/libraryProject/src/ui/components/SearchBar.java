package ui.components;

import controllers.BookController;
import models.Book;
import utils.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * A responsive search bar component that allows users to search and filter books.
 */
public class SearchBar extends JPanel {
    private final JTextField searchField;
    private final JComboBox<String> categoryFilter;
    private final JComboBox<String> authorFilter;
    private final BookController bookController;
    private final Consumer<List<Book>> onSearchResults;

    public SearchBar(BookController bookController, Consumer<List<Book>> onSearchResults) {
        this.bookController = bookController;
        this.onSearchResults = onSearchResults;

        // Initialize UI components
        searchField = createTextField();
        categoryFilter = createDropdown("All Categories");
        authorFilter = createDropdown("All Authors");

        // Set up layout and functionality
        setupLayout();
        loadFilters();
        setupListeners();
    }

    /**
     * Creates a styled search text field.
     */
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    /**
     * Creates a filter dropdown with default styling.
     */
    private JComboBox<String> createDropdown(String defaultOption) {
        JComboBox<String> dropdown = new JComboBox<>(new String[]{defaultOption});
        dropdown.setFont(new Font("Arial", Font.PLAIN, 14));
        dropdown.setBackground(Color.WHITE);
        dropdown.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        dropdown.setPreferredSize(new Dimension(150, dropdown.getPreferredSize().height)); // Set preferred width
        return dropdown;
    }

    /**
     * Sets up the component layout using GridBagLayout for responsiveness.
     */
    private void setupLayout() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add spacing between components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add search icon
        JLabel searchIcon = createSearchIcon();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0; // Do not expand
        add(searchIcon, gbc);

        // Add search field
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1; // Expand to fill available space
        add(searchField, gbc);

        // Add category filter
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0; // Do not expand
        add(categoryFilter, gbc);

        // Add author filter
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0; // Do not expand
        add(authorFilter, gbc);
    }

    /**
     * Creates and configures the search icon.
     */
    private JLabel createSearchIcon() {
        ImageIcon icon = ImageLoader.loadImageIcon(
                "/images/icons/search.png",
                20,
                20
        );
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        iconLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                search();
            }
        });
        return iconLabel;
    }

    /**
     * Sets up search triggers (icon click and enter key).
     */
    private void setupListeners() {
        // Search on enter key
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    search();
                }
            }
        });

        // Search on filter change
        categoryFilter.addActionListener(e -> search());
        authorFilter.addActionListener(e -> search());
    }

    /**
     * Loads filter data from the book controller.
     */
    private void loadFilters() {
        // Clear existing items
        categoryFilter.removeAllItems();
        authorFilter.removeAllItems();

        // Add default options
        categoryFilter.addItem("All Categories");
        authorFilter.addItem("All Authors");

        // Add data from controller
        bookController.getAllGenres().forEach(categoryFilter::addItem);
        bookController.getAllAuthors().forEach(authorFilter::addItem);
    }

    /**
     * Performs the search based on current field values.
     */
    private void search() {
        String searchText = searchField.getText().trim().toLowerCase();
        String category = categoryFilter.getSelectedItem().toString();
        String author = authorFilter.getSelectedItem().toString();

        // Filter books based on search criteria
        List<Book> results = bookController.getAllBooks().stream()
                .filter(book -> matchesSearch(book, searchText, category, author))
                .toList();

        // Pass the results to the callback
        if (onSearchResults != null) {
            onSearchResults.accept(results);
        }
    }

    /**
     * Checks if a book matches the search criteria.
     */
    private boolean matchesSearch(Book book, String searchText, String category, String author) {
        boolean matchesText = searchText.isEmpty() ||
                book.getTitle().toLowerCase().contains(searchText) ||
                book.getDescription().toLowerCase().contains(searchText);

        boolean matchesCategory = category.equals("All Categories") ||
                book.getGenre().equals(category);

        boolean matchesAuthor = author.equals("All Authors") ||
                book.getAuthor().equals(author);

        return matchesText && matchesCategory && matchesAuthor;
    }

    /**
     * Clears the search field and resets filters.
     */
    public void clearSearch() {
        searchField.setText("");
        categoryFilter.setSelectedItem("All Categories");
        authorFilter.setSelectedItem("All Authors");
        search();
    }

    /**
     * Refreshes the filter dropdowns with updated data.
     */
    public void refreshFilters() {
        loadFilters();
    }
}