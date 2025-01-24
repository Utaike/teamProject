
package services;
import models.Admin;
import models.User;
import models.Visitor;
import utils.CSVUtils;
import java.awt.print.Book;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private static final String REGISTER_HEADER="name,email,password,role,imgPath";
    private static final String REGISTER_INFO ="IdeaProjects/libraryProject/src/data/registerInfo.csv";
    private static final int NAME_INDEX = 0;
    private static final int PASSWORD_INDEX = 2;
    private static final int EMAIL_INDEX = 1;
    private static final int ROLE_INDEX=3;
    private static final int IMG_PATH = 4;
    private static final int REGISTER_DATE = 5;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
    private Map<String,User> userMap; // HashMap for fast lookup(we plan to use email as a key )
    private List<User> userList;  // ArrayList store user in order

    public UserService(){
        this.userMap=new HashMap<>();
        this.userList=new ArrayList<>();

        loadUsers();
    }

    public void loadUsers() {
        List<String[]> rows = CSVUtils.readCSV(REGISTER_INFO);

        for(String[] row:rows){
            if (row.length >= 6){
                String name = row[NAME_INDEX];
                String email = row[EMAIL_INDEX];
                String password = row[PASSWORD_INDEX];
                String role = row[ROLE_INDEX]; // Role is in the 4th column
                String imgPath = "IdeaProjects/libraryProject/src/images/profiles/" + row[IMG_PATH];
                LocalDate registerDate = LocalDate.parse(row[REGISTER_DATE], formatter);
                System.out.println(imgPath);
                User user;
                if (role.equals("admin")) {
                    user = new Admin(name, email, password, imgPath,registerDate); // Create Admin object
                } else {
                    user = new Visitor(name, email, password, imgPath,registerDate); // Create Visitor object
                }
                userMap.put(user.getEmail(), user); // Add to HashMap for fast lookup
                userList.add(user); // Add to ArrayList for displaying and admin operations
            } else {
                System.err.println("Invalid row in CSV: " + String.join(",", row));
            }
        }
        System.out.println("Total users: " + getTotalVisitor());
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
        newData.add(new String[]{newUser.getName(),newUser.getEmail(),newUser.getPassword(),newUser.getRole(), newUser.getImgPath()
        ,newUser.getRegisterDate().toString()
        });
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
    public boolean updateUser(String email,String newName,String newEmail,String newRole ){
        User userToUpdate= userMap.get(email);
        if(userToUpdate==null){
            return false;
        }
        if(!email.equals(newEmail) && userMap.containsKey(newEmail)){
            return false;
        }
        userToUpdate.setName(newName);
        userToUpdate.setEmail(newEmail);
        userToUpdate.setRole(newRole);
        userMap.remove(email);
        userMap.put(newEmail,userToUpdate);
        for(int i =0;i<userList.size();i++){
            if(userList.get(i).getEmail().equals(email)){
                userList.set(i,userToUpdate);
                break;
            }
        }
        List<String[]> updatedData = new ArrayList<>();
        for (User user : userList) {
            updatedData.add(new String[]{
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole(),
                    user.getImgPath().replace("src/images/profiles/", ""),
                    user.getRegisterDate().toString()// Remove the prefix for CSV
            });
        }
        CSVUtils.updateCSV(REGISTER_INFO, updatedData,REGISTER_HEADER);

        return true;
    }
    public boolean deleteUser(String email) {
        // Find the user to delete from the map
        User userToDelete = userMap.get(email);
        if (userToDelete == null) {
            // User not found
            return false;
        }

        // Remove the user from the map
        userMap.remove(email);

        // Remove the user from the list
        userList.removeIf(user -> user.getEmail().equals(email));

        // Prepare the updated data for the CSV file (including the header)
        List<String[]> updatedData = new ArrayList<>();


        // Add the updated user data
        for (User user : userList) {
            updatedData.add(new String[]{
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole(),
                    user.getImgPath().replace("src/images/profiles/", ""),
                    user.getRegisterDate().toString()// Remove the prefix for CSV
            });
        }

        // Update the CSV file by overwriting it with the updated data (header is preserved)
        CSVUtils.updateCSV(REGISTER_INFO, updatedData,REGISTER_HEADER);

        System.out.println("User with email " + email + " has been deleted.");
        return true;
    }
    public boolean addUser(User newUser){
        if(userMap.containsKey(newUser.getEmail())){
            return false;
        }
        userMap.put(newUser.getEmail(),newUser);
        userList.add(newUser);

        List<String[]> newData= new ArrayList<>();
        newData.add(new String[]{
                newUser.getName(),
                newUser.getEmail(),
                newUser.getPassword(),
                newUser.getRole(),
                newUser.getImgPath().replace("src/images/profiles/", ""),
                newUser.getRegisterDate().toString()// Remove the prefix for CSV
        });

        CSVUtils.writeCSV(REGISTER_INFO, newData); // true = append mode

        System.out.println("User added successfully. Total users: " + userList.size());
        return true; // User added successfully
    }
    public Map<LocalDate, Integer> getRegistrationsPerDay() {
        Map<LocalDate, Integer> registrationsPerDay = new HashMap<>();

        for (User user : userList) {
            LocalDate registerDate = user.getRegisterDate();
            registrationsPerDay.put(registerDate, registrationsPerDay.getOrDefault(registerDate, 0) + 1);
        }

        System.out.println("Registrations per day: " + registrationsPerDay); // Debugging
        return registrationsPerDay;
    }
    public Map<String, Integer> getRoleDistribution() {
        Map<String, Integer> roleDistribution = new HashMap<>();

        for (User user : userList) {
            String role = user.getRole();
            roleDistribution.put(role, roleDistribution.getOrDefault(role, 0) + 1);
        }

        System.out.println("Role distribution: " + roleDistribution); // Debugging
        return roleDistribution;
    }
}
