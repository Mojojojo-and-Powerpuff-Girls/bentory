package com.example.bentory_app.model;

import com.google.firebase.database.PropertyName;

public class StatsModel {
    private String title;
    private String description;
    private String numFigures;

    public StatsModel(String title, String description, String numFigures) {
        this.title = title;
        this.description = description;
        this.numFigures = numFigures;
    }

    // Getters
    @PropertyName("title")
    public String getTitle() {
        return title;
    }
    @PropertyName("description")
    public String getDescription() {
        return description;
    }
    @PropertyName("numFigures")
    public String getNumFigures() {
        return numFigures;
    }
}
