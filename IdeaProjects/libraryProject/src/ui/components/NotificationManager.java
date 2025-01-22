package ui.components;

import Listener.TransactionListener;
import controllers.TransactionController;
import models.Transaction;
import models.User;
import utils.ImageLoader;

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
    private static final int REFRESH_INTERVAL = 5000;
    private static final int POPUP_WIDTH = 400;
    private static final int POPUP_HEIGHT = 300;
    private static final int ICON_SIZE = 24;
    private static final String BELL_ICON_PATH = "/images/icons/bell.png";

    private static final Color HIGHLIGHT_COLOR = new Color(210, 210, 230);
    private static final Color DEFAULT_COLOR = Color.WHITE;
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);

    private final TransactionController transactionController;
    private final User user;
    private final Set<String> displayedNotifications;
    private final DefaultListModel<Transaction> notificationListModel;
    private final JPanel notificationIconPanel;
    private final JLabel badgeLabel;
    private final JPopupMenu notificationPopup;
    private final Timer refreshTimer;

    public NotificationManager(TransactionController transactionController, User user) {
        this.transactionController = transactionController;
        this.user = user;
        this.displayedNotifications = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.notificationListModel = new DefaultListModel<>();
        this.notificationIconPanel = createNotificationIconPanel();
        this.badgeLabel = (JLabel) notificationIconPanel.getComponent(1);
        this.notificationPopup = createNotificationPopup();
        this.refreshTimer = new Timer(REFRESH_INTERVAL, e -> refreshNotifications());

        initializeComponents();
    }

    private JPanel createNotificationIconPanel() {
        JPanel panel = new JPanel(null);
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(ICON_SIZE + 10, ICON_SIZE + 10));

        JLabel bellIcon = new JLabel();
        bellIcon.setIcon(ImageLoader.loadImageIcon(BELL_ICON_PATH, ICON_SIZE, ICON_SIZE));
        bellIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellIcon.setBounds(0, 0, ICON_SIZE, ICON_SIZE);

        bellIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showNotificationPopup(bellIcon, bellIcon.getWidth() / 2, bellIcon.getHeight());
            }
        });

        JLabel badgeLabel = new JLabel("", SwingConstants.CENTER);
        badgeLabel.setFont(new Font("Arial", Font.BOLD, 10));
        badgeLabel.setForeground(Color.WHITE);
        badgeLabel.setBackground(Color.RED);
        badgeLabel.setOpaque(true);
        badgeLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        badgeLabel.setBounds(18, 0, 16, 16);
        badgeLabel.setVisible(false);

        panel.add(bellIcon);
        panel.add(badgeLabel);

        return panel;
    }

    private JPopupMenu createNotificationPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT));
        popup.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        JList<Transaction> notificationList = createNotificationList();
        JScrollPane scrollPane = new JScrollPane(notificationList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        popup.add(scrollPane);
        return popup;
    }

    private JList<Transaction> createNotificationList() {
        JList<Transaction> list = new JList<>(notificationListModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new NotificationRenderer());
        list.setBackground(BACKGROUND_COLOR);

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int selectedIndex = list.getSelectedIndex();
                    if (selectedIndex >= 0) {
                        viewNotificationDetails(notificationListModel.getElementAt(selectedIndex));
                    }
                }
            }
        });

        return list;
    }

    private void initializeComponents() {
        refreshTimer.start();
        refreshNotifications();
    }

    private void viewNotificationDetails(Transaction transaction) {
        if (transaction == null) return;

        String message = formatTransactionDetails(transaction);

        if (user.getRole().equals("admin") && transaction.getStatus().equalsIgnoreCase("PENDING")) {
            handleAdminAction(transaction, message);
        } else {
            showInfoDialog("Notification Details", message);
        }
    }

    private String formatTransactionDetails(Transaction transaction) {
        return String.format(
                "Transaction Details:\nID: %s\nUser: %s\nBook: %s\nStatus: %s",
                transaction.getId(),
                transaction.getUserEmail(),
                transaction.getBookId(),
                transaction.getStatus()
        );
    }

    private void handleAdminAction(Transaction transaction, String message) {
        String[] options = {"Accept", "Reject", "Close"};
        int choice = JOptionPane.showOptionDialog(
                null, message, "Notification Details",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, "Close"
        );

        switch (choice) {
            case 0: // Accept
                handleTransactionAction(
                        transactionController::approveBorrowingRequest,
                        "approve",
                        transaction.getId(),
                        transaction.getUserEmail(),
                        "Your borrowing request has been accepted."
                );
                break;
            case 1: // Reject
                handleTransactionAction(
                        transactionController::rejectBorrowingRequest,
                        "reject",
                        transaction.getId(),
                        transaction.getUserEmail(),
                        "Your borrowing request has been rejected."
                );
                break;
        }
    }

    private void handleTransactionAction(TransactionAction action, String actionName,
                                         String transactionId, String userEmail, String notificationMessage) {
        boolean success = action.perform(transactionId, this);
        String resultMessage = success ?
                String.format("Request %s successfully!", actionName) :
                String.format("Failed to %s request.", actionName);

        showDialog(success ? "Success" : "Error", resultMessage,
                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        if (success) {
            notifyUser(userEmail, notificationMessage);
            refreshNotifications();
        }
    }

    private void refreshNotifications() {
        List<Transaction> notifications = getUserNotifications();

        Set<String> newNotifications = notifications.stream()
                .map(Transaction::getId)
                .collect(Collectors.toSet());

        if (!newNotifications.equals(displayedNotifications)) {
            updateNotifications(notifications, newNotifications);
        }
    }

    private void updateNotifications(List<Transaction> notifications, Set<String> newNotifications) {
        displayedNotifications.clear();
        displayedNotifications.addAll(newNotifications);

        notificationListModel.clear();
        notifications.forEach(notificationListModel::addElement);

        updateNotificationIcon(newNotifications.size());
    }

    private void updateNotificationIcon(int count) {
        badgeLabel.setText(count > 99 ? "99+" : String.valueOf(count));
        badgeLabel.setVisible(count > 0);
        badgeLabel.setToolTipText(count + " new notifications");
    }

    private List<Transaction> getUserNotifications() {
        if (user.getRole().equals("admin")) {
            // Admin fetches all pending transactions
            return transactionController.getPendingRequests();
        } else {
            // Regular users fetch their own notifications
            return transactionController.getTransactionsByUser(user.getEmail());
        }
    }

    public JPanel getNotificationIconPanel() {
        return notificationIconPanel;
    }

    private void showNotificationPopup(Component component, int x, int y) {
        notificationPopup.show(component, x, y);
    }

    private void showDialog(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(null, message, title, messageType);
    }

    private void showInfoDialog(String title, String message) {
        showDialog(title, message, JOptionPane.INFORMATION_MESSAGE);
    }

    private void notifyUser(String userEmail, String message) {
        if (user.getRole().equals("admin")) {
            // Admin should not receive notifications meant for users
            return;
        }
        JOptionPane.showMessageDialog(null, "Notification for " + userEmail + ":\n" + message,
                "Request Update", JOptionPane.INFORMATION_MESSAGE);
    }

    // TransactionListener implementation
    @Override
    public void onBorrowSuccess(Transaction transaction) {
        showInfoDialog("Success", "Borrowing request successful!");
        refreshNotifications();
    }

    @Override
    public void onBorrowFailure(String message) {
        showDialog("Error", "Borrowing request failed: " + message, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onReturnSuccess(Transaction transaction) {
        showInfoDialog("Success", "Book returned successfully!");
        refreshNotifications();
    }

    @Override
    public void onReturnFailure(String message) {
        showDialog("Error", "Book return failed: " + message, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onRejectSuccess(Transaction transaction) {
        showInfoDialog("Success", "Request rejected successfully!");
        refreshNotifications();
    }

    public Component getNotificationIcon() {
        return notificationIconPanel;
    }

    private static class NotificationRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Transaction) {
                Transaction transaction = (Transaction) value;
                configureLabel(label, transaction, isSelected);
            }

            return label;
        }

        private void configureLabel(JLabel label, Transaction transaction, boolean isSelected) {
            boolean isPending = transaction.getStatus().equalsIgnoreCase("PENDING");
            String notificationText = String.format("%s: ID: %s | User: %s | Book's id: %s",
                    isPending ? "Pending Request" : "Successful Request",
                    transaction.getId(),
                    transaction.getUserEmail(),
                    transaction.getBookId()
            );

            label.setText(notificationText);
            label.setIcon(new ImageIcon(isPending ? "path/to/pending_icon.png" : "path/to/processed_icon.png"));
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            label.setBackground(isSelected ? HIGHLIGHT_COLOR : DEFAULT_COLOR);
        }
    }

    @FunctionalInterface
    private interface TransactionAction {
        boolean perform(String transactionId, TransactionListener listener);
    }
}