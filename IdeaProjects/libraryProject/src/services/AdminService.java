package services;

import models.Book;
import models.User;

import java.util.List;

public class AdminService {
    private UserService userService;
    private BookService bookService;

    public AdminService() {
        this.userService = new UserService();
        this.bookService = new BookService();
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

}
