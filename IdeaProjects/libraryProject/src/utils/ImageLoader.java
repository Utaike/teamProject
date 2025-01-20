package utils;

import javax.swing.*;
import java.awt.*;

public class ImageLoader {

    /**
     * Loads and resizes an image from the specified path.
     *
     * @param path   The path to the image resource.
     * @param width  The desired width of the image.
     * @param height The desired height of the image.
     * @return An ImageIcon with the resized image, or an empty ImageIcon if the image is not found.
     */
    public static ImageIcon loadImageIcon(String path, int width, int height) {
        try {
            // Ensure the path starts with a '/' if it's an absolute path
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            // Load the image
            ImageIcon icon = new ImageIcon(ImageLoader.class.getResource(path));
            if (icon.getImage() == null) {
                throw new NullPointerException("Image not found at the specified path: " + path);
            }

            // Resize the image
            Image resizedImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resizedImage);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            return new ImageIcon(); // Fallback: Return an empty icon
        }
    }
}
