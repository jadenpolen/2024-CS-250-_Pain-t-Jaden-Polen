package com.example.imageeditorjaden;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage; // Import this class
import javafx.scene.paint.Color;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class DrawingCanvas {
    private Canvas canvas;
    private GraphicsContext gc;
    private double startX, startY;
    private Image currentImage;
    private double lineWidth = 2; // Default line width
    private Color lineColor = Color.BLACK; // Default line color
    private String currentShape; // Current shape to draw
    private boolean isPenActive = false;
    private List<ShapeData> shapes; // List to store shapes
    private boolean isSelecting = false;
    private ShapeData selectedShape = null; // Currently selected shape

    public DrawingCanvas(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        gc.setStroke(lineColor);
        gc.setLineWidth(lineWidth);
        shapes = new ArrayList<>();
        initializeMouseHandlers();
    }

        public void setLineWidth(double width) {
        this.lineWidth = width;
        gc.setLineWidth(lineWidth);
    }

        public void setLineColor(Color color) {
        this.lineColor = color;
        gc.setStroke(lineColor);
    }

       public void saveImage(File file) {
        try {
            WritableImage snapshot = canvas.snapshot(null, null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
            ImageIO.write(bufferedImage, "png", file); // Save as PNG
        } catch (IOException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }
    }

    /**
     * Sets the current shape to be drawn on the canvas.
     *
     * @param shape The name of the shape to set (e.g., "circle", "rectangle").
     */
    public void setShape(String shape) {
        this.currentShape = shape;
        isPenActive = false;
        initializeMouseHandlers();
    }

    public void setSelectActive(boolean isActive) {
        isSelecting = isActive; // Set the selection state
        if (isActive) {
            selectedShape = null; // Clear previous selection
        }
    }

    public void setPenActive(boolean isActive) {
        this.isPenActive = isActive;
        if (isActive) {
            currentShape = null; // Clear current shape
        }
        initializeMouseHandlers();
    }

    private void initializeMouseHandlers() {
        canvas.setOnMousePressed(e -> {
            if (isSelecting) {
                updateSelection(e.getX(), e.getY());
            } else {
                startDrawing(e.getX(), e.getY());
            }
        });

        canvas.setOnMouseDragged(e -> {
            if (isPenActive) {
                drawWithPen(e.getX(), e.getY());
            } else if (isSelecting) {
                updateSelection(e.getX(), e.getY());
            } else {
                drawCurrentShape(e.getX(), e.getY());
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (isPenActive) {
                finalizeLine();
            } else if (isSelecting) {
                finalizeSelection();
            } else {
                finalizeShape(e.getX(), e.getY());
            }
        });
    }

    /**
     * Initiates the drawing process at the specified coordinates.
     *
     * @param x The x-coordinate where the drawing starts.
     * @param y The y-coordinate where the drawing starts.
     */
    private void startDrawing(double x, double y) {
        startX = x;
        startY = y;


        if (isPenActive) {
            gc.beginPath();
            gc.moveTo(startX, startY);
            gc.setStroke(lineColor);
            gc.setLineWidth(lineWidth);
            gc.stroke();
        }
    }

    private void drawWithPen(double x, double y) {
        gc.lineTo(x, y);
        gc.stroke();
    }

    private void updateSelection(double x, double y) {
        selectedShape = null; // Reset the selected shape
        for (ShapeData shape : shapes) {
            if (shape.startX <= x && x <= shape.endX && shape.startY <= y && y <= shape.endY) {
                selectedShape = shape; // Set the clicked shape as selected
                shape.highlighted = true; // Highlight the shape
                shape.color = Color.RED; // Change the color when highlighted
                break;
            } else {
                shape.highlighted = false; // Reset highlight if not selected
                shape.color = shape.originalColor; // Restore original color
            }
        }
        redrawCanvas();
    }

    private void finalizeSelection() {
        isSelecting = false; // End the selection
        redrawCanvas(); // Redraw to show selected shapes
    }

    private void drawOctagon(double startX, double startY, double endX, double endY) {
        double centerX = (startX + endX) / 2;
        double centerY = (startY + endY) / 2;
        double radius = Math.hypot(endX - startX, endY - startY) / 2; // Calculate radius
        double[] xPoints = new double[8];
        double[] yPoints = new double[8];

        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(45 * i); // 45 degrees apart
            xPoints[i] = centerX + radius * Math.cos(angle);
            yPoints[i] = centerY + radius * Math.sin(angle);
        }

        gc.strokePolygon(xPoints, yPoints, 8); // Draw octagon
    }

    private void drawCurrentShape(double x, double y) {
        redrawCanvas();

        if (currentShape != null) {
            switch (currentShape) {
                case "square":
                    gc.strokeRect(startX, startY, x - startX, x - startX);
                    break;
                case "circle":
                    double radius = Math.hypot(x - startX, y - startY);
                    gc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
                    break;
                case "rectangle":
                    gc.strokeRect(startX, startY, x - startX, y - startY);
                    break;
                case "ellipse":
                    gc.strokeOval(startX, startY, x - startX, y - startY);
                    break;
                case "triangle":
                    gc.strokePolygon(new double[]{startX, x, (startX + x) / 2},
                            new double[]{startY, y, startY - (y - startY)}, 3);
                    break;
                case "octagon":
                    drawOctagon(startX, startY, x, y);
                    break;
                // Add more shapes as needed
            }
        }
    }

    private void finalizeShape(double x, double y) {
        ShapeData shapeData = new ShapeData(currentShape, startX, startY, x, y);
        shapes.add(shapeData);
        redrawCanvas();
    }

    private void finalizeLine() {
        // Placeholder for finalizing the line; customize as needed
    }

    private void redrawCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (currentImage != null) {
            gc.drawImage(currentImage, 0, 0);
        }

        redrawShapes();
    }

    private void redrawShapes() {
        for (ShapeData shape : shapes) {
            gc.setStroke(shape.highlighted ? Color.RED : shape.originalColor); // Highlight color
            gc.setLineWidth(shape.highlighted ? 3 : lineWidth); // Thicker line for highlighted shapes


            switch (shape.type) {
                case "square":
                    gc.strokeRect(shape.startX, shape.startY, shape.endX - shape.startX, shape.endX - shape.startX);
                    break;
                case "circle":
                    double radius = Math.hypot(shape.endX - shape.startX, shape.endY - shape.startY);
                    gc.strokeOval(shape.startX - radius, shape.startY - radius, radius * 2, radius * 2);
                    break;
                case "rectangle":
                    gc.strokeRect(shape.startX, shape.startY, shape.endX - shape.startX, shape.endY - shape.startY);
                    break;
                case "ellipse":
                    gc.strokeOval(shape.startX, shape.startY, shape.endX - shape.startX, shape.endY - shape.startY);
                    break;
                case "triangle":
                    gc.strokePolygon(new double[]{shape.startX, shape.endX, (shape.startX + shape.endX) / 2},
                            new double[]{shape.startY, shape.endY, shape.startY - (shape.endY - shape.startY)}, 3);
                    break;
                case "octagon":
                    drawOctagon(shape.startX, shape.startY, shape.endX, shape.endY);
                    break;
                // Add more shapes as needed
            }
        }
    }

    public void setImage(Image image) {
        this.currentImage = image;
        if (currentImage != null) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(currentImage, 0, 0);
        }
    }

    public void clear() {
        shapes.clear();
        currentImage = null; // Clear the current image
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private class ShapeData {
        String type;
        double startX, startY, endX, endY;
        boolean highlighted = false; // New property for highlighting
        Color originalColor; // Store original color
        Color color; // Current color for the shape

        ShapeData(String type, double startX, double startY, double endX, double endY) {
            this.type = type;
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.originalColor = lineColor; // Set original color
            this.color = originalColor; // Initialize current color
        }
    }
}
