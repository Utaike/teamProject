package models;

public class Admin extends User {
    public Admin(String name, String email, String password,String imgPath) {
        super(name, email, password,imgPath);
        this.role="admin";
    }

}
