package ui.components;

import Listener.TransactionListener;
import controllers.UserController;
import controllers.BookController;
import controllers.TransactionController;
import models.Transaction;
import models.User;
import utils.ImageLoader;
import utils.createStyledButton;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NotificationManager implements TransactionListener {
    // Constants for UI configuration
    private static final int REFRESH_INTERVAL = 5000; // Refresh notifications every 5 seconds
    private static final int POPUP_WIDTH = 400; // Width of the notification popup
    private static final int POPUP_HEIGHT = 300; // Height of the notification popup
    private static final int ICON_SIZE = 24; // Size of the notification bell icon
    private static final String BELL_ICON_PATH = "/images/icons/bell.png"; // Path to the bell icon

    // Colors for UI elements
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color CARD_HOVER_COLOR = new Color(240, 240, 240);

    // Controllers and user data
    private final UserController userController;
    private final BookController bookController;
    private final TransactionController transactionController;
    private final User user; // Current logged-in user

    // Notification data and UI components
    private final Set<String> displayedNotifications = Collections.newSetFromMap(new ConcurrentHashMap<>()); // Track displayed notifications
    private final DefaultListModel<Transaction> notificationListModel = new DefaultListModel<>(); // List model for notifications
    private final JPanel notificationIconPanel; // Panel for the bell icon and badge
    private final JLabel badgeLabel; // Badge showing the number of notifications
    private final JPopupMenu notificationPopup; // Popup menu for displaying notifications
    private final Timer refreshTimer; // Timer to periodically refresh notifications

    // Constructor
    public NotificationManager(TransactionController transactionController, User user) {
        this.userController = new UserController();
        this.bookController = new BookController();
        this.transactionController = transactionController;
        this.user = user;

        // Initialize UI components
        this.notificationIconPanel = createNotificationIconPanel();
        this.badgeLabel = (JLabel) notificationIconPanel.getComponent(1); // Badge is the second component in the panel
        this.notificationPopup = createNotificationPopup();
        this.refreshTimer = new Timer(REFRESH_INTERVAL, e -> refreshNotifications()); // Timer to refresh notifications

        // Start the timer and load initial notifications
        initializeComponents();
    }

    // Create the notification icon panel (bell icon + badge)
    private JPanel createNotificationIconPanel() {
        JPanel panel = new JPanel(null); // Use absolute positioning
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(ICON_SIZE + 10, ICON_SIZE + 10));

        // Bell icon
        JLabel bellIcon = new JLabel();
        bellIcon.setIcon(ImageLoader.loadImageIcon(BELL_ICON_PATH, ICON_SIZE, ICON_SIZE));
        bellIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Change cursor to hand on hover
        bellIcon.setBounds(0, 0, ICON_SIZE, ICON_SIZE);

        // Show notification popup when the bell icon is clicked
        bellIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showNotificationPopup(bellIcon, bellIcon.getWidth() / 2, bellIcon.getHeight());
            }
        });

        // Badge to show the number of notifications
        JLabel badgeLabel = new JLabel("", SwingConstants.CENTER);
        badgeLabel.setFont(new Font("Arial", Font.BOLD, 10));
        badgeLabel.setForeground(Color.WHITE);
        badgeLabel.setBackground(Color.RED);
        badgeLabel.setOpaque(true);
        badgeLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        badgeLabel.setBounds(18, 0, 16, 16); // Position the badge
        badgeLabel.setVisible(false); // Hide badge if there are no notifications

        // Add bell icon and badge to the panel
        panel.add(bellIcon);
        panel.add(badgeLabel);

        return panel;
    }

    // Create the notification popup menu
    private JPopupMenu createNotificationPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT));
        popup.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        // Panel to hold the notification list and "Clear All" button
        JPanel notificationPanel = new JPanel();
        notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));
        notificationPanel.setBackground(BACKGROUND_COLOR);

        // Notification list
        JList<Transaction> notificationList = createNotificationList();
        JScrollPane scrollPane = new JScrollPane(notificationList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT - 50));

        // "Clear All" button
        JButton clearAllButton = createStyledButton.create("Clear All", Color.RED);
        clearAllButton.addActionListener(e -> clearAllNotifications());
        clearAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearAllButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Add components to the popup
        notificationPanel.add(scrollPane);
        notificationPanel.add(clearAllButton);
        popup.add(notificationPanel);

        return popup;
    }

    // Create the notification list with custom rendering
    private JList<Transaction> createNotificationList() {
        JList<Transaction> list = new JList<>(notificationListModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new NotificationRenderer(this)); // Use custom renderer
        list.setBackground(BACKGROUND_COLOR);

        // Double-click to view notification details
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Transaction transaction = list.getSelectedValue();
                    if (transaction != null) {
                        viewNotificationDetails(transaction);
                    }
                }
            }
        });

        return list;
    }

    // Initialize components and start the refresh timer
    private void initializeComponents() {
        refreshTimer.start(); // Start the timer
        refreshNotifications(); // Load initial notifications
    }

    // Show notification details in a dialog
    private void viewNotificationDetails(Transaction transaction) {
        if (transaction == null) return;

        String message = formatTransactionDetails(transaction);

        // Display details in a scrollable text area
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));
        textArea.setBackground(Color.WHITE);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JOptionPane.showMessageDialog(null, new JScrollPane(textArea), "Notification Details", JOptionPane.INFORMATION_MESSAGE);
    }

    // Format transaction details into a readable string
    private String formatTransactionDetails(Transaction transaction) {
        String bookTitle = getBookTitleById(transaction.getBookId());
        String userName = getUserNameByEmail(transaction.getUserEmail());

        if (user.getRole().equals("admin")) {
            if (transaction.getStatus().equalsIgnoreCase("COMPLETED")) {
                return String.format("User %s has returned the book: %s.", userName, bookTitle);
            } else {
                return String.format("User %s has requested to borrow %s.\n\nPlease review the request.", userName, bookTitle);
            }
        } else {
            if (transaction.getStatus().equalsIgnoreCase("COMPLETED")) {
                return String.format("You have returned the book: %s.", bookTitle);
            } else {
                return String.format("Your request to borrow %s has been %s.", bookTitle, transaction.getStatus().toLowerCase());
            }
        }
    }

    // Refresh notifications by fetching the latest data
    private void refreshNotifications() {
        List<Transaction> notifications = getUserNotifications();

        // Track new notifications
        Set<String> newNotifications = notifications.stream()
                .map(Transaction::getId)
                .collect(Collectors.toSet());

        // Update the UI if there are new notifications
        if (!newNotifications.equals(displayedNotifications)) {
            updateNotifications(notifications, newNotifications);
        }
    }

    // Update the notification list and badge count
    private void updateNotifications(List<Transaction> notifications, Set<String> newNotifications) {
        displayedNotifications.clear();
        displayedNotifications.addAll(newNotifications);

        notificationListModel.clear();
        notifications.forEach(notificationListModel::addElement);

        updateNotificationIcon(newNotifications.size());
    }

    // Update the badge count
    private void updateNotificationIcon(int count) {
        badgeLabel.setText(count > 99 ? "99+" : String.valueOf(count));
        badgeLabel.setVisible(count > 0);
        badgeLabel.setToolTipText(count + " new notifications");
    }

    // Fetch notifications for the current user
    private List<Transaction> getUserNotifications() {
        if (user.getRole().equals("admin")) {
            // Admins see pending requests and returned books
            List<Transaction> pendingRequests = transactionController.getPendingRequests();
            List<Transaction> returnedBooks = transactionController.getReturnedBooks();
            List<Transaction> allNotifications = new ArrayList<>();
            allNotifications.addAll(pendingRequests);
            allNotifications.addAll(returnedBooks);
            return allNotifications;
        } else {
            // Users see their own transactions
            return transactionController.getTransactionsByUser(user.getEmail());
        }
    }

    // Show the notification popup
    private void showNotificationPopup(Component component, int x, int y) {
        notificationPopup.show(component, x, y);
    }

    // Clear all notifications
    private void clearAllNotifications() {
        notificationListModel.clear();
        displayedNotifications.clear();
        updateNotificationIcon(0);
    }

    // Handle transaction events (from TransactionListener)
    @Override
    public void onReturnSuccess(Transaction transaction) {
        refreshNotifications();
    }

    @Override
    public void onReturnFailure(String message) {
        JOptionPane.showMessageDialog(null, "Book return failed: " + message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onRejection(Transaction transaction) {
        refreshNotifications();
    }

    @Override
    public void onAdminApproval(Transaction transaction) {
        refreshNotifications();
    }

    // Custom renderer for notification list items
    private static class NotificationRenderer extends DefaultListCellRenderer {
        private final NotificationManager notificationManager;

        public NotificationRenderer(NotificationManager notificationManager) {
            this.notificationManager = notificationManager;
        }

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel cardPanel = new JPanel();
            cardPanel.setLayout(new BorderLayout());
            cardPanel.setBackground(CARD_COLOR);
            cardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            cardPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (value instanceof Transaction transaction) {
                JLabel label = new JLabel();
                configureLabel(label, transaction, isSelected);
                cardPanel.add(label, BorderLayout.CENTER);

                // Highlight on hover
                cardPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        cardPanel.setBackground(CARD_HOVER_COLOR);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        cardPanel.setBackground(CARD_COLOR);
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        notificationManager.viewNotificationDetails(transaction);
                    }
                });
            }

            return cardPanel;
        }

        private void configureLabel(JLabel label, Transaction transaction, boolean isSelected) {
            String bookTitle = notificationManager.getBookTitleById(transaction.getBookId());
            String userName = notificationManager.getUserNameByEmail(transaction.getUserEmail());

            String notificationText;
            if (notificationManager.user.getRole().equals("admin")) {
                if (transaction.getStatus().equalsIgnoreCase("RETURNED")) {
                    notificationText = String.format("User %s has returned the book: %s.", userName, bookTitle);
                } else {
                    notificationText = String.format("User %s has requested to borrow %s.", userName, bookTitle);
                }
            } else {
                if (transaction.getStatus().equalsIgnoreCase("RETURNED")) {
                    notificationText = String.format("You have returned the book: %s.", bookTitle);
                } else {
                    notificationText = String.format("Your request to borrow %s has been %s.", bookTitle, transaction.getStatus().toLowerCase());
                }
            }

            label.setText(notificationText);
            label.setForeground(transaction.getStatus().equalsIgnoreCase("PENDING") ? Color.RED : Color.BLACK);
        }
    }

    // Getter for the notification icon panel
    public JPanel getNotificationIconPanel() {
        return notificationIconPanel;
    }

    // Helper methods to fetch book title and user name
    private String getBookTitleById(String bookId) {
        return bookController.getBooksByID(bookId).getTitle();
    }

    private String getUserNameByEmail(String userEmail) {
        User user = userController.getUserByEmail(userEmail);
        return user == null ? "Unknown User" : user.getName();
    }
}