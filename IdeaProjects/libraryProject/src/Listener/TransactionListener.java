package Listener;

import models.Transaction;

public interface TransactionListener {
    void onReturnSuccess(Transaction transaction); // Called when a book is successfully returned
    void onReturnFailure(String errorMessage);     // Called when a book return fails
    void onRejection(Transaction transaction);  // called when admin reject and borrow fail
    void onAdminApproval(Transaction transaction);
}