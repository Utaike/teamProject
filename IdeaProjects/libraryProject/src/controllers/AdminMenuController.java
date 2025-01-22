package controllers;
import models.User;
import ui.ManageUsersPanel;
import ui.components.UserProfileDialog;

public class AdminMenuController {
    private final User user;
    private final AdminController adminController;
    private final Runnable navigateToHome;
    private final Runnable navigateToManageBooks;
    private final Runnable navigateToManageUsers;
    private final Runnable navigateToBorrowedHistory;

    public AdminMenuController(User user, AdminController adminController,
                               Runnable navigateToHome,
                               Runnable navigateToManageBooks,
                               Runnable navigateToManageUsers,
                               Runnable navigateToBorrowedHistory) {
        this.user = user;
        this.adminController = adminController;
        this.navigateToHome = navigateToHome;
        this.navigateToManageBooks = navigateToManageBooks;
        this.navigateToManageUsers = navigateToManageUsers;
        this.navigateToBorrowedHistory = navigateToBorrowedHistory;
    }

    public void handleMenuButtonClick(String menuItem) {
        switch (menuItem) {
            case "Home":
                navigateToHome.run();
                break;
            case "View Profile":
                showUserProfileDialog();
                break;
            case "Manage Books":
                navigateToManageBooks.run();
                break;
            case "Manage Users":
                navigateToManageUsers.run();
                break;
            case "Borrowed History":
                navigateToBorrowedHistory.run();
                break;
            default:
                System.err.println("Unknown admin menu item: " + menuItem);
                break;
        }
    }

    private void showUserProfileDialog() {
        UserProfileDialog profileDialog = new UserProfileDialog(user);
        profileDialog.setVisible(true);
    }
}