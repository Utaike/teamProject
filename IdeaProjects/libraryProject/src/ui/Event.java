package ui;

import javax.swing.*;

public class Event extends JFrame {
    Event(){
        this.setSize(400,500);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton button =new JButton("Hello");
        this.add(button);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new  Event();
    }
}
