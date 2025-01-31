package controllers;

import models.Book;
import models.Transaction;
import models.User;
import services.AdminService;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminController {
    private AdminService adminService;
    public AdminController() {
        this.adminService=new AdminService();
    }
    public int totalUsers(){
        return adminService.getTotalUsers();
    }
    public int totalBooks(){
        return adminService.getTotalBooks();
    }
    public int totalGenre(){
        return adminService.getTotalGenre();
    }
    public List<User> allUsers(){
        return adminService.getAllUsers();
    }
    public List<Book> allBooks(){

        return adminService.getALlBooks();
    }
    public List<Book> allAvailableBooks(){
        return adminService.getAllAvailableBooks();
    }
    public int totalAvailableBooks(){
        return adminService.getAvailableBooks();
    }
    public User fetchUserByEmail(String email){
        return adminService.getUserByEmail(email);
    }
    public boolean updateUser(String email,String newName,String newEmail,String newRole){
        return adminService.updateUser(email,newName,newEmail,newRole);
    }
    public boolean deleteUser(String email){
        return adminService.deleteUser(email);
    }
    public boolean addUser(User user){
        return adminService.addUser(user);
    }
    public Map<String,Integer> allGenres(){
        return adminService.allGenres();
    }
    public int totalBorrowedBooks(){
        return adminService.getBorrowedBooks();
    }
    public List<Transaction> allTransactions(){
        return adminService.getAllTransactions();
    }
    public Map<LocalDate,Integer> getRegisterPerDay(){
        return adminService.getRegistrationsPerDay();
    }
    public Map<String, Integer> getRoleDistribution() {
        return adminService.getRoleDistribution();
    }
    public Transaction getTransactionById(String id){
        return adminService.getTransactionById(id);
    }
    public boolean addTransaction(String userEmail, String bookId, LocalDate borrowDate, LocalDate dueDate){
        return adminService.addTransaction(userEmail,bookId,borrowDate,dueDate);
    }
    public boolean deleteTransaction(String id){
        return adminService.deleteTransaction(id);
    }
    public boolean updateTransaction(Transaction transaction){
        return adminService.updateTransaction(transaction);
    }
    public String getBookTitle(String id){
        return adminService.getBookTitleById(id);
    }
    // Add these methods if missing
    public List<Book> getAllBooks() {
        return adminService.getAllBooks();
    }

    public boolean addBook(Book book) {
        return adminService.addBook(book);
    }

    public Book getBookByISBN(String isbn) {
        return adminService.getBookByISBN(isbn);
    }

    public boolean deleteBook(String isbn) {
        return adminService.deleteBook(isbn);
    }

    public boolean updateBook(String originalISBN, Book updatedBook) {
        return adminService.updateBook(originalISBN, updatedBook);
    }
    public List<Transaction> allBorrowedBooks(){
        return adminService.getAllBorrowedBooks();
    }
    public Book getBookById(String bookID) {
        return adminService.getBookById(bookID);
    }
}
