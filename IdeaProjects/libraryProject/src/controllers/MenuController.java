package controllers;

import models.User;
import ui.BorrowBookFrame;
import ui.ReturnBook;
import ui.ViewTransactionHistory;

import java.awt.CardLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import ui.components.UserProfileDialog;

public class MenuController {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private User user;
    private BookController bookController; // Required for BorrowBookFrame
    private TransactionController transactionController;

    public MenuController(User user, CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.user = user;
        this.bookController = new BookController();
        this.transactionController = new TransactionController();

//        System.out.println(user.getImgPath());
//        System.out.println("MenuController initialized with user: " + user.getName()); // Debugging statement
    }

    public void handleMenuButtonClick(String menuItem) {
        System.out.println("Button clicked: " + menuItem); // Debugging statement
        switch (menuItem) {
            case "Home":
                cardLayout.show(cardPanel, "Dashboard");
                break;
            case "View profile":
                showUserProfileDialog();
                break;
            case "Borrow Book":
                BorrowBook();
                break;
            case "Return Book":
                ReturnBook();
                break;
            case "Borrowed books":
                ViewHistory();
                break;
            case "Back to previous":
                cardLayout.previous(cardPanel);
                break;
            case "New Arrivals":
                cardLayout.show(cardPanel, "NewArrivals");
                break;
            default:
                System.err.println("Unknown menu item: " + menuItem);
                break;
        }
    }
//    private JPanel availableBookPanel{
//        JPanel panel =new JPanel(new BorderLayout())
//    }
    private void ReturnBook(){
        ReturnBook returnBook = new ReturnBook(cardLayout,cardPanel,user);
        cardPanel.add(returnBook, "ReturnBook");
        cardLayout.show(cardPanel,"ReturnBook");
    }
    private void ViewHistory(){
        ViewTransactionHistory viewTransactionHistory = new ViewTransactionHistory(cardLayout,cardPanel,user);
        cardPanel.add(viewTransactionHistory, "BorrowedBooks");
        cardLayout.show(cardPanel,"BorrowedBooks");
    }
    private void BorrowBook(){
        BorrowBookFrame borrowBookFrame = new BorrowBookFrame(user,cardLayout,cardPanel,bookController,transactionController);
        cardPanel.add(borrowBookFrame,"BorrowBook");
        cardLayout.show(cardPanel,"BorrowBook");
    }
    private void showUserProfileDialog() {
        UserProfileDialog userProfileDialog = new UserProfileDialog(user);
        userProfileDialog.setVisible(true);
    }
}