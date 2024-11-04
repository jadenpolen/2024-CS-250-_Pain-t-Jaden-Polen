package com.example.imageeditorjaden;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.File;

public class openImage extends Application {

    private DrawingCanvas drawingCanvas;
    private Canvas canvas;
    private HBox lineOptionsHBox;
    private HBox buttonBox;
    private ScheduledExecutorService scheduler;
    private Label timerLabel;
    private long timeRemaining = 60; // Time in seconds
    private boolean isTimerVisible = true; // Timer visibility
    private File originalFile;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Opener");

        // Create a Canvas for drawing
        canvas = new Canvas(); // Initialize without size
        drawingCanvas = new DrawingCanvas(canvas);

        // Set up the size of the canvas to fill the window
        canvas.widthProperty().bind(primaryStage.widthProperty());
        canvas.heightProperty().bind(primaryStage.heightProperty().subtract(150)); // Adjust for button and timer height

        // Create a Slider to adjust the line width
        Slider lineWidthSlider = new Slider(1, 20, 2);
        lineWidthSlider.setShowTickMarks(true);
        lineWidthSlider.setShowTickLabels(true);
        lineWidthSlider.setMajorTickUnit(1);
        lineWidthSlider.setMinorTickCount(1);
        lineWidthSlider.setBlockIncrement(1);
        lineWidthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            drawingCanvas.setLineWidth(newVal.doubleValue());
        });

        // Create a Label for the line width slider
        Label lineWidthLabel = new Label("Line Width");

        // Create a ColorPicker to choose the line color
        ColorPicker colorPicker = new ColorPicker(Color.BLACK);
        colorPicker.setOnAction(e -> drawingCanvas.setLineColor(colorPicker.getValue()));

        // Grouping line label, slider, and color picker into an HBox
        lineOptionsHBox = new HBox(10);
        lineOptionsHBox.getChildren().addAll(lineWidthLabel, lineWidthSlider, colorPicker);
        lineOptionsHBox.setVisible(false); // Hidden initially

        // Create the buttonBox for image-related buttons
        buttonBox = new HBox(10);

        // Initialize Buttons with buttonClass
        buttonClass buttons = new buttonClass(drawingCanvas, primaryStage, lineOptionsHBox, buttonBox, this);

        // Create VBox to hold all components
        VBox vbox = new VBox(10);

        // Timer Label
        timerLabel = new Label("Autosave in: " + timeRemaining + "s");
        timerLabel.setVisible(isTimerVisible); // Set visibility
        vbox.getChildren().addAll(timerLabel, buttons.getButtons(), lineOptionsHBox, canvas); // Added timerLabel above buttons

        // Create and start the timer
        startTimer();

        // Create and set the scene
        Scene scene = new Scene(vbox, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Starts the timer that counts down and triggers an autosave when it reaches zero.
     */
    private void startTimer() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduleTimerTask(); // Schedule the timer task
    }

    /**
     * Schedules the timer task to update the countdown and perform autosave.
     */
    private void scheduleTimerTask() {
        scheduler.scheduleAtFixedRate(() -> {
            if (timeRemaining > 0) {
                timeRemaining--;
                updateTimerLabel();
            } else {
                System.out.println("Timer reached zero, attempting to autosave.");
                // Use Platform.runLater to execute saveImage on the JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    saveImage(); // Call your save function here
                    resetTimer(); // Reset timer after saving
                });
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Updates the timer label to show the remaining time.
     */
    private void updateTimerLabel() {
        javafx.application.Platform.runLater(() -> timerLabel.setText("Autosave in: " + timeRemaining + "s"));
    }

    /**
     * Resets the timer to the initial time.
     */
    public void resetTimer() {
        timeRemaining = 60; // Reset to 60 seconds
        updateTimerLabel();
    }

    /**
     * Saves the current drawing to the original file if it exists.
     */
    public void saveImage() {
        if (originalFile != null) {
            try {
                // Call the save method of the DrawingCanvas
                drawingCanvas.saveImage(originalFile);
                notifyUser("Image has been autosaved!"); // Notify that it was saved
            } catch (Exception e) {
                e.printStackTrace();
                notifyUser("Failed to autosave image: " + e.getMessage());
            }
        } else {
            notifyUser("No original file to save! Please open an image first.");
        }
    }

    /**
     * Displays a notification to the user.
     *
     * @param message The message to display in the alert.
     */
    private void notifyUser(String message) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Autosave");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Sets the original file that was opened.
     *
     * @param file The File object representing the original image file.
     */
    public void setOriginalFile(File file) {
        this.originalFile = file;
    }

    /**
     * Retrieves the current original file.
     *
     * @return The File object representing the original image file.
     */
    public File getOriginalFile() {
        return originalFile; // Retrieve the current original file
    }

    /**
     * Toggles the visibility of the timer label.
     */
    public void setTimerVisibility() {
        isTimerVisible = !isTimerVisible; // Toggle the visibility state
        timerLabel.setVisible(isTimerVisible); // Update label visibility
    }

    /**
     * The main entry point for the JavaFX application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
