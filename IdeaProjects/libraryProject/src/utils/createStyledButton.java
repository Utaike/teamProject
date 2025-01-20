package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class createStyledButton {

    // Static method to create a styled button
    public static JButton create(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        Color hoverColor = backgroundColor.darker(); // Darker color for hover effect

        // Set default button properties
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setOpaque(true); // Ensure the background is painted
        button.setContentAreaFilled(false); // Prevent default background painting
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Add padding

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });

        // Custom painting for rounded corners
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(button.getBackground());
                g2.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), 20, 20); // Rounded corners
                super.paint(g2, c);
                g2.dispose();
            }
        });

        return button;
    }
}
