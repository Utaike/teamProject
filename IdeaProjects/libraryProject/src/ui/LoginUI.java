package ui;

import controllers.AdminController;
import controllers.MenuController;
import utils.createStyledButton;
import controllers.BookController;
import models.Admin;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import controllers.UserController;
import models.User;
import static utils.PlaceHolder.addPasswordPlaceholder;
import static utils.PlaceHolder.addPlaceholder;

public class LoginUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private String uniqueName;
    private File selectedFile;
    private UserController userController =new UserController();

    LoginUI() {
        this.setSize(1000, 700);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Login Page");
        cardLayout =new CardLayout();
        cardPanel=new JPanel(cardLayout);

        cardPanel.add(CreateLoginForm(),"Login");
        cardPanel.add(CreateRegistrationForm(),"Registration");
        this.add(cardPanel);
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.setVisible(true);
        this.setResizable(false);
    }
    private JPanel CreateLoginForm(){
        JPanel loginContainer = new JPanel(new BorderLayout());
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/thumbnails/library.jpg"));
        Image scaledImage = imageIcon.getImage().getScaledInstance(600, 500, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel imgLabel = new JLabel(scaledIcon);

        JPanel imgPanel = new JPanel();
        imgPanel.add(imgLabel);
        JPanel loginPanel =new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel,BoxLayout.Y_AXIS));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Login");
        JLabel welcome=new JLabel("Welcome to Imagine Library");
        welcome.setFont(new Font("Arial", Font.BOLD, 24));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();


        addPlaceholder(emailField, "Enter your email");
        addPasswordPlaceholder(passwordField, "Enter your password");

        emailField.setMaximumSize(new Dimension(300, 30));
        passwordField.setMaximumSize(new Dimension(300, 30));

        JButton loginButton = createStyledButton.create("Login",new Color(153, 153, 255));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel createAccPanel = new JPanel();
        createAccPanel.setLayout(new BoxLayout(createAccPanel,BoxLayout.Y_AXIS));
        createAccPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        createAccPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel crateAccText = new JLabel("Don't have account?");
        JLabel registerLink =new JLabel("Create one");
        crateAccText.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerLink.setForeground(Color.BLUE);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createAccPanel.add(crateAccText);
        createAccPanel.add(registerLink);
        registerLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(cardPanel,"Registration");
            }
        });

        loginButton.addActionListener(e -> {
            String inputEmail = emailField.getText().trim();
            char[] inputPassword = passwordField.getPassword();

            if (inputEmail.isEmpty() || inputEmail.equals("Enter your email") ) {
                JOptionPane.showMessageDialog(
                        LoginUI.this,
                        "Please enter an email!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } else if (!utils.ValidationUtils.isValidEmail(inputEmail)) {
                JOptionPane.showMessageDialog(
                        LoginUI.this,
                        "Please enter a valid email address!(email should be followed by @gmail.com)",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } else if (inputPassword.length == 0 || new String(inputPassword).equals("Enter your password")) {
                JOptionPane.showMessageDialog(
                        LoginUI.this,
                        "Please enter your password!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } else{
                if(userController.login(inputEmail,new String(inputPassword))){

                    User loggedInUser=userController.getUserByEmail(inputEmail);
                    if(loggedInUser.getRole().equals("admin")){
                        AdminController adminController =new AdminController();
                        cardPanel.add(new AdminDashboard(userController.getUserByEmail(inputEmail), adminController,cardLayout,cardPanel),"AdminDashboard");
                        cardLayout.show(cardPanel,"AdminDashboard");
                    }else{
                        cardPanel.add(new UserDashboard(userController.getUserByEmail(inputEmail),cardLayout,cardPanel,new BookController()),"Dashboard");
                        cardLayout.show(cardPanel,"Dashboard");
                        setResizable(true);
                        JOptionPane.showMessageDialog(
                                LoginUI.this,
                                "Hello welcome",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                    }

                }else {
                    JOptionPane.showMessageDialog(
                            LoginUI.this,
                            "Invalid credentials. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

            }
            addPlaceholder(emailField, "Enter your email");
            addPasswordPlaceholder(passwordField, "Enter your password");
        });
        loginPanel.add(titleLabel);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(welcome);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(welcome);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(emailField);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(passwordField);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(loginButton);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(createAccPanel);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(loginButton);

        loginContainer.add(imgPanel,BorderLayout.WEST);
        loginContainer.add(loginPanel,BorderLayout.CENTER);

        return loginContainer;
    }
    private JPanel CreateRegistrationForm(){
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/thumbnails/library.jpg"));
        Image scaledImage = imageIcon.getImage().getScaledInstance(600, 500, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel imgLabel = new JLabel(scaledIcon);

        JPanel imgPanel = new JPanel();
        imgPanel.add(imgLabel);

        JPanel registrationContainer=new JPanel(new BorderLayout());
        JPanel registerPanel=new JPanel();
        registerPanel.setLayout(new BoxLayout(registerPanel,BoxLayout.Y_AXIS));
        registerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Registration");
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel welcome=new JLabel("Welcome to Imagine Library");
        welcome.setFont(new Font("Arial", Font.PLAIN, 16));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);


        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField nameField=new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();


        addPlaceholder(nameField,"Enter your name");
        addPlaceholder(emailField, "Enter your email");
        addPasswordPlaceholder(passwordField, "Enter your password");
        nameField.setMaximumSize(new Dimension(300,30));
        emailField.setMaximumSize(new Dimension(300, 30));
        passwordField.setMaximumSize(new Dimension(300, 30));
        JButton uploadButton=createStyledButton.create("Choose profile",new Color(153, 153, 255));
        JLabel filePathLabel=new JLabel();
        uploadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        filePathLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        uploadButton.addActionListener(e->{
            JFileChooser fileChooser =new JFileChooser();
            fileChooser.setDialogTitle("Choose your profile picture");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files ", "jpg", "png", "jpeg"));
            int result=fileChooser.showOpenDialog(registerPanel);
            if(result==JFileChooser.APPROVE_OPTION){
                 selectedFile = fileChooser.getSelectedFile();
                filePathLabel.setText("Selected: "+selectedFile.getName());

            }else{
                filePathLabel.setText("No file selected");
            }
        });

        JLabel backToLogin=new JLabel("Back to login");
        backToLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToLogin.setForeground(Color.BLUE);

        backToLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
               cardLayout.show(cardPanel,"Login");
            }
        });

        JButton registerBtn =createStyledButton.create("Register",new Color(153, 153, 255));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> {
            String inputName=nameField.getText().trim();
            String inputEmail = emailField.getText().trim();
            char[] inputPassword = passwordField.getPassword();
            if(inputName.isEmpty() || inputName.equals("Enter your name") ){
                JOptionPane.showMessageDialog(this,"Please enter your name","error",JOptionPane.ERROR_MESSAGE);
            }
            else if (inputEmail.isEmpty() || inputEmail.equals("Enter your email") ) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter an email!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } else if (!utils.ValidationUtils.isValidEmail(inputEmail)) {
                JOptionPane.showMessageDialog(
                        LoginUI.this,
                        "Please enter a valid email address!(email should be followed by @gmail.com)",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } else if (inputPassword.length == 0 || new String(inputPassword).equals("Enter your password") ) {
                JOptionPane.showMessageDialog(
                        LoginUI.this,
                        "Please enter a password!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

            }else if(selectedFile ==null){
                JOptionPane.showMessageDialog(this,"Please select your profile","Error",JOptionPane.ERROR_MESSAGE);
            }
            else{
                uniqueName = utils.FileUtils.saveImage(selectedFile);
                System.out.println(uniqueName);
                if(uniqueName ==null){
                    JOptionPane.showMessageDialog(this,"Error saving profile picture!","Error",JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Handle registration logic
                boolean registrationSuccess = userController.register(inputName, inputEmail, new String(inputPassword),uniqueName);
                if (registrationSuccess) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Registration successful! Please login.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    filePathLabel.setText("");
                    cardLayout.show(cardPanel, "Login");

                    // Clear fields
                    addPlaceholder(nameField,"Enter your name");
                    addPlaceholder(emailField, "Enter your email");
                    addPasswordPlaceholder(passwordField, "Enter your password");
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Registration failed. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        });

        registerPanel.add(titleLabel);
        registerPanel.add(Box.createVerticalStrut(10));
        registerPanel.add(welcome);
        registerPanel.add(Box.createVerticalStrut(10));
        registerPanel.add(nameField);
        registerPanel.add(Box.createVerticalStrut(10));
        registerPanel.add(emailField);
        registerPanel.add(Box.createVerticalStrut(10));
        registerPanel.add(passwordField);
        registerPanel.add(Box.createVerticalStrut(10));
        registerPanel.add(uploadButton);
        registerPanel.add(filePathLabel);
        registerPanel.add(Box.createVerticalStrut(10));
        registerPanel.add(registerBtn);
        registerPanel.add(Box.createVerticalStrut(20));
        registerPanel.add(backToLogin);
        registrationContainer.add(imgPanel,BorderLayout.WEST);
        registrationContainer.add(registerPanel,BorderLayout.CENTER);

        return registrationContainer;
    }



//    private boolean isValidEmail(String email) {
//        String emailRegex = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"; // email validation
//        return email.matches(emailRegex);
//    }
    public static void main(String[] args) {
        new LoginUI();
    }
}
