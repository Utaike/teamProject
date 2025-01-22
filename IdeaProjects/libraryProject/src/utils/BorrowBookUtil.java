package utils;

import Listener.TransactionListener;
import controllers.TransactionController;
import models.Book;
import models.User;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class BorrowBookUtil {

    public static void borrowBook(Book book, User user, TransactionController transactionController, TransactionListener listener, Component parentComponent) {
        // Create a custom dialog panel
        JDialog dialog = new JDialog();
        dialog.setTitle("Borrow Confirmation");
        dialog.setSize(700, 450); // Adjusted size to fit the layout
        dialog.setLocationRelativeTo(parentComponent);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        // Main panel with custom styling
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20)); // Add horizontal and vertical gaps
        mainPanel.setBackground(new Color(245, 245, 245)); // Light gray background
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Left panel for the book image
        JPanel imagePanel = new JPanel();
        imagePanel.setBackground(new Color(245, 245, 245));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Load and display the book image
        JLabel bookImageLabel = new JLabel();
        try {
            ImageIcon icon = ImageLoader.loadImageIcon(book.getImgPath(), 200, 300); // Resize the image
            bookImageLabel.setIcon(icon);
        } catch (Exception e) {
            bookImageLabel.setText("Error loading image");
            bookImageLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            bookImageLabel.setForeground(Color.RED);
        }
        imagePanel.add(bookImageLabel);

        // Right panel for the book details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(new Color(245, 245, 245));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Confirm Borrowing");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Book details
        addDetailLabel(detailsPanel, "Book Title: " + book.getTitle(), Font.BOLD, 18);
        addDetailLabel(detailsPanel, "Author: " + book.getAuthor(), Font.PLAIN, 16);
        addDetailLabel(detailsPanel, "ISBN: " + book.getIsbn(), Font.PLAIN, 16);

        // User details
        addDetailLabel(detailsPanel, "User Email: " + user.getEmail(), Font.PLAIN, 16);

        // Preloaded transaction details
        JLabel transactionIdLabel = addDetailLabel(detailsPanel, "Transaction ID: " + transactionController.generateId(), Font.PLAIN, 16);
        JLabel borrowDateLabel = addDetailLabel(detailsPanel, "Borrow Date: " + LocalDate.now().toString(), Font.PLAIN, 16);
        JLabel dueDateLabel = addDetailLabel(detailsPanel, "Due Date: " + LocalDate.now().plusWeeks(2).toString(), Font.PLAIN, 16);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Confirm button
        JButton confirmButton = createStyledButton.create("Confirm", new Color(90, 160, 255));
        confirmButton.setFont(new Font("Arial", Font.BOLD, 16));
        confirmButton.setPreferredSize(new Dimension(150, 50));
        confirmButton.addActionListener(_ -> {
            // Save the transaction only when the user confirms
            boolean success = transactionController.createBorrowingRequest(book, user, listener);

            if (!success) {
                showErrorDialog("Failed to create a borrowing request. Please try again.", parentComponent);
            } else {
                dialog.dispose(); // Close the dialog after successful borrow
            }
        });

        // Cancel button
        JButton cancelButton = createStyledButton.create("Cancel", Color.GRAY);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 16));
        cancelButton.setPreferredSize(new Dimension(150, 50));
        cancelButton.addActionListener(_ -> dialog.dispose());

        // Add buttons to the panel
        buttonPanel.add(confirmButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancelButton);

        // Add components to the details panel
        detailsPanel.add(titleLabel);
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(transactionIdLabel);
        detailsPanel.add(borrowDateLabel);
        detailsPanel.add(dueDateLabel);
        detailsPanel.add(Box.createVerticalStrut(20));
        detailsPanel.add(buttonPanel);

        // Add panels to the main panel
        mainPanel.add(imagePanel, BorderLayout.WEST);
        mainPanel.add(detailsPanel, BorderLayout.CENTER);

        // Add panel to dialog
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private static JLabel addDetailLabel(JPanel panel, String text, int fontStyle, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", fontStyle, fontSize));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(label);
        return label;
    }

    private static void showErrorDialog(String message, Component parentComponent) {
        JDialog errorDialog = new JDialog();
        errorDialog.setUndecorated(true);
        errorDialog.setSize(350, 150);
        errorDialog.setLocationRelativeTo(parentComponent);
        errorDialog.setModal(true);

        JPanel errorPanel = new JPanel();
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
        errorPanel.setBackground(Color.WHITE);
        errorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel errorTitle = new JLabel("Error");
        errorTitle.setFont(new Font("Arial", Font.BOLD, 18));
        errorTitle.setForeground(Color.RED);
        errorTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel errorMessage = new JLabel("<html><center>" + message + "</center></html>");
        errorMessage.setFont(new Font("Arial", Font.PLAIN, 14));
        errorMessage.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = createStyledButton.create("OK", Color.RED);
        okButton.addActionListener(_ -> errorDialog.dispose());
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorPanel.add(errorTitle);
        errorPanel.add(Box.createVerticalStrut(15));
        errorPanel.add(errorMessage);
        errorPanel.add(Box.createVerticalStrut(15));
        errorPanel.add(okButton);

        errorDialog.add(errorPanel);
        errorDialog.setVisible(true);
    }
}