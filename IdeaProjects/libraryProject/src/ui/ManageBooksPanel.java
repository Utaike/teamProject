package ui;

import controllers.AdminController;
import models.Book;
import utils.FileUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ManageBooksPanel extends JPanel {
    private final AdminController adminController;
    private final DefaultTableModel tableModel;
    private final JTable bookTable;

    public ManageBooksPanel(AdminController adminController) {
        this.adminController = adminController;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table Model
        tableModel = new DefaultTableModel(
                new String[]{"Title", "Author", "Genre", "ISBN", "Status", "Edit", "Delete"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 || column == 6;
            }
        };

        // Initialize Table
        bookTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(bookTable);

        // Add Button Renderers/Editors
        bookTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        bookTable.getColumn("Edit").setCellEditor(new ButtonEditor("Edit"));
        bookTable.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        bookTable.getColumn("Delete").setCellEditor(new ButtonEditor("Delete"));

        // Add Components
        add(createTopPanel(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Load Initial Data
        refreshBookTable();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField(20);
        JButton addButton = new JButton("Add Book");

        searchField.getDocument().addDocumentListener(new SearchListener(searchField));
        addButton.addActionListener(e -> showAddBookForm());

        topPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(addButton, BorderLayout.EAST);

        return topPanel;
    }

    private void refreshBookTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            List<Book> books = adminController.getAllBooks();
            for (Book book : books) {
                tableModel.addRow(new Object[]{
                        book.getTitle(),
                        book.getAuthor(),
                        book.getGenre(),
                        book.getIsbn(),
                        book.isAvailable() ? "Available" : "Borrowed",
                        "Edit",
                        "Delete"
                });
            }
        });
    }

    private void showAddBookForm() {
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField genreField = new JTextField();
        JTextField isbnField = new JTextField();
        JTextArea descriptionArea = new JTextArea(3, 20);
        JCheckBox availableCheckbox = new JCheckBox("Available");

        // File Upload Components
        JLabel coverLabel = new JLabel("No cover selected");
        JButton chooseCoverButton = new JButton("Choose Cover");
        JLabel pdfLabel = new JLabel("No PDF selected");
        JButton choosePDFButton = new JButton("Choose PDF");

        JFileChooser imageChooser = new JFileChooser();
        imageChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png"));

        JFileChooser pdfChooser = new JFileChooser();
        pdfChooser.setFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));

        File[] selectedCover = new File[1];
        File[] selectedPDF = new File[1];

        chooseCoverButton.addActionListener(e -> {
            if (imageChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedCover[0] = imageChooser.getSelectedFile();
                coverLabel.setText(selectedCover[0].getName());
            }
        });

        choosePDFButton.addActionListener(e -> {
            if (pdfChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedPDF[0] = pdfChooser.getSelectedFile();
                pdfLabel.setText(selectedPDF[0].getName());
            }
        });

        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        fieldsPanel.add(new JLabel("Title:"));
        fieldsPanel.add(titleField);
        fieldsPanel.add(new JLabel("Author:"));
        fieldsPanel.add(authorField);
        fieldsPanel.add(new JLabel("Genre:"));
        fieldsPanel.add(genreField);
        fieldsPanel.add(new JLabel("ISBN:"));
        fieldsPanel.add(isbnField);
        fieldsPanel.add(new JLabel("Description:"));
        fieldsPanel.add(new JScrollPane(descriptionArea));
        fieldsPanel.add(new JLabel("Available:"));
        fieldsPanel.add(availableCheckbox);

        JPanel filePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        filePanel.add(new JLabel("Cover Image:"));
        filePanel.add(coverLabel);
        filePanel.add(chooseCoverButton);
        filePanel.add(new JLabel("PDF File:"));
        filePanel.add(pdfLabel);
        filePanel.add(choosePDFButton);

        formPanel.add(fieldsPanel, BorderLayout.NORTH);
        formPanel.add(filePanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this, formPanel, "Add New Book", JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            if (titleField.getText().isEmpty() || authorField.getText().isEmpty() ||
                    genreField.getText().isEmpty() || isbnField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String coverPath = handleFileUpload(selectedCover[0]);
            String pdfPath = handleFileUpload(selectedPDF[0]);

            Book newBook = new Book(
                    isbnField.getText(),
                    titleField.getText(),
                    genreField.getText(),
                    authorField.getText(),
                    coverPath,
                    pdfPath,
                    availableCheckbox.isSelected(),
                    descriptionArea.getText()
            );

            if (adminController.addBook(newBook)) {
                JOptionPane.showMessageDialog(this, "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshBookTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add book. ISBN might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        }
    }

//    private String handleFileUpload(File file, String folder) {
//        if (file == null) return "";
//        String uniqueName = FileUtils.saveImage(file);
//        return uniqueName != null ? "src/images/" + folder + "/" + uniqueName : "";
//    }
    // In ManageBooksPanel.java

    private String handleFileUpload(File sourceFile) {
        if (sourceFile == null) return "";

        String targetDirPath = "src/images/books"; // Unified directory
        File targetDir = new File(targetDirPath);
        if (!targetDir.exists()) targetDir.mkdirs();

        String fileName = sourceFile.getName();
        File destFile = new File(targetDir, fileName);

        try {
            java.nio.file.Files.copy(
                    sourceFile.toPath(),
                    destFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            return "images/books/" + fileName; // Path format you want
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }



    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBorderPainted(false);
            setFocusPainted(false);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            String text = value.toString();
            setText(text);

            // Custom styling based on button type
            if ("Edit".equals(text)) {
                setBackground(new Color(70, 130, 180));  // Steel Blue
                setForeground(Color.WHITE);
            } else if ("Delete".equals(text)) {
                setBackground(new Color(220, 20, 60));   // Crimson Red
                setForeground(Color.WHITE);
            }

            setFont(new Font("SansSerif", Font.BOLD, 12));
            return this;
        }
    }

    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button;
        private Color originalBg;
        private Color originalFg;

        public ButtonEditor(String action) {
            this.button = new JButton(action);
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setFont(new Font("SansSerif", Font.BOLD, 12));

            // Set initial colors
            if ("Edit".equals(action)) {
                button.setBackground(new Color(70, 130, 180));  // Steel Blue
                button.setForeground(Color.WHITE);
            } else if ("Delete".equals(action)) {
                button.setBackground(new Color(220, 20, 60));   // Crimson Red
                button.setForeground(Color.WHITE);
            }

            originalBg = button.getBackground();
            originalFg = button.getForeground();

            // Hover effects
            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(originalBg.darker());
                    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                public void mouseExited(MouseEvent e) {
                    button.setBackground(originalBg);
                    button.setCursor(Cursor.getDefaultCursor());
                }
            });

            button.addActionListener(e -> {
                int row = bookTable.getSelectedRow();
                String isbn = (String) tableModel.getValueAt(row, 3);
                if ("Edit".equals(action)) {
                    editBook(isbn);
                } else if ("Delete".equals(action)) {
                    deleteBook(isbn);
                }
                fireEditingStopped();
            });
        }

        public Object getCellEditorValue() {
            return button.getText();
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            return button;
        }
    }

    private class SearchListener implements DocumentListener {
        private final JTextField searchField;

        public SearchListener(JTextField searchField) {
            this.searchField = searchField;
        }

        public void insertUpdate(DocumentEvent e) {
            filter();
        }

        public void removeUpdate(DocumentEvent e) {
            filter();
        }

        public void changedUpdate(DocumentEvent e) {
            filter();
        }

        private void filter() {
            String query = searchField.getText().toLowerCase();
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                for (Book book : adminController.getAllBooks()) {
                    if (book.getTitle().toLowerCase().contains(query) ||
                            book.getAuthor().toLowerCase().contains(query) ||
                            book.getGenre().toLowerCase().contains(query) ||
                            book.getIsbn().toLowerCase().contains(query)) {
                        tableModel.addRow(new Object[]{
                                book.getTitle(),
                                book.getAuthor(),
                                book.getGenre(),
                                book.getIsbn(),
                                book.isAvailable() ? "Available" : "Borrowed",
                                "Edit",
                                "Delete"
                        });
                    }
                }
            });
        }
    }


    private void editBook(String isbn) {
        Book book = adminController.getBookByISBN(isbn);
        if (book == null) {
            JOptionPane.showMessageDialog(this, "Book not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Local file arrays
        File[] selectedCover = new File[1];
        File[] selectedPDF = new File[1];

        // Create form components with existing values
        JTextField titleField = new JTextField(book.getTitle());
        JTextField authorField = new JTextField(book.getAuthor());
        JTextField genreField = new JTextField(book.getGenre());
        JTextField isbnField = new JTextField(book.getIsbn());
        JTextArea descriptionArea = new JTextArea(book.getDescription());
        JCheckBox availableCheckbox = new JCheckBox("Available", book.isAvailable());

        // Initialize file components with existing paths
        JLabel coverLabel = new JLabel(book.getImgPath());
        JLabel pdfLabel = new JLabel(book.getLink());

        // File chooser setup
        JFileChooser imageChooser = new JFileChooser();
        imageChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png"));

        JFileChooser pdfChooser = new JFileChooser();
        pdfChooser.setFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));

        // Add file selection listeners
        JButton chooseCoverButton = new JButton("Change Cover");
        chooseCoverButton.addActionListener(e -> {
            if (imageChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedCover[0] = imageChooser.getSelectedFile();
                coverLabel.setText(selectedCover[0].getName());
            }
        });

        JButton choosePDFButton = new JButton("Change PDF");
        choosePDFButton.addActionListener(e -> {
            if (pdfChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedPDF[0] = pdfChooser.getSelectedFile();
                pdfLabel.setText(selectedPDF[0].getName());
            }
        });

        // Build the form panel
        JPanel formPanel = new JPanel(new BorderLayout(5, 5));

        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        fieldsPanel.add(new JLabel("Title:"));
        fieldsPanel.add(titleField);
        fieldsPanel.add(new JLabel("Author:"));
        fieldsPanel.add(authorField);
        fieldsPanel.add(new JLabel("Genre:"));
        fieldsPanel.add(genreField);
        fieldsPanel.add(new JLabel("ISBN:"));
        fieldsPanel.add(isbnField);
        fieldsPanel.add(new JLabel("Description:"));
        fieldsPanel.add(new JScrollPane(descriptionArea));
        fieldsPanel.add(new JLabel("Available:"));
        fieldsPanel.add(availableCheckbox);

        JPanel filePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        filePanel.add(new JLabel("Cover Image:"));
        filePanel.add(coverLabel);
        filePanel.add(chooseCoverButton);
        filePanel.add(new JLabel("PDF File:"));
        filePanel.add(pdfLabel);
        filePanel.add(choosePDFButton);

        formPanel.add(fieldsPanel, BorderLayout.NORTH);
        formPanel.add(filePanel, BorderLayout.CENTER);

        // Show dialog
        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                "Edit Book",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            // Handle file uploads
            String newCoverPath = handleFileUpload(selectedCover[0]);
            String newPdfPath = handleFileUpload(selectedPDF[0]);

            // Preserve existing paths if no new files selected
            if (newCoverPath.isEmpty()) newCoverPath = book.getImgPath();
            if (newPdfPath.isEmpty()) newPdfPath = book.getLink();

            // Create updated book object
            Book updatedBook = new Book(
                    isbnField.getText(),
                    titleField.getText(),
                    genreField.getText(),
                    authorField.getText(),
                    newCoverPath,
                    newPdfPath,
                    availableCheckbox.isSelected(),
                    descriptionArea.getText()
            );
            updatedBook.setId(book.getId()); // Preserve original ID

            // Perform update
            if (adminController.updateBook(isbn, updatedBook)) {
                JOptionPane.showMessageDialog(this,
                        "Book updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                refreshBookTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Update failed. ISBN might already exist.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void deleteBook(String isbn) {
        int confirm = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to delete this book?", "Confirm Delete", JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION && adminController.deleteBook(isbn)) {
            JOptionPane.showMessageDialog(this, "Book deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshBookTable();
        }
    }
}

