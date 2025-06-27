package com.example.logger;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.scene.control.SpinnerValueFactory;

public class TaskController {

    @FXML
    private TextField taskField;

    @FXML
    private DatePicker taskDate;
    
    @FXML
    private Spinner<Integer> hourSpinner;
    
    @FXML
    private Spinner<Integer> minuteSpinner;
    
    @FXML
    private ChoiceBox<String> amPmChoice;

    @FXML
    private TableView<Task> taskTable;

    @FXML
    private TableColumn<Task, String> descColumn;

    @FXML
    private TableColumn<Task, Integer> serialColumn;
    
    @FXML
    private TableColumn<Task, LocalDate> dateColumn;
    
    @FXML
    private Label taskCountLabel;
    
    private ObservableList<Task> tasks = FXCollections.observableArrayList();
    
    private void updateTaskCount() {
        int count = tasks.size();
        taskCountLabel.setText(String.format("Total Tasks: %d", count));
    }
    
    private void updateSerialNumbers() {
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setSerialNumber(i + 1);
        }
    }
    
    @FXML
    public void initialize() {
        // Initialize the time spinners for 12-hour format
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 12));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        
        // Initialize AM/PM choice box
        amPmChoice.getItems().addAll("AM", "PM");
        amPmChoice.setValue("PM");
        
        // Format spinners
        hourSpinner.setEditable(true);
        minuteSpinner.setEditable(true);
        
        // Initialize the table columns
        descColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        
        // For the date column, we'll use a custom cell factory to display both date and time
        dateColumn.setCellFactory(column -> new TableCell<Task, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Task task = getTableRow().getItem();
                    if (task != null) {
                        String dateTime = task.getDate().toString();
                        if (task.getTime() != null) {
                            int hour = task.getTime().getHour();
                            int minute = task.getTime().getMinute();
                            String amPm = "AM";
                            int displayHour = hour;
                            
                            if (hour >= 12) {
                                amPm = "PM";
                                if (hour > 12) {
                                    displayHour = hour - 12;
                                }
                            } else if (hour == 0) {
                                displayHour = 12;
                            }
                            
                            dateTime += String.format(" %d:%02d %s", displayHour, minute, amPm);
                        }
                        setText(dateTime);
                    }
                }
            }
        });
        
        // Still need to set the cell value factory to get the date
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        
        // Set up the serial number column
        serialColumn.setCellValueFactory(cellData -> cellData.getValue().serialNumberProperty().asObject());
        
        // Add data to the table and update count when it changes
        taskTable.setItems(tasks);
        tasks.addListener((ListChangeListener<Task>) c -> {
            updateTaskCount();
            updateSerialNumbers();
            c.next();
        });
        updateTaskCount();
        updateSerialNumbers();
    }

    @FXML
    protected void handleAddTask() {
        String taskText = taskField.getText().trim();
        LocalDate date = taskDate.getValue();
        
        if (taskText.isEmpty() || date == null) {
            showAlert("Error", "Please enter both task description and date");
            return;
        }
        
        // Get time from spinners and AM/PM
        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        String amPm = amPmChoice.getValue();
        
        // Convert 12-hour to 24-hour format
        if (amPm.equals("PM") && hour < 12) {
            hour += 12;
        } else if (amPm.equals("AM") && hour == 12) {
            hour = 0;
        }
        
        LocalTime time = LocalTime.of(hour, minute);
        
        Task newTask = new Task(taskText, date, time);
        tasks.add(newTask);
        // No need to call updateTaskCount() or updateSerialNumbers() here as the listener will handle it
        
        // Clear the input fields
        taskField.clear();
        taskDate.setValue(null);
        hourSpinner.getValueFactory().setValue(12);
        minuteSpinner.getValueFactory().setValue(0);
        amPmChoice.setValue("PM");
        
        // Set focus back to the task field for quick entry of next task
        taskField.requestFocus();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void handleDeleteTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        
        if (selectedTask == null) {
            showAlert("No Selection", "Please select a task to delete.");
            return;
        }
        
        // Ask for confirmation before deleting
        Alert confirmAlert = new Alert(
            Alert.AlertType.CONFIRMATION,
            String.format("Are you sure you want to delete the task: %s?", selectedTask.getDescription()),
            ButtonType.YES, ButtonType.NO
        );
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText(null);
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                tasks.remove(selectedTask);
                // No need to call updateSerialNumbers() or updateTaskCount() as the listener will handle it
            }
        });
    }

    @FXML
    protected void handleExportTXT() {
        if (tasks.isEmpty()) {
            showAlert("No Tasks", "There are no tasks to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Task List");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.setInitialFileName("tasks_" + LocalDate.now() + ".txt");
        
        File file = fileChooser.showSaveDialog(taskTable.getScene().getWindow());
        
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Write header
                writer.println("Task List - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                writer.println("-".repeat(50));
                
                // Write tasks
                for (Task task : tasks) {
                    writer.printf("%d. %s%n", 
                        tasks.indexOf(task) + 1,
                        task.getDescription());
                    writer.printf("   Date: %s%n", 
                        task.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                    writer.printf("   Time: %s%n%n",
                        task.getTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                }
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("Tasks have been exported successfully to:\n" + file.getAbsolutePath());
                alert.showAndWait();
                
            } catch (IOException e) {
                showAlert("Export Error", "An error occurred while exporting tasks: " + e.getMessage());
            }
        }
    }
}
