package ui.components;

import controllers.TransactionController;
import models.User;
import utils.ImageLoader;
import utils.createStyledButton;

import javax.swing.*;
import java.awt.*;

public class Header extends JPanel {
    private final TransactionController transactionController;
    private NotificationManager notificationManager; // Make it non-final to allow lazy initialization
    private final User user;
    private boolean isNotificationManagerInitialized = false; // Flag to track initialization

    public Header(String title, User user, Runnable logoutAction) {
        this.transactionController = new TransactionController(); // Initialize the controller
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(new Color(153, 153, 255)); // Purple background
        setPreferredSize(new Dimension(getWidth(), 80)); // Set height of the header

        // Left section: Logo and library name
        JPanel leftSection = createLeftSection(title);
        add(leftSection, BorderLayout.WEST);

        // Center section: Welcome message
        JPanel centerSection = createCenterSection(user.getName());
        add(centerSection, BorderLayout.CENTER);

        // Right section: Notification icon and logout button
        JPanel rightSection = createRightSection(logoutAction);
        add(rightSection, BorderLayout.EAST);
    }

    private JPanel createLeftSection(String title) {
        JPanel leftSection = new JPanel();
        leftSection.setOpaque(false); // Transparent background
        leftSection.setLayout(new BoxLayout(leftSection, BoxLayout.X_AXIS)); // Horizontal layout
        leftSection.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding

        // Load and resize the logo image
        ImageIcon logoIcon = ImageLoader.loadImageIcon("/images/thumbnails/library.jpg", 40, 40);
        JLabel logo = new JLabel(logoIcon);
        leftSection.add(logo);

        // Add spacing between logo and library name
        leftSection.add(Box.createRigidArea(new Dimension(10, 0)));

        // Add the library name
        JLabel libraryName = new JLabel(title);
        libraryName.setFont(new Font("Arial", Font.BOLD, 20));
        libraryName.setForeground(Color.WHITE); // White text color
        leftSection.add(libraryName);

        return leftSection;
    }

    private JPanel createCenterSection(String userName) {
        JPanel centerSection = new JPanel(new GridBagLayout());
        centerSection.setOpaque(false); // Transparent background

        JLabel welcomeLabel = new JLabel("Welcome, " + userName);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeLabel.setForeground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER; // Center the label
        centerSection.add(welcomeLabel, gbc);

        return centerSection;
    }

    private JPanel createRightSection(Runnable logoutAction) {
        JPanel rightSection = new JPanel(new GridBagLayout());
        rightSection.setOpaque(false); // Transparent background
        rightSection.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding

        // Initialize the NotificationManager only once
        if (!isNotificationManagerInitialized) {
            notificationManager = new NotificationManager(transactionController, user);
            isNotificationManagerInitialized = true; // Mark as initialized
        }

        // Get the notification icon
        JComponent notificationIcon = (JComponent) notificationManager.getNotificationIconPanel();

        // Logout button
        JButton logoutButton = createStyledButton.create("Log out", Color.RED);
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(180, 34, 34)); // Red button
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logoutAction.run());

        // GridBagConstraints for notification icon
        GridBagConstraints gbcIcon = new GridBagConstraints();
        gbcIcon.gridx = 0;
        gbcIcon.gridy = 0;
        gbcIcon.anchor = GridBagConstraints.CENTER; // Center the icon
        gbcIcon.insets = new Insets(0, 0, 0, 10); // Add spacing to the right of the icon

        // GridBagConstraints for logout button
        GridBagConstraints gbcButton = new GridBagConstraints();
        gbcButton.gridx = 1;
        gbcButton.gridy = 0;
        gbcButton.anchor = GridBagConstraints.CENTER; // Center the button

        // Add components to the right section
        rightSection.add(notificationIcon, gbcIcon);
        rightSection.add(logoutButton, gbcButton);

        return rightSection;
    }
}