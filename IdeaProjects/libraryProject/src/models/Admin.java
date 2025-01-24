package models;

import java.time.LocalDate;

public class Admin extends User {
    public Admin(String name, String email, String password, String imgPath, LocalDate registerDate) {
        super(name, email, password,imgPath,registerDate);
        this.role="admin";
    }

}
