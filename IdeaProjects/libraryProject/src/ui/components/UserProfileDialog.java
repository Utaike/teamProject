package ui.components;

import models.User;

import javax.swing.*;
import java.awt.*;

public class UserProfileDialog extends JDialog {
    public UserProfileDialog(User user) {
        setTitle("Profile");
        setModal(true);
        setLayout(new BorderLayout(10,10));
        setSize(300,400);
        setLocationRelativeTo(null);

        JPanel profilePanel=new JPanel(new BorderLayout(10,10));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        profilePanel.setBackground(new Color(245, 245, 245)); // Light gray background
        ImageIcon defaultPhoto = new ImageIcon(user.getImgPath());
        if (defaultPhoto.getImage() == null) {
            System.err.println("Image not found at the specified path.");
            defaultPhoto = new ImageIcon(); // Fallback if the image is not found
        }

        // Create a rounded profile picture
        JLabel photoLabel = new JLabel(new ImageIcon(RoundedImage.getRoundedImage(defaultPhoto.getImage(),100,100)));
        photoLabel.setHorizontalAlignment(JLabel.CENTER);
        profilePanel.add(photoLabel, BorderLayout.NORTH);
        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        detailsPanel.setBackground(new Color(245, 245, 245));

        Font labelFont = new Font("Arial", Font.BOLD, 14);
        JLabel nameLabel = new JLabel("Name: " + user.getName());
        nameLabel.setFont(labelFont);
        JLabel emailLabel = new JLabel("Email: " + user.getEmail());
        emailLabel.setFont(labelFont);
        JLabel roleLabel = new JLabel("Role: " + user.getRole());
        roleLabel.setFont(labelFont);
        detailsPanel.add(nameLabel);
        detailsPanel.add(emailLabel);
        detailsPanel.add(roleLabel);

        profilePanel.add(detailsPanel, BorderLayout.CENTER);

        // Add a close button at the bottom
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.PLAIN, 14));
        closeButton.setBackground(new Color(153, 153, 255));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.add(closeButton);
        profilePanel.add(buttonPanel, BorderLayout.SOUTH);

        add(profilePanel);
    }

    public static void main(String[] args) {

    }
}
