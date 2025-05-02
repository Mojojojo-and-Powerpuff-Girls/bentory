package com.example.bentory_app.model;

public class StatsModel {
    private String title;
    private String description;
    private String numFigures;

    public StatsModel(String title, String description, String numFigures) {
        this.title = title;
        this.description = description;
        this.numFigures = numFigures;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getNumFigures() {
        return numFigures;
    }
}
