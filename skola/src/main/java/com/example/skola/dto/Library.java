package com.example.skola.dto;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class Library {
    private final List<User> users;
    private final List<Book> books;
    private final List<Category> categories;

    public Library() {
        this.users = new ArrayList<>();
        this.books = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    // Users
    public void addUser(User user) {
        users.add(user);
    }

    public User getUserByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    public List<User> getUsers() {
        return users;
    }

    public boolean deleteUser(String email) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getEmail().equals(email)) {
                users.remove(i);
                return true;
            }
        }
        return false;
    }

    // Books
    public void addBook(Book book) {
        books.add(book);
    }

    public Book getBook(UUID id) {
        for (Book book : books) {
            if (book.getId().equals(id)) {
                return book;
            }
        }
        return null;
    }

    public List<Book> getBooks() {
        return books;
    }

    public boolean deleteBook(UUID id) {
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            if (book.getId().equals(id)) {
                books.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean lendBookToUser(UUID bookId, String email) {
        Book book = getBook(bookId);
        User user = getUserByEmail(email);
        if (book == null || user == null) {
            return false;
        }
        book.setReserved(true);
        book.setUser(user);
        return true;
    }

    public List<Book> getBooksByCategory(Category category) {
        List<Book> categoryBooks = new ArrayList<>();
        for (Book book : books) {
            for (Category bookCategory : book.getCategories()) {
                if (bookCategory.equals(category)) {
                    categoryBooks.add(book);
                    break;
                }
            }
        }
        return categoryBooks;
    }


    // Categories
    public void addCategory(Category category) {
        categories.add(category);
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Category getCategory(UUID id) {
        for (Category category : categories) {
            if (category.getId().equals(id)) {
                return category;
            }
        }
        return null;
    }

    public Category getCategoryByName(String name) {
        for (Category category : categories) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }

    public boolean deleteCategory(UUID id) {
        Category categoryToDelete = getCategory(id);
        if (categoryToDelete == null) {
            return false;
        }
        for (Book book : books) {
            book.getCategories().remove(categoryToDelete);
        }
        categories.remove(categoryToDelete);
        return true;
    }
}
