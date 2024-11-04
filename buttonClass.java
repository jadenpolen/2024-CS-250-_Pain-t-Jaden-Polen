package com.example.imageeditorjaden;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.Optional;

public class buttonClass {
    // Existing buttons and fields
    private Button openButton;
    private Button clearButton;
    private Button saveButton;
    private Button saveAsButton;
    private Button optionsButton;
    private Button lineOptionsButton;
    private Button helpButton;
    private Button insertShapesButton;
    private Button penButton; // New Pen button
    private Button undoButton; // Empty Undo button
    private Button selectButton; // New Select button
    private HBox shapeButtonsBox;
    private String currentShape;
    private HBox lineOptionsHBox;
    private HBox buttonBox;
    private boolean isPenActive = false; // Track pen state
    private boolean isSelectActive = false; // Track select state
    private openImage mainApp; // Reference to main openImage class
    private Button toggleTimerButton;

    /**
     * Initializes a new buttonClass instance, setting up buttons and their actions
     *
     * @param drawingCanvas
     * @param primaryStage
     * @param lineOptionsHBox
     * @param buttonBox
     * @param mainApp
     */
    public buttonClass(DrawingCanvas drawingCanvas, Stage primaryStage, HBox lineOptionsHBox, HBox buttonBox, openImage mainApp) {
        this.lineOptionsHBox = lineOptionsHBox;
        this.buttonBox = buttonBox;
        this.mainApp = mainApp; // Store reference to main class
        initializeButtons(drawingCanvas, primaryStage);
        createToggleTimerButton();
    }

    private void createToggleTimerButton() {
        toggleTimerButton = new Button("Toggle Timer");
        toggleTimerButton.setOnAction(e -> {
            mainApp.setTimerVisibility(); // Updated method call
        });
        buttonBox.getChildren().add(toggleTimerButton);
    }

    private void initializeButtons(DrawingCanvas drawingCanvas, Stage primaryStage) {
        openButton = new Button("Open Image");
        clearButton = new Button("Clear Screen");
        saveButton = new Button("Save Image");
        saveAsButton = new Button("Save Image As");
        optionsButton = new Button("Image Options");
        lineOptionsButton = new Button("Line Options");
        helpButton = new Button("Help");
        insertShapesButton = new Button("Insert Shapes");

        penButton = new Button("Pen");
        penButton.setOnAction(e -> togglePen(drawingCanvas));

        selectButton = new Button("Select");
        selectButton.setOnAction(e -> toggleSelect(drawingCanvas));

        undoButton = new Button("Undo");

        shapeButtonsBox = new HBox(10);
        createShapeButtons(drawingCanvas);

        // Set button actions
        openButton.setOnAction(e -> openImage(drawingCanvas, primaryStage));
        clearButton.setOnAction(e -> clearCanvas(drawingCanvas));
        saveButton.setOnAction(e -> {
            saveImage(drawingCanvas);
            mainApp.resetTimer(); // Ensure resetTimer() is public in openImage
        });
        saveAsButton.setOnAction(e -> saveImageAs(drawingCanvas, primaryStage));
        optionsButton.setOnAction(e -> toggleImageOptions());
        lineOptionsButton.setOnAction(e -> toggleLineOptions());
        insertShapesButton.setOnAction(e -> toggleShapeButtons());
        helpButton.setOnAction(e -> showHelpDialog());

        buttonBox.getChildren().addAll(openButton, clearButton, saveButton, saveAsButton, optionsButton,
                lineOptionsButton, helpButton, insertShapesButton, penButton,
                selectButton, undoButton, shapeButtonsBox);
        shapeButtonsBox.setVisible(false);
    }

    private void togglePen(DrawingCanvas drawingCanvas) {
        isPenActive = !isPenActive;
        drawingCanvas.setPenActive(isPenActive);
        penButton.setStyle(isPenActive ? "-fx-background-color: lightblue;" : ""); // Change color when active

        if (isPenActive) {
            isSelectActive = false; // Deactivate select if pen is active
            drawingCanvas.setSelectActive(false);
            selectButton.setStyle(""); // Reset select button style
        } else {
            drawingCanvas.setShape(null); // Reset shape if pen is inactive
        }
    }

    private void toggleSelect(DrawingCanvas drawingCanvas) {
        isSelectActive = !isSelectActive;
        drawingCanvas.setSelectActive(isSelectActive);
        selectButton.setStyle(isSelectActive ? "-fx-background-color: lightblue;" : ""); // Change color when active

        if (isSelectActive) {
            isPenActive = false; // Deactivate pen if select is active
            drawingCanvas.setPenActive(false);
            penButton.setStyle(""); // Reset pen button style
        }
    }

