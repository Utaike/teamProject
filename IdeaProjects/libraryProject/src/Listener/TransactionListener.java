package Listener;

import models.Transaction;

public interface TransactionListener {
    void onBorrowSuccess(Transaction transaction); // Called when a book is successfully borrowed and accepted by admin
    void onBorrowFailure(String errorMessage);     // Called when a book borrowing fails
    void onReturnSuccess(Transaction transaction); // Called when a book is successfully returned
    void onReturnFailure(String errorMessage);     // Called when a book return fails
    void onRejectSuccess(Transaction transaction);  // called when admin reject and borrow fail
}