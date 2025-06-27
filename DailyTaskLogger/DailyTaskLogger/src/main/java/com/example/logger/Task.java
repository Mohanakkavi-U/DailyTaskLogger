package com.example.logger;

import java.time.LocalDate;
import java.time.LocalTime;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Task {
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> time = new SimpleObjectProperty<>();
    private final IntegerProperty serialNumber = new SimpleIntegerProperty();

    public Task(String description, LocalDate date, LocalTime time) {
        setDescription(description);
        setDate(date);
        setTime(time);
    }
    
    public int getSerialNumber() {
        return serialNumber.get();
    }
    
    public void setSerialNumber(int serialNumber) {
        this.serialNumber.set(serialNumber);
    }
    
    public IntegerProperty serialNumberProperty() {
        return serialNumber;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }

    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }
    
    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }
    
    public LocalTime getTime() {
        return time.get();
    }
    
    public void setTime(LocalTime time) {
        this.time.set(time);
    }
    
    public ObjectProperty<LocalTime> timeProperty() {
        return time;
    }

    @Override
    public String toString() {
        return String.format("%s (%s at %s)", 
            getDescription(), 
            getDate().toString(),
            getTime() != null ? getTime().toString() : "No time specified");
    }
}