    private void createShapeButtons(DrawingCanvas drawingCanvas) {
        Button squareButton = new Button("Square");
        Button circleButton = new Button("Circle");
        Button rectangleButton = new Button("Rectangle");
        Button ellipseButton = new Button("Ellipse");
        Button triangleButton = new Button("Triangle");
        Button octagonButton = new Button("Octagon");

        squareButton.setOnAction(e -> setShapeAndToggle("square", drawingCanvas));
        circleButton.setOnAction(e -> setShapeAndToggle("circle", drawingCanvas));
        rectangleButton.setOnAction(e -> setShapeAndToggle("rectangle", drawingCanvas));
        ellipseButton.setOnAction(e -> setShapeAndToggle("ellipse", drawingCanvas));
        triangleButton.setOnAction(e -> setShapeAndToggle("triangle", drawingCanvas));
        octagonButton.setOnAction(e -> setShapeAndToggle("octagon", drawingCanvas));

        shapeButtonsBox.getChildren().addAll(squareButton, circleButton, rectangleButton, ellipseButton, triangleButton, octagonButton);
    }

    /**
     * Opens an image file and sets it as the current canvas image.
     *
     * @param drawingCanvas The drawing canvas to set the image on.
     * @param primaryStage The primary stage of the application.
     */
    private void openImage(DrawingCanvas drawingCanvas, Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            mainApp.setOriginalFile(file); // Set original file in main app
            drawingCanvas.setImage(new javafx.scene.image.Image(file.toURI().toString()));
        }
    }

    private void clearCanvas(DrawingCanvas drawingCanvas) {
        drawingCanvas.clear(); // This will call the clear method in DrawingCanvas
    }

    /**
     * Saves the current drawing canvas image to a file.
     *
     * @param drawingCanvas The drawing canvas to save.
     */
    private void saveImage(DrawingCanvas drawingCanvas) {
        if (mainApp.getOriginalFile() != null) {
            drawingCanvas.saveImage(mainApp.getOriginalFile()); // Ensure getOriginalFile() is public in openImage
        }
    }

    private void saveImageAs(DrawingCanvas drawingCanvas, Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        // Add multiple file format options
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPG Files", "*.jpg"),
                new FileChooser.ExtensionFilter("GIF Files", "*.gif")
        );
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            // Check for potential data loss
            boolean shouldProceed = showDataLossWarning(file);
            if (shouldProceed) {
                drawingCanvas.saveImage(file); // Save to the chosen file
                mainApp.setOriginalFile(file); // Update original file reference
            }
        }
    }

    private boolean showDataLossWarning(File file) {
        String originalExtension = (mainApp.getOriginalFile() != null) ? getFileExtension(mainApp.getOriginalFile()) : "";
        String newExtension = getFileExtension(file);

        if (!originalExtension.equalsIgnoreCase(newExtension)) {
            // Show a warning dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("You are saving the image in a different format (" + newExtension + ") than the original format (" + originalExtension + "). This may result in data loss. Do you want to proceed?");

            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.YES;
        }
        return true; // No warning needed, proceed with saving
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot > 0 && lastIndexOfDot < fileName.length() - 1) {
            return fileName.substring(lastIndexOfDot + 1);
        }
        return ""; // No extension found
    }
    private void toggleImageOptions() {
        boolean isVisible = lineOptionsHBox.isVisible();
        lineOptionsHBox.setVisible(!isVisible);
    }

    private void toggleLineOptions() {
        boolean isVisible = lineOptionsHBox.isVisible();
        lineOptionsHBox.setVisible(!isVisible);
    }

    private void toggleShapeButtons() {
        shapeButtonsBox.setVisible(!shapeButtonsBox.isVisible());
    }

    private void setShapeAndToggle(String shape, DrawingCanvas drawingCanvas) {
        currentShape = shape;
        drawingCanvas.setShape(currentShape);
        shapeButtonsBox.setVisible(false);
        isPenActive = false; // Deactivate pen when shape is selected
        drawingCanvas.setPenActive(false);
        penButton.setStyle(""); // Reset pen button style
    }

    private void showHelpDialog() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText(null);
        alert.setContentText("1. Press Image Options to view image-related settings.\n2. Use Insert Shapes to add shapes to your image.\n3. Adjust line width and color as needed.");
        alert.showAndWait();
    }

    public HBox getButtons() {
        return buttonBox;
    }
}