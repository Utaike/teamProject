package models;

import java.time.LocalDate;
public class Transaction {
    private String id;
    private String userEmail;
    private String bookId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private boolean isBorrow; //true mean borrow.false for returned

    public Transaction(String id, String userEmail, String bookId, LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate, boolean isBorrow) {
        this.id = id;
        this.userEmail = userEmail;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.isBorrow = isBorrow;
    }
    // Getters and setters
    public String getId() { return id; }
    public String getUserEmail() {
        return userEmail;
    }
    public String getBookId() { return bookId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public boolean isBorrow() { return isBorrow;
    }

    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public void setBorrow(boolean isBorrow) { this.isBorrow = isBorrow; }

    // Helper method to check if the book is returned
    public boolean isReturned() {
        return returnDate != null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
