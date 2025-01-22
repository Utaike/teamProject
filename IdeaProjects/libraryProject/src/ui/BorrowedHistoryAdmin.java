package ui;

import controllers.AdminController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BorrowedHistoryAdmin extends JPanel{
    private final AdminController adminController;
    private final DefaultTableModel tableModel;
    private final JTable historyTable;


    public BorrowedHistoryAdmin(AdminController adminController) {
        this.adminController = adminController;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tableModel = new DefaultTableModel(new String[]{"id","userEmail","bookId","borrowDate","dueDate","returnDate","isBorrow"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4; // Only allow interaction with buttons
            }
        };
        historyTable= new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(historyTable);


    }
}
