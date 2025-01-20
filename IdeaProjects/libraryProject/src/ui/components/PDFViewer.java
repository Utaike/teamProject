package ui.components;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;
import utils.createStyledButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PDFViewer extends JPanel {
    private PDDocument document; // Holds the PDF document
    private PDFRenderer renderer; // Renders PDF pages to images
    private JPanel pdfPagesPanel; // Panel to display PDF pages
    private int currentPageIndex = 0; // Tracks the current page in horizontal mode
    private CardLayout cardLayout; // Layout for switching between panels
    private JPanel cardPanel; // Main container for panels
    private JButton nextButton; // Button to navigate to the next page in horizontal mode
    private JButton previousButton; // Button to navigate to the previous page in horizontal mode
    private JButton toggleModeButton; // Button to switch between vertical and horizontal modes
    private double scale = 1.0; // Scale factor for zooming
    private Point dragStart; // Tracks the starting point for panning

    // Constructor to initialize the PDF viewer
    public PDFViewer(String pdfPath, CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        setLayout(new BorderLayout()); // Use BorderLayout for the main panel
        setBackground(new Color(245, 245, 245)); // Light gray background
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        // Validate the file path before proceeding
        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists() || !pdfFile.isFile()) {
            JOptionPane.showMessageDialog(this, "The file does not exist or is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Stop loading if the file path is invalid
        }

        try {
            // Load the PDF document
            document = PDDocument.load(pdfFile);
            renderer = new PDFRenderer(document);

            // Create a title bar for the PDF viewer
            JPanel titleBar = createTitleBar(pdfPath);
            add(titleBar, BorderLayout.NORTH); // Add title bar to the top

            // Create a panel to hold the PDF pages
            pdfPagesPanel = new JPanel();
            pdfPagesPanel.setLayout(new BoxLayout(pdfPagesPanel, BoxLayout.Y_AXIS)); // Default to vertical mode
            pdfPagesPanel.setBackground(Color.WHITE); // White background for pages

            // Render the PDF pages
            updateVerticalPages();

            // Create a scroll pane for the PDF pages
            JScrollPane pdfScrollPane = new JScrollPane(pdfPagesPanel);
            pdfScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Enable vertical scroll
            pdfScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Disable horizontal scroll
            pdfScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove scroll pane border

            // Customize the scroll bars
            JScrollBar verticalScrollBar = pdfScrollPane.getVerticalScrollBar();
            verticalScrollBar.setUnitIncrement(16); // Smoother scrolling
            verticalScrollBar.setBackground(new Color(245, 245, 245)); // Match background

            // Add the PDF scroll pane to the viewer panel
            add(pdfScrollPane, BorderLayout.CENTER);

            // Create a control panel for buttons
            JPanel controlPanel = createControlPanel();
            add(controlPanel, BorderLayout.SOUTH); // Add control panel to the bottom

            // Add mouse wheel listener for zooming
            pdfPagesPanel.addMouseWheelListener(e -> {
                if (e.isControlDown()) { // Check if Ctrl key is pressed
                    double scaleFactor = 1.1; // Scale factor for zooming
                    if (e.getWheelRotation() > 0) {
                        scale /= scaleFactor; // Zoom out
                    } else {
                        scale *= scaleFactor; // Zoom in
                    }
                    updateVerticalPages(); // Update the pages with the new scale
                }
            });

            // Add mouse listeners for panning
            pdfPagesPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    dragStart = e.getPoint(); // Record the starting point for panning
                }
            });

            pdfPagesPanel.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStart != null) {
                        JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, pdfPagesPanel);
                        if (viewport != null) {
                            int deltaX = dragStart.x - e.getX(); // Calculate horizontal panning distance
                            int deltaY = dragStart.y - e.getY(); // Calculate vertical panning distance
                            Rectangle view = viewport.getViewRect();
                            view.x += deltaX; // Update the view position
                            view.y += deltaY;
                            pdfPagesPanel.scrollRectToVisible(view); // Scroll to the new position
                        }
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error opening PDF file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Create the title bar for the PDF viewer
    private JPanel createTitleBar(String pdfPath) {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(60, 63, 65)); // Dark background
        titleBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Add padding

        // Add a title label with the PDF file name
        JLabel titleLabel = new JLabel("PDF Viewer: " + new File(pdfPath).getName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Set font
        titleLabel.setForeground(Color.WHITE); // White text
        titleBar.add(titleLabel, BorderLayout.WEST); // Add the label to the left

        return titleBar;
    }

    // Create the control panel with buttons
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Center-align buttons
        controlPanel.setBackground(new Color(245, 245, 245)); // Light gray background
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Add padding

        // Button to toggle between vertical and horizontal modes
        toggleModeButton = createStyledButton.create("Switch to Horizontal Mode",new Color(90, 160, 255));
        toggleModeButton.addActionListener(e -> toggleViewMode()); // Add action listener
        controlPanel.add(toggleModeButton);

        // Button to return to the library view
        JButton backButton = createStyledButton.create("Back to Library",new Color(180, 34, 34));
        backButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Dashboard"); // Switch back to the library view
            try {
                if (document != null) {
                    document.close(); // Close the PDF document
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        controlPanel.add(backButton);

        // Navigation buttons for horizontal mode
        nextButton = createStyledButton.create("Next",new Color(90, 160, 255));
        nextButton.addActionListener(e -> navigateNext()); // Add action listener
        nextButton.setVisible(false); // Initially hidden
        controlPanel.add(nextButton);

        previousButton = createStyledButton.create("Previous",new Color(90, 160, 255));
        previousButton.addActionListener(e -> navigatePrevious()); // Add action listener
        previousButton.setVisible(false); // Initially hidden
        controlPanel.add(previousButton);

        return controlPanel;
    }

    // Toggle between vertical and horizontal modes
    private void toggleViewMode() {
        if (pdfPagesPanel.getLayout() instanceof BoxLayout) {
            // Switch to horizontal mode
            pdfPagesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Use FlowLayout for horizontal mode
            toggleModeButton.setText("Switch to Vertical Mode"); // Update button text
            nextButton.setVisible(true); // Show Next button
            previousButton.setVisible(true); // Show Previous button
            updateHorizontalPages(); // Update the pages for horizontal mode
        } else {
            // Switch to vertical mode
            pdfPagesPanel.setLayout(new BoxLayout(pdfPagesPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical mode
            toggleModeButton.setText("Switch to Horizontal Mode"); // Update button text
            nextButton.setVisible(false); // Hide Next button
            previousButton.setVisible(false); // Hide Previous button
            updateVerticalPages(); // Update the pages for vertical mode
        }
        pdfPagesPanel.revalidate(); // Refresh the panel
        pdfPagesPanel.repaint(); // Repaint the panel
    }

    // Navigate to the next page in horizontal mode
    private void navigateNext() {
        if (currentPageIndex + 2 < document.getNumberOfPages()) {
            currentPageIndex += 2; // Move to the next pair of pages
            updateHorizontalPages(); // Update the pages
        }
    }

    // Navigate to the previous page in horizontal mode
    private void navigatePrevious() {
        if (currentPageIndex - 2 >= 0) {
            currentPageIndex -= 2; // Move to the previous pair of pages
            updateHorizontalPages(); // Update the pages
        }
    }

    // Update the pages for vertical mode
    private void updateVerticalPages() {
        pdfPagesPanel.removeAll(); // Clear the current pages
        for (int i = 0; i < document.getNumberOfPages(); i++) {
            int pageIndex = i;
            JPanel pagePanel = createPagePanel(pageIndex); // Create a panel for the page
            pdfPagesPanel.add(pagePanel); // Add the page to the panel
            pdfPagesPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between pages
        }
        pdfPagesPanel.revalidate(); // Refresh the panel
        pdfPagesPanel.repaint(); // Repaint the panel
    }

    // Update the pages for horizontal mode
    private void updateHorizontalPages() {
        pdfPagesPanel.removeAll(); // Clear the current pages
        for (int i = currentPageIndex; i < currentPageIndex + 2 && i < document.getNumberOfPages(); i++) {
            int pageIndex = i;
            JPanel pagePanel = createPagePanel(pageIndex); // Create a panel for the page
            pdfPagesPanel.add(pagePanel); // Add the page to the panel
        }
        pdfPagesPanel.revalidate(); // Refresh the panel
        pdfPagesPanel.repaint(); // Repaint the panel
    }

    // Create a panel for a single PDF page
    private JPanel createPagePanel(int pageIndex) {
        JPanel pagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    // Render the page to the panel with the current scale
                    BufferedImage image = renderer.renderImage(pageIndex, (float) scale);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.drawImage(image, 0, 0, this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        pagePanel.setPreferredSize(new Dimension((int) (600 * scale), (int) (800 * scale))); // Set size for each page
        pagePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); // Add a border
        return pagePanel;
    }
}