package services;

import models.Book;
import models.Transaction;
import models.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AdminService {
    private UserService userService;
    private BookService bookService;
    private TransactionService transactionService;

    public AdminService() {
        this.userService = new UserService();
        this.bookService = new BookService();
        this.transactionService=new TransactionService();
    }
    public int getTotalUsers(){
        return userService.getTotalVisitor();
    }

    public int getTotalBooks(){
        return bookService.getTotalBooks();
    }
    public int getTotalGenre(){
        return bookService.getUniqueGenres().size();
    }
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }
    public List<Book> getALlBooks(){
        return bookService.getAllBooks();
    }
    public int getAvailableBooks(){
        return bookService.getTotalAvailableBooks();
    }
    public User getUserByEmail(String email){
        return userService.getUserByEmail(email);
    }
    public boolean updateUser(String email, String newName,String newEmail, String newRole){
        return userService.updateUser(email,newName,newEmail,newRole);
    }
    public boolean deleteUser(String email){
        return userService.deleteUser(email);
    }
    public boolean addUser(User user){
        return userService.addUser(user);
    }
    public Map<String,Integer > allGenres() {
        return bookService.getGenreCounts();
    }
    public int getBorrowedBooks(){
        return bookService.getTotalBorrowedBook();
    }
    public List<Transaction> getAllTransactions(){
        return transactionService.getAllTransactions();
    }
    public List<Book> getAllAvailableBooks(){
        return bookService.getAvailableBooks();
    }
    public Map<LocalDate, Integer> getRegistrationsPerDay() {
        return userService.getRegistrationsPerDay();
    }
    public Map<String, Integer> getRoleDistribution() {
        return userService.getRoleDistribution();
    }
    public Book getBookById(String id){
        return bookService.getBookById(id);
    }
    public Transaction getTransactionById(String id){
        return transactionService.getTransactionById(id);
    }
    public boolean addTransaction(String userEmail, String bookId, LocalDate borrowDate, LocalDate dueDate){
        return transactionService.addTransaction(userEmail,bookId,borrowDate,dueDate);
    }
    public boolean deleteTransaction(String id){
        return transactionService.deleteTransaction(id);
    }
    public boolean updateTransaction(Transaction transaction){
        return transactionService.updateTransaction(transaction);
    }
    public String getBookTitleById(String id){
        return bookService.getBookTitleById(id);
    }



}
