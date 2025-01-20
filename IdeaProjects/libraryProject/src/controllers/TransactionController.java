package controllers;

import Listener.TransactionListener;
import models.Transaction;
import models.Book;
import models.User;
import services.TransactionService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionController {
    private final TransactionService transactionService;
    private final BookController bookController;

    public TransactionController() {
        this.transactionService = new TransactionService();
        this.bookController = new BookController();
    }

    /**
     * Borrow a book and notify the listener of the result.
     *
     * @param book     The book to borrow.
     * @param user     The user borrowing the book.
     * @param listener The listener to notify of the result.
     * @return True if the borrow request was initiated successfully, false otherwise.
     */
    public boolean borrowBook(Book book, User user, TransactionListener listener) {
        if (book == null || user == null || !book.isAvailable()) {
            listener.onBorrowFailure("Invalid book or user, or the book is not available.");
            return false;
        }

        // Attempt to borrow the book
        boolean success = transactionService.borrowBook(user.getEmail(), book.getId());
        if (success) {
            bookController.updateBookAvailability(book.getId(),false);
            // Create a new transaction object
            Transaction transaction = new Transaction(
                    transactionService.generateId(),
                    user.getEmail(),
                    book.getId(),
                    LocalDate.now(),
                    LocalDate.now().plusWeeks(2), // 2 weeks lending period
                    null,
                    true // isBorrow
            );
            listener.onBorrowSuccess(transaction); // Notify the listener of success
        } else {
            listener.onBorrowFailure("Failed to borrow the book. Please try again."); // Notify the listener of failure
        }

        return success;
    }

    /**
     * Return a book and notify the listener of the result.
     *
     * @param book     The book to return.
     * @param user     The user returning the book.
     * @param listener The listener to notify of the result.
     * @return True if the return request was initiated successfully, false otherwise.
     */
    public boolean returnBook(Book book, User user, TransactionListener listener) {
        if (book == null || user == null) {
            listener.onReturnFailure("Invalid book or user.");
            return false;
        }

        // Attempt to return the book
        boolean success = transactionService.returnBook(user.getEmail(), book.getId());
        if (success) {
            // Update book availability to true
            bookController.updateBookAvailability(book.getId(), true);
            // Create a new transaction object
            Transaction transaction = new Transaction(
                    transactionService.generateId(),
                    user.getEmail(),
                    book.getId(),
                    LocalDate.now(),
                    LocalDate.now().plusWeeks(2), // 2 weeks lending period
                    LocalDate.now(),
                    false // isBorrow (false for return)
            );
            listener.onReturnSuccess(transaction); // Notify the listener of success
        } else {
            listener.onReturnFailure("Failed to return the book. Please try again."); // Notify the listener of failure
        }

        return success;
    }

    /**
     * Get all active (unreturned) transactions for a user.
     *
     * @param userEmail The email of the user.
     * @return A list of active transactions for the user.
     */
    public List<Transaction> getActiveTransactionsByUser(String userEmail) {
        return transactionService.getTransactionsByUser(userEmail).stream()
                .filter(transaction -> transaction.isBorrow() && transaction.getReturnDate() == null)
                .collect(Collectors.toList());
    }

    /**
     * Get all transactions for a user.
     *
     * @param userEmail The email of the user.
     * @return A list of all transactions for the user.
     */
    public List<Transaction> getTransactionsByUser(String userEmail) {
        return transactionService.getTransactionsByUser(userEmail);
    }
    //get all borrowed transaction;
    public List<Transaction> getAllActiveTransaction(){
        return transactionService.getActiveTransactions();
    }
    public String GenerateId(){
        return transactionService.generateId();
    }
}