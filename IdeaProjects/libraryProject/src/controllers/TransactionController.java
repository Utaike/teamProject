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
     * Create a pending borrowing request.
     *
     * @param book     The book to borrow.
     * @param user     The user borrowing the book.
     * @param listener The listener to notify of the result.
     * @return True if the request was created successfully, false otherwise.
     */
    public boolean createBorrowingRequest(Book book, User user, TransactionListener listener) {
        if (book == null || user == null || !book.isAvailable()) {
            return false;
        }

        // Create a pending transaction using TransactionService
        boolean success = transactionService.createPendingRequest(user.getEmail(), book.getId());
        if (success) {
            // Notify the listener of success
            Transaction transaction = new Transaction(
                    transactionService.generateId(),
                    user.getEmail(),
                    book.getId(),
                    LocalDate.now(),
                    LocalDate.now().plusWeeks(2), // 2 weeks lending period
                    null,
                    true, // isBorrow
                    "PENDING" // Status
            );
            listener.onAdminApproval(transaction);
        }
        return success;
    }

    /**
     * Approve a borrowing request and finalize the transaction.
     *
     * @param transactionId The ID of the transaction to approve.
     * @param listener      The listener to notify of the result.
     * @return True if the request was approved successfully, false otherwise.
     */
    public boolean approveBorrowingRequest(String transactionId, TransactionListener listener) {
        // Approve the transaction using TransactionService
        boolean success = transactionService.approveRequest(transactionId);
        if (success) {
            // Fetch the approved transaction
            Transaction transaction = transactionService.getTransactionById(transactionId);
            if (transaction != null) {
                // Update book availability
                bookController.updateBookAvailability(transaction.getBookId(), false);
            } else {
                return false;
            }
        }
        return success;
    }

    /**
     * Reject a borrowing request.
     *
     * @param transactionId The ID of the transaction to reject.
     * @param listener      The listener to notify of the result.
     * @return True if the request was rejected successfully, false otherwise.
     */
    public boolean rejectBorrowingRequest(String transactionId, TransactionListener listener) {
        // Reject the transaction using TransactionService
        boolean success = transactionService.rejectRequest(transactionId);
        if (success) {
            // Fetch the rejected transaction
            Transaction transaction = transactionService.getTransactionById(transactionId);
            if (transaction != null) {
                listener.onRejection(transaction); // Notify the listener of success
            } else {
                return false;
            }
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

        // Return the book using TransactionService
        boolean success = transactionService.returnBook(user.getEmail(), book.getId());
        if (success) {
            // Update book availability to true
            bookController.updateBookAvailability(book.getId(), true);
            // Create a new transaction object for the return
            Transaction transaction = new Transaction(
                    transactionService.generateId(),
                    user.getEmail(),
                    book.getId(),
                    LocalDate.now(),
                    LocalDate.now().plusWeeks(2), // 2 weeks lending period
                    LocalDate.now(),
                    false, // isBorrow (false for return)
                    "COMPLETED" // Status
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
                .filter(transaction -> transaction.isBorrowed() && transaction.getReturnDate() == null)
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

    /**
     * Get all borrowed transactions.
     *
     * @return A list of all borrowed transactions.
     */
    public List<Transaction> getAllActiveTransactions() {
        return transactionService.getActiveTransactions();
    }

    /*** Get all pending borrowing requests.
     * @return A list of all pending transactions.*/
    public List<Transaction> getPendingRequests() {
        return transactionService.getPendingRequests();
    }

    /**Generate a unique transaction ID.
     * @return A new unique ID.*/
    public String generateId() {
        return transactionService.generateId();
    }

    public Transaction getTransactionById(String transactionId) {
        return transactionService.getTransactionById(transactionId);
    }

    public List<Transaction> getReturnedBooksByUser(String email) {
        return transactionService.getTransactionsByUser(email).stream()
                .filter(transaction -> transaction.isReturned() == true)
                .toList();
    }

    public List<Transaction> getReturnedBooks() {
        return transactionService.getAllTransactions().stream().filter(transaction -> transaction.isReturned() == true).toList();
    }

    public boolean addTransaction(Transaction newTransaction) {
        return transactionService.addTransaction(newTransaction);
    }

    public boolean updateTransaction(Transaction transaction) {
        return transactionService.updateTransaction(transaction);
    }

    public boolean deleteTransaction(String transactionId) {
        return transactionService.deleteTransaction(transactionId);
    }
}