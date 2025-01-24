package models;

import java.time.LocalDate;

public class Visitor extends User{
    public Visitor(String name, String email, String password, String imgPath, LocalDate registerDate) {
        super(name, email, password,imgPath,registerDate);
        this.role = "visitor";
    }

}
