package ui.components;

import utils.createStyledButton;

import javax.swing.*;
import java.awt.*;

public class Header extends JPanel {
    public Header(String title, String userName, Runnable logoutAction) {
        setLayout(new BorderLayout());
        setBackground(new Color(153, 153, 255)); // Purple background
        setPreferredSize(new Dimension(getWidth(), 80)); // Set height of the header

        // Logo and library name section
        JPanel logoSection = new JPanel();
        logoSection.setOpaque(false); // Transparent background
        logoSection.setLayout(new BoxLayout(logoSection, BoxLayout.X_AXIS)); // Horizontal layout
        logoSection.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding

        // Load and resize the logo image
        ImageIcon logoIcon = loadImageIcon("/images/thumbnails/library.jpg", 40, 40);
        JLabel logo = new JLabel(logoIcon);
        logoSection.add(logo);

        // Add spacing between logo and library name
        logoSection.add(Box.createRigidArea(new Dimension(10, 0)));

        // Add the library name
        JLabel libraryName = new JLabel(title);
        libraryName.setFont(new Font("Arial", Font.BOLD, 20));
        libraryName.setForeground(Color.WHITE); // White text color
        logoSection.add(libraryName);

        // User section with logout button
        JPanel userSection = new JPanel();
        userSection.setOpaque(false); // Transparent background
        userSection.setLayout(new BoxLayout(userSection, BoxLayout.X_AXIS)); // Horizontal layout
        userSection.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding

        // Logout button
        JButton logoutButton = createStyledButton.create("Log out",Color.RED);
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(180, 34, 34)); // Red button
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logoutAction.run());
        userSection.add(logoutButton);

        // Welcome message in the center
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setOpaque(false); // Transparent background

        JLabel welcomeLabel = new JLabel("Welcome, " + userName);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeLabel.setForeground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER; // Center the label
        welcomePanel.add(welcomeLabel, gbc);

        // Add sections to the header
        add(logoSection, BorderLayout.WEST); // Logo and library name on the left
        add(welcomePanel, BorderLayout.CENTER); // Welcome message in the center
        add(userSection, BorderLayout.EAST); // Logout button on the right
    }

    private ImageIcon loadImageIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(getClass().getResource(path));
        Image resizedImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }
}