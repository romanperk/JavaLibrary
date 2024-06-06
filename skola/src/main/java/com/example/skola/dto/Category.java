package com.example.skola.dto;

import java.util.UUID;

public class Category {
    private final UUID id;
    private String name;

    // Default constructor
    public Category() {
        this.id = UUID.randomUUID();
    }

    // Constructor with name parameter
    public Category(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
