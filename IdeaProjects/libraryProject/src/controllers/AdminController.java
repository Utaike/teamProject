package controllers;

import models.Book;
import models.Transaction;
import models.User;
import services.AdminService;
import java.util.List;
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
}
