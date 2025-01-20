package controllers;
import models.User;
import ui.components.UserProfileDialog;

import javax.swing.*;
import java.awt.*;

public class AdminMenuController {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private User user;

    public AdminMenuController(User user, CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.user = user;
        System.out.println("AdminMenuController initialized with user: " + user.getRole());
    }

    public void handleMenuButtonClick(String menuItem) {
        System.out.println("Admin button clicked: " + menuItem);
        switch (menuItem) {
            case "Home":
                cardLayout.show(cardPanel, "AdminDashboard");
                break;
            case "View Profile":
                showUserProfileDialog();
                break;
            case "Manage Books":
                cardLayout.show(cardPanel, "ManageBooks");
                break;
            case "Manage Users":
                cardLayout.show(cardPanel, "AddUser");
                break;
            case "Borrowed History":
                cardLayout.show(cardPanel, "BorrowedHistory");
                break;
            default:
                System.err.println("Unknown admin menu item: " + menuItem);
                break;
        }
    }
    private void showUserProfileDialog() {
        UserProfileDialog profileDialog =new UserProfileDialog(user);
        profileDialog.setVisible(true);

    }

}