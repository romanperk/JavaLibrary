package com.example.skola.dto;

import java.util.List;
import java.util.UUID;

public class Book {
    private final UUID id;
    private String title;
    private String author;
    private Integer pages;
    private Integer year;
    private Integer quantity;
    private Boolean isReserved;
    private List<Category> categories;
    private User user;

    public Book(String title, boolean isReserved) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.isReserved = isReserved;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Boolean isReserved() {
        return isReserved;
    }

    public void setReserved(boolean reservation) {
        isReserved = reservation;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
