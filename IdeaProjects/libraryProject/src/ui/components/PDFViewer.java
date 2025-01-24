package ui.components;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import utils.createStyledButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PDFViewer extends JPanel {
    private PDDocument document;
    private PDFRenderer renderer;
    private JPanel pdfPagesPanel;
    private int currentPageIndex = 0;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JButton nextButton;
    private JButton previousButton;
    private JButton toggleModeButton;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private double scale = 1.0;
    private Point dragStart;
    private boolean isHorizontalMode = false;
    private Map<Integer, BufferedImage> pageCache;

    public PDFViewer(String pdfPath, CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.pageCache = new HashMap<>();
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists() || !pdfFile.isFile()) {
            JOptionPane.showMessageDialog(this, "The file does not exist or is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            document = PDDocument.load(pdfFile);
            renderer = new PDFRenderer(document);

            JPanel titleBar = createTitleBar(pdfPath);
            add(titleBar, BorderLayout.NORTH);

            pdfPagesPanel = new JPanel();
            pdfPagesPanel.setLayout(new BoxLayout(pdfPagesPanel, BoxLayout.Y_AXIS));
            pdfPagesPanel.setBackground(Color.WHITE);

            updatePages();

            JScrollPane pdfScrollPane = new JScrollPane(pdfPagesPanel);
            pdfScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            pdfScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            pdfScrollPane.setBorder(BorderFactory.createEmptyBorder());

            JScrollBar verticalScrollBar = pdfScrollPane.getVerticalScrollBar();
            verticalScrollBar.setUnitIncrement(50);
            verticalScrollBar.setBackground(new Color(245, 245, 245));

            add(pdfScrollPane, BorderLayout.CENTER);

            JPanel controlPanel = createControlPanel();
            add(controlPanel, BorderLayout.SOUTH);

            pdfPagesPanel.addMouseWheelListener(e -> {
                if (e.isControlDown()) {
                    if (e.getWheelRotation() > 0) {
                        zoomOut();
                    } else {
                        zoomIn();
                    }
                } else {
                    JScrollBar verticalScrollBar1 = pdfScrollPane.getVerticalScrollBar();
                    int currentValue = verticalScrollBar1.getValue();
                    int newValue = currentValue + e.getWheelRotation() * verticalScrollBar1.getUnitIncrement();
                    verticalScrollBar1.setValue(newValue);
                }
            });

            pdfPagesPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    dragStart = e.getPoint();
                }
            });

            pdfPagesPanel.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStart != null) {
                        JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, pdfPagesPanel);
                        if (viewport != null) {
                            int deltaX = dragStart.x - e.getX();
                            int deltaY = dragStart.y - e.getY();
                            Rectangle view = viewport.getViewRect();
                            view.x += deltaX;
                            view.y += deltaY;
                            pdfPagesPanel.scrollRectToVisible(view);
                        }
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error opening PDF file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createTitleBar(String pdfPath) {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(60, 63, 65));
        titleBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("PDF Viewer");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleBar.add(titleLabel, BorderLayout.WEST);

        return titleBar;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBackground(new Color(245, 245, 245));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        toggleModeButton = createStyledButton.create("Switch to Horizontal Mode", new Color(90, 160, 255));
        toggleModeButton.addActionListener(e -> toggleViewMode());
        controlPanel.add(toggleModeButton);

        JButton backButton = createStyledButton.create("Back to Library", new Color(180, 34, 34));
        backButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Dashboard");
            try {
                if (document != null) {
                    document.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        controlPanel.add(backButton);

        nextButton = createStyledButton.create("Next", new Color(90, 160, 255));
        nextButton.addActionListener(e -> navigateNext());
        nextButton.setVisible(false);
        controlPanel.add(nextButton);

        previousButton = createStyledButton.create("Previous", new Color(90, 160, 255));
        previousButton.addActionListener(e -> navigatePrevious());
        previousButton.setVisible(false);
        controlPanel.add(previousButton);

        zoomInButton = createStyledButton.create("Zoom In", new Color(90, 160, 255));
        zoomInButton.addActionListener(e -> zoomIn());
        controlPanel.add(zoomInButton);

        zoomOutButton = createStyledButton.create("Zoom Out", new Color(90, 160, 255));
        zoomOutButton.addActionListener(e -> zoomOut());
        controlPanel.add(zoomOutButton);

        return controlPanel;
    }

    private void toggleViewMode() {
        isHorizontalMode = !isHorizontalMode;
        if (isHorizontalMode) {
            pdfPagesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Center pages horizontally
            toggleModeButton.setText("Switch to Vertical Mode");
            nextButton.setVisible(true);
            previousButton.setVisible(true);
        } else {
            pdfPagesPanel.setLayout(new BoxLayout(pdfPagesPanel, BoxLayout.Y_AXIS));
            toggleModeButton.setText("Switch to Horizontal Mode");
            nextButton.setVisible(false);
            previousButton.setVisible(false);
        }
        updatePages();
    }

    private void navigateNext() {
        if (currentPageIndex + 2 < document.getNumberOfPages()) {
            currentPageIndex += 2;
            updatePages();
        }
    }

    private void navigatePrevious() {
        if (currentPageIndex - 2 >= 0) {
            currentPageIndex -= 2;
            updatePages();
        }
    }

    private void zoomIn() {
        scale *= 1.1;
        pageCache.clear(); // Clear the cache to force re-rendering
        updatePages();
    }

    private void zoomOut() {
        scale /= 1.1;
        pageCache.clear(); // Clear the cache to force re-rendering
        updatePages();
    }

    private void updatePages() {
        pdfPagesPanel.removeAll();
        if (isHorizontalMode) {
            for (int i = currentPageIndex; i < currentPageIndex + 2 && i < document.getNumberOfPages(); i++) {
                JPanel pagePanel = createPagePanel(i);
                pdfPagesPanel.add(pagePanel);
            }
        } else {
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                JPanel pagePanel = createPagePanel(i);
                pdfPagesPanel.add(pagePanel);
                pdfPagesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        pdfPagesPanel.revalidate();
        pdfPagesPanel.repaint();
    }

    private JPanel createPagePanel(int pageIndex) {
        JPanel pagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage image = pageCache.get(pageIndex);
                if (image == null) {
                    try {
                        image = renderer.renderImage(pageIndex, (float) scale);
                        pageCache.put(pageIndex, image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (image != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    int x = (getWidth() - image.getWidth()) / 2;
                    int y = (getHeight() - image.getHeight()) / 2;
                    g2d.drawImage(image, x, y, this);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                BufferedImage image = pageCache.get(pageIndex);
                if (image != null) {
                    return new Dimension(image.getWidth(), image.getHeight());
                } else {
                    return new Dimension(600, 800);
                }
            }
        };
        pagePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        return pagePanel;
    }
}