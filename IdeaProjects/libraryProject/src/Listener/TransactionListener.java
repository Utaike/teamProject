package Listener;

import models.Transaction;

public interface TransactionListener {
    void onBorrowSuccess(Transaction transaction); // Called when a book is successfully borrowed
    void onBorrowFailure(String errorMessage);     // Called when a book borrowing fails
    void onReturnSuccess(Transaction transaction); // Called when a book is successfully returned
    void onReturnFailure(String errorMessage);     // Called when a book return fails
}