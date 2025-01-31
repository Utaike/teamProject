package ui.components;

import utils.createStyledButton;
import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class Menu extends JPanel {
    public Menu(String[] menuItems, String[] iconPaths, Consumer<String> menuAction) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Vertical layout
        setBackground(new Color(204, 204, 255)); // Light purple background
        setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10)); // Padding

        for (int i = 0; i < menuItems.length; i++) {
            String item = menuItems[i];
            String iconPath = (i < iconPaths.length) ? iconPaths[i] : null; // ✅ Prevent Out-of-Bounds

            JButton button = createStyledButton.create(item, new Color(153, 153, 255));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setFont(new Font("Arial", Font.PLAIN, 14));
            button.setForeground(Color.WHITE);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setBackground(new Color(153, 153, 255));
            button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

            if (iconPath != null && !iconPath.isEmpty()) { // ✅ Check if path exists
                ImageIcon icon = new ImageIcon(iconPath);
                Image scaledIcon = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(scaledIcon));
                button.setHorizontalAlignment(SwingConstants.LEFT);
                button.setIconTextGap(10);
            }

            button.addActionListener(e -> menuAction.accept(item));
            add(button);
            add(Box.createRigidArea(new Dimension(0, 20)));
        }

    }
}