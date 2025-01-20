package controllers;

import models.User;
import models.Visitor;
import services.UserService;

public class UserController {
    private UserService userService;
    public UserController(){
        userService =new UserService(); // load data.
    }
    public boolean login(String email, String password) {
        return userService.validateUser(email, password);  // Validate user using UserService
    }
    public boolean register(String username, String email, String password, String imgPath) {
        User newUser = new Visitor(username, email, password,imgPath);
        return userService.registerUser(newUser);  // Register the user using UserService
    }
    // Retrieve a user by email
    public User getUserByEmail(String email) {
        return userService.getUserByEmail(email); // Pass the email parameter directly
    }
}
