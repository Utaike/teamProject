package ui.components;

import models.Book;
import utils.createStyledButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static utils.ImageLoader.loadImageIcon;

public class BookCard extends JPanel {
    private final Book book; // Store the book object
    private final JButton actionButton; // Store the action button for later updates

    public BookCard(Book book, Runnable action, Runnable viewDetailsAction, String frameType) {
        this.book = book; // Initialize the book object
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Vertical layout
        setPreferredSize(new Dimension(180, 260)); // Set card size
        setBackground(Color.WHITE); // White background
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1), // Light gray border
                BorderFactory.createEmptyBorder(10, 10, 10, 10) // Padding inside the card
        ));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Change cursor to hand on hover

        // Load the book image
        ImageIcon originalIcon = loadImageIcon(book.getImgPath(), 100, 140); // Load and resize image
        JLabel bookImage = new JLabel(originalIcon);
        bookImage.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the image

        // Create and style the title label
        JLabel titleLabel = new JLabel(book.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14)); // Bold font for title
        titleLabel.setForeground(new Color(50, 50, 50)); // Dark gray text
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the title

        // Create and style the author label
        JLabel authorLabel = new JLabel("By " + book.getAuthor());
        authorLabel.setFont(new Font("Arial", Font.PLAIN, 12)); // Plain font for author
        authorLabel.setForeground(new Color(100, 100, 100)); // Medium gray text
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the author

        // Determine button label based on the frame type and book availability
        String buttonLabel = determineButtonLabel(frameType, book);

        // Create and style the action button
        actionButton = createStyledButton.create(buttonLabel, book.isAvailable() ? new Color(0, 100, 225) : Color.GRAY);
        actionButton.setFont(new Font("Arial", Font.PLAIN, 12)); // Font for button
        actionButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the button
        actionButton.setEnabled(book.isAvailable()); // Disable the button if the book is unavailable
        actionButton.addActionListener(e -> action.run()); // Set button action

        // Add components to the book card
        add(bookImage);
        add(Box.createVerticalStrut(10)); // Add vertical spacing
        add(titleLabel);
        add(Box.createVerticalStrut(5)); // Add vertical spacing
        add(authorLabel);
        add(Box.createVerticalStrut(10)); // Add vertical spacing
        add(actionButton); // Add the action button

        // Add hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                setBackground(new Color(245, 245, 245)); // Light gray background on hover
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(180, 180, 180), 1), // Darker border on hover
                        BorderFactory.createEmptyBorder(10, 10, 10, 10) // Maintain padding
                ));
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                setBackground(Color.WHITE); // Reset background color
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230), 1), // Reset border
                        BorderFactory.createEmptyBorder(10, 10, 10, 10) // Maintain padding
                ));
            }
        });

        // Add click listener for book details
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getSource() == BookCard.this && evt.getComponent() != actionButton) {
                    viewDetailsAction.run();
                }
            }
        });
    }

    /**
     * Determines the button label based on the frame type and book availability.
     *
     * @param frameType The type of frame (e.g., "BorrowBookFrame" or "UserDashboard").
     * @param book      The book object.
     * @return The button label.
     */
    private String determineButtonLabel(String frameType, Book book) {
        if (!book.isAvailable()) {
            return "Unavailable"; // Show "Unavailable" if the book is not available
        } else if ("BorrowBookFrame".equals(frameType)) {
            return "Borrow"; // Always show "Borrow" in the BorrowBookFrame
        } else if ("UserDashboard".equals(frameType)) {
            return "Read"; // Show "Read" in the UserDashboard
        }
        return ""; // Default label
    }

    /**
     * Updates the availability status of the book in the UI.
     * Disables the action button and changes its text and color if the book is unavailable.
     */
    public void updateAvailability() {
        if (!book.isAvailable()) {
            actionButton.setEnabled(false); // Disable the borrow button
            actionButton.setText("Unavailable");
            actionButton.setBackground(Color.GRAY);
        }
    }

    // Getter for the button text
    public String getButtonText() {
        return actionButton.getText();
    }

    // Setter for the button text
    public void setButtonText(String buttonText) {
        actionButton.setText(buttonText);
    }

    // Setter for the button enabled state
    public void setButtonEnabled(boolean enabled) {
        actionButton.setEnabled(enabled);
    }
}