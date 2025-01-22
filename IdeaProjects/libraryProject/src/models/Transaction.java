package models;

import java.time.LocalDate;

public class Transaction {
    private String id; // Unique transaction ID
    private String userEmail; // Email of the user borrowing the book
    private String bookId; // ID of the book being borrowed
    private LocalDate borrowDate; // Date the book was borrowed
    private LocalDate dueDate; // Date the book is due to be returned
    private LocalDate returnDate; // Date the book was returned (null if not returned yet)
    private boolean isBorrowed; // True if the book is currently borrowed, false if returned
    private String status; // Status of the transaction: "PENDING", "APPROVED", "REJECTED"

    // Constructor
    public Transaction(String id, String userEmail, String bookId, LocalDate borrowDate, LocalDate dueDate,LocalDate returnDate, boolean isBorrowed, String status) {
        this.id = id;
        this.userEmail = userEmail;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate  = returnDate;
        this.isBorrowed = isBorrowed;
        this.status = status;
    }

    // Getters
    public String getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public String getBookId() { return bookId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public boolean isBorrowed() { return isBorrowed; }
    public String getStatus() { return status; }

    // Setters
    public void setReturnDate(LocalDate returnDate) {
        if (returnDate != null && returnDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Return date cannot be before borrow date.");
        }
        this.returnDate = returnDate;
        this.isBorrowed = false; // Automatically mark as returned when returnDate is set
    }
    public void setBorrowed(boolean borrowed) {
        isBorrowed = borrowed;
    }

    public void setStatus(String status) {this.status = status;}
    // Helper method to check if the book is returned
    public boolean isReturned() {return returnDate != null;}
}