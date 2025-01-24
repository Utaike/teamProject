package models;

import java.time.LocalDate;

abstract public class User {

    protected String name;
    //    protected String id;
    protected String password;
    protected String email;
    protected String imgPath;
    protected String role;
    protected LocalDate registerDate;

    public User(String name, String email,String password,String imgPath,LocalDate registerDate) {
        this.email = email;
        this.password = password;
        this.name=name;
        this.imgPath=imgPath;
        this.registerDate=registerDate;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
    public String getRole(){
        return role;
    }

    public String getImgPath() {
        return imgPath;
    }
    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(LocalDate registerDate) {
        this.registerDate = registerDate;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public void setRole(String role) {
        this.role = role;
    }
}


