package utils;

public class ValidationUtils {
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    public static boolean isValidPassword(String password) {
        return password.length() >= 8; // Add more rules if needed
    }

}
