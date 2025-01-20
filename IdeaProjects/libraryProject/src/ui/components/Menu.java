package ui.components;

import utils.createStyledButton;
import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class Menu extends JPanel {
    public Menu(String[] menuItems, Consumer<String> menuAction) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Vertical layout
        setBackground(new Color(204, 204, 255)); // Light purple background
        setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10)); // Padding

        for (String item : menuItems) {
            JButton button = createStyledButton.create(item,new Color(153, 153, 255));
            button.setAlignmentX(Component.LEFT_ALIGNMENT); // Align buttons to the left
            button.setFont(new Font("Arial", Font.PLAIN, 14));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(153, 153, 255)); // Purple button
            button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // Fill width

            button.addActionListener(e -> menuAction.accept(item));

            add(button);
            add(Box.createRigidArea(new Dimension(0, 20))); // Spacing between buttons
        }
    }
}
