package ui;

import controllers.AdminController;
import models.User;
import models.Visitor;
import utils.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ManageUsersPanel extends JPanel {
    private final AdminController adminController;
    private final DefaultTableModel tableModel;
    private final JTable userTable;

    public ManageUsersPanel(AdminController adminController) {
        this.adminController = adminController;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table Model
        tableModel = new DefaultTableModel(new String[]{"Name", "Email", "Role", "Edit", "Delete"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4; // Only allow interaction with buttons
            }
        };


        // User Table
        userTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);

        // Add Button Renderers and Editors
        userTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        userTable.getColumn("Edit").setCellEditor(new ButtonEditor("Edit"));
        userTable.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        userTable.getColumn("Delete").setCellEditor(new ButtonEditor("Delete"));

        // Add Components
        add(createTopPanel(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Load Data
        refreshUserTable();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());

        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Search users by name, email, or role");
        JButton addButton = new JButton("Add User");
        addButton.setToolTipText("Click to add a new user");

        addButton.addActionListener(e -> showAddUserForm());

        searchField.getDocument().addDocumentListener(new SearchListener(searchField));

        topPanel.add(new JLabel("Search:"), BorderLayout.WEST);

        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(addButton, BorderLayout.EAST);

        return topPanel;
    }

    private void refreshUserTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            List<User> users = adminController.allUsers();
            for (User user : users) {
                tableModel.addRow(new Object[]{user.getName(), user.getEmail(), user.getRole(), "Edit", "Delete"});
            }
        });
    }

    private void showAddUserForm() {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField passwordField = new JTextField();
        JTextField roleField = new JTextField();

        // Add a JLabel and JButton for image selection
        JLabel imageLabel = new JLabel("No image selected");
        JButton chooseImageButton = new JButton("Choose Image");
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        imagePanel.add(new JLabel("Image:"));
        imagePanel.add(imageLabel);
        imagePanel.add(chooseImageButton);

        // File chooser for selecting an image
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));

        // Action listener for the "Choose Image" button
        File[] selectedFile = new File[1]; // Array to hold the selected file
        chooseImageButton.addActionListener(e -> {
            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = fileChooser.getSelectedFile();
                imageLabel.setText(selectedFile[0].getName()); // Display the selected file name
            }
        });

        // Create the form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(roleField);
        formPanel.add(imagePanel); // Add the image selection panel
        formPanel.add(new JLabel()); // Empty label for alignment

        // Show the dialog
        int result = JOptionPane.showConfirmDialog(this, formPanel, "Add User", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            // Validate input fields
            if (nameField.getText().isEmpty() || emailField.getText().isEmpty() ||
                    passwordField.getText().isEmpty() || roleField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Handle image selection and saving
            String imagePath = "default.jpg"; // Default image if no file is selected
            if (selectedFile[0] != null) {
                // Save the selected image and get the unique file name
                String uniqueFileName = FileUtils.saveImage(selectedFile[0]);
                if (uniqueFileName != null) {
                    imagePath = uniqueFileName; // Use the unique file name in the CSV
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save the image.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Create a new user
            User newUser = new Visitor(
                    nameField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    "src/images/profiles/" + imagePath // Store the image path
            );

            // Add the user to the AdminController
            boolean isAdded = adminController.addUser(newUser);

            if (isAdded) {
                JOptionPane.showMessageDialog(this, "User added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshUserTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add user. Email may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editUser(String email) {
        User user = adminController.fetchUserByEmail(email);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "User not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField nameField = new JTextField(user.getName());
        JTextField emailField = new JTextField(user.getEmail());
        JTextField roleField = new JTextField(user.getRole());

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(roleField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Edit User", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            // Update the user
            boolean isUpdated = adminController.updateUser(
                    email,
                    nameField.getText(),
                    emailField.getText(),
                    roleField.getText()
            );

            if (isUpdated) {
                JOptionPane.showMessageDialog(this, "User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshUserTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteUser(String email) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this user?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            boolean isDeleted = adminController.deleteUser(email);

            if (isDeleted) {
                JOptionPane.showMessageDialog(this, "User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshUserTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button;
        private String action;

        public ButtonEditor(String action) {
            button = new JButton(action);
            this.action = action;
            button.setOpaque(true);

            // Button action listener
            button.addActionListener(e -> {
                int row = userTable.getSelectedRow();
                if (row == -1) {
                    // No row selected, show a warning
                    JOptionPane.showMessageDialog(ManageUsersPanel.this, "Please select a user first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String email = (String) tableModel.getValueAt(row, 1); // Get email from row
                if ("Edit".equals(action)) {
                    editUser(email);
                } else if ("Delete".equals(action)) {
                    deleteUser(email);
                }
                fireEditingStopped(); // Stop editing mode
            });
        }

        @Override
        public Object getCellEditorValue() {
            return action;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            button.setText(value == null ? "" : value.toString());
            return button;
        }
    }

    private class SearchListener implements javax.swing.event.DocumentListener {
        private final JTextField searchField;

        public SearchListener(JTextField searchField) {
            this.searchField = searchField;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            filterTable();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            filterTable();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            filterTable();
        }

        private void filterTable() {
            String query = searchField.getText().toLowerCase();
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0); // Clear the table

                List<User> users = adminController.allUsers();
                for (User user : users) {
                    if (user.getName().toLowerCase().contains(query) ||
                            user.getEmail().toLowerCase().contains(query) ||
                            user.getRole().toLowerCase().contains(query)) {
                        tableModel.addRow(new Object[]{user.getName(), user.getEmail(), user.getRole(), "Edit", "Delete"});
                    }
                }
            });
        }
    }
}