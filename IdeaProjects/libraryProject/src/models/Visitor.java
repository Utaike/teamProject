package models;

public class Visitor extends User{
    private static int totalVisitors=0;
    public Visitor(String name, String email, String password,String imgPath) {
        super(name, email, password,imgPath);
        this.role = "visitor";
    }

}
