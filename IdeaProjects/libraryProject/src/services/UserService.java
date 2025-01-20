package services;
import models.Admin;
import models.User;
import models.Visitor;
import utils.CSVUtils;
import java.awt.print.Book;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private static final String REGISTER_INFO ="IdeaProjects/libraryProject/src/data/registerInfo.csv";
    private static final int NAME_INDEX = 0;
    private static final int PASSWORD_INDEX = 2;
    private static final int EMAIL_INDEX = 1;
    private static final int ROLE_INDEX=3;
    private static final int IMG_PATH = 4;
    private Map<String,User> userMap; // HashMap for fast lookup(we plan to use email as a key )
    private List<User> userList;  // ArrayList store user in order
    public UserService(){
        this.userMap=new HashMap<>();
        this.userList=new ArrayList<>();
        loadUsers();
    }

public void loadUsers() {
    List<String[]> rows = CSVUtils.readCSV(REGISTER_INFO);
    for (String[] row : rows) {
        if (row.length >= 5) { // Ensure the row has all required fields
            String name = row[NAME_INDEX];
            String email = row[EMAIL_INDEX];
            String password = row[PASSWORD_INDEX];
            String role = row[ROLE_INDEX]; // Role is in the 4th column
            String imgPath = "src/images/profiles/" + row[IMG_PATH]; // Image path is in the 5th column

            User user;
            if (role.equals("admin")) {
                user = new Admin(name, email, password, imgPath); // Create Admin object
            } else {
                user = new Visitor(name, email, password, imgPath); // Create Visitor object
            }

            userMap.put(user.getEmail(), user); // Add to HashMap for fast lookup
            userList.add(user); // Add to ArrayList for displaying and admin operations
        } else {
            System.err.println("Invalid row in CSV: " + String.join(",", row));
        }

    }
    System.out.println("Total users"+getTotalVisitor());

    System.out.println("Loading Registration information successfully");

}
    public boolean validateUser(String email,String password){
        User user =userMap.get(email);
        if(user !=null && user.getPassword().equals(password)){
            return true;
        }
        return false;
    }
    public boolean registerUser(User newUser){
        if (userMap.containsKey(newUser.getEmail())){
            return false;
        }
        userMap.put(newUser.getEmail(),newUser);
        userList.add(newUser);
        List<String []> newData =new ArrayList<>();
        newData.add(new String[]{newUser.getName(),newUser.getEmail(),newUser.getPassword(),newUser.getRole(), newUser.getImgPath()});
        CSVUtils.writeCSV(REGISTER_INFO,newData);
        System.out.println("Register Successfully,Total Users "+getTotalVisitor());
        return true;
    }
    private List<String[]> convertUsersToStringArray(List<User> users) {
        List<String[]> data = new ArrayList<>();
        for (User user : users) {
            data.add(new String[] {user.getName(), user.getPassword(), user.getEmail()});
        }
        return data;
    }

    public User getUserByEmail(String email) {
        return userMap.get(email);
    }
    public int getTotalVisitor() {
        int totalVisitors = 0;
        for (User user : userList) {
            if (user instanceof Visitor) {
                totalVisitors++;
            }
        }
        return totalVisitors;
    }
    public List<User> getAllUsers(){
        return new ArrayList<>(userList);
    }


}
