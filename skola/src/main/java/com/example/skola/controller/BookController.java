package com.example.skola.controller;

import com.example.skola.dto.Category;
import com.example.skola.dto.Library;
import com.example.skola.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.skola.dto.Book;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/books")
public class BookController {

    private final Library library;

    @Autowired
    public BookController(Library library) {
        this.library = library;
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createBook(@RequestBody Book book) {

        // Check if typed in data is complete
        if (book == null || book.getTitle() == null || book.getAuthor() == null || book.getYear() == null || book.getPages() == null) {
            return new ResponseEntity<>("Book data is incomplete", HttpStatus.BAD_REQUEST);
        }

        // Check if a book with the same title, author and year of production already exists
        for (Book existingBook : library.getBooks()) {
            if (existingBook.getTitle().equalsIgnoreCase(book.getTitle()) && existingBook.getAuthor().equalsIgnoreCase(book.getAuthor()) && existingBook.getYear().equals(book.getYear())) {
                return new ResponseEntity<>("Book with the same title, author and year of production already exists", HttpStatus.BAD_REQUEST);
            }
        }

        // Check if the book has categories
        List<Category> bookCategories = book.getCategories();
        if (bookCategories == null || bookCategories.isEmpty()) {
            return new ResponseEntity<>("Book categories are missing", HttpStatus.BAD_REQUEST);
        }

        // Check for every category in database
        for (int i = 0; i < bookCategories.size(); i++) {
            Category bookCategory = bookCategories.get(i);
            // Check if the category already exists
            Category existingCategory = library.getCategoryByName(bookCategory.getName());
            if (existingCategory != null) {
                // If the category exists, assign it to the book
                bookCategories.set(i, existingCategory);
            } else {
                // If the category doesn't exist, create a new one and add it to the library
                library.addCategory(bookCategory);
            }
        }

        // If the quantity is set to a negative number, return an error message
        if (book.getQuantity() < 0) {
            return new ResponseEntity<>("Quantity can't be negative", HttpStatus.BAD_REQUEST);
        }

        // If the quantity is not filled in, set it to one
        if (book.getQuantity() == null) {
            book.setQuantity(1);
        }

        // If the reserved status is filled in, return an error mesage
        if (book.isReserved()) {
            return new ResponseEntity<>("Cannot set the reservation status, use the reservation system instead", HttpStatus.BAD_REQUEST);
        }

        // If the user is filled in, return an error message
        if (book.getUser() != null ) {
            return new ResponseEntity<>("Cannot set the user, use the reservation system instead", HttpStatus.BAD_REQUEST);
        }

        library.addBook(book);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Object> getBook(@PathVariable UUID id) {
        Book book = library.getBook(id);

        // Check if the book with given ID exists
        if (book == null) {
            return new ResponseEntity<>("Book with ID " + id + " not found", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<Object> getBooks() {
        List<Book> books = library.getBooks();

        // Check if there are any books in the database
        if (books.isEmpty()) {
            return new ResponseEntity<>("No books in the database", HttpStatus.OK);
        }

        // Extract only the ID, title, author and year of production of each book
        List<Map<String, Object>> simplifiedBooks = books.stream().map(book -> {
            Map<String, Object> bookInfo = new HashMap<>();
            bookInfo.put("id", book.getId());
            bookInfo.put("title", book.getTitle());
            bookInfo.put("year", book.getYear());
            bookInfo.put("author", book.getAuthor());
            return bookInfo;
        }).collect(Collectors.toList());

        return new ResponseEntity<>(simplifiedBooks, HttpStatus.OK);
    }

    @GetMapping("/isReserved/{id}")
    public ResponseEntity<Object> isReserved(@PathVariable UUID id) {
        Book book = library.getBook(id);

        // Check if the book with given ID exists
        if (book == null) {
            return new ResponseEntity<>("Book with ID " + id + " not found", HttpStatus.NOT_FOUND);
        }

        String bookTitle = book.getTitle();

        // Check if the book is reserved
        if (book.isReserved()) {
            String message = "Book '" + bookTitle + "' is reserved by user with email " + book.getUser().getEmail();
            return new ResponseEntity<>(message, HttpStatus.OK);
        } else {
            String message = "Book '" + bookTitle + "' is not reserved";
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
    }

    @PutMapping("/lend/{bookId}")
    public ResponseEntity<String> lendBook(@PathVariable UUID bookId, @RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");

        Book book = library.getBook(bookId);

        if (book == null) {
            return new ResponseEntity<>("Book with ID " + bookId + " not found", HttpStatus.NOT_FOUND);
        }

        User user = library.getUserByEmail(email);

        // Check if user with given email exists
        if (user == null) {
            return new ResponseEntity<>("User with email '" + email + "' not found", HttpStatus.NOT_FOUND);
        }

        if (book.isReserved()) {
            return new ResponseEntity<>("Book '" + book.getTitle() + "' is already reserved by user with email " + book.getUser().getEmail(), HttpStatus.BAD_REQUEST);
        }

        if (!library.lendBookToUser(bookId, email)) {
            return new ResponseEntity<>("Unable to lend book '" + book.getTitle() + "' to user with email " + email, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Book '" + book.getTitle() + "' lent to user with email " + email, HttpStatus.OK);
    }

    @PutMapping("/return/{id}")
    public ResponseEntity<String> returnBook(@PathVariable UUID id) {
        Book book = library.getBook(id);

        if (book == null) {
            return new ResponseEntity<>("Book with ID " + id + " not found", HttpStatus.NOT_FOUND);
        }

        // Check if book is really lent to someone
        if (!book.isReserved()) {
            return new ResponseEntity<>("Book is not lent to anyone", HttpStatus.BAD_REQUEST);
        }

        // Change the reserved status and remove corresponding user
        book.setReserved(false);
        book.setUser(null);
        return new ResponseEntity<>("Book '" + book.getTitle() + "' returned", HttpStatus.OK);
    }

    @PutMapping("/update/{bookId}")
    public ResponseEntity<String> updateBook(@PathVariable String bookId, @RequestBody Book updatedBook) {
        Book book = library.getBook(UUID.fromString(bookId));

        if (book == null) {
            return new ResponseEntity<>("Book with ID " + bookId + " not found", HttpStatus.NOT_FOUND);
        }

        // Update book's title if provided
        if (updatedBook.getTitle() != null) {
            book.setTitle(updatedBook.getTitle());
        }

        // Update book's author if provided
        if (updatedBook.getAuthor() != null) {
            book.setAuthor(updatedBook.getAuthor());
        }

        // Update book's pages if provided
        if (updatedBook.getPages() != null) {
            book.setPages(updatedBook.getPages());
        }

        // Update book's year of production if provided
        if (updatedBook.getYear() != null) {
            book.setYear(updatedBook.getYear());
        }

        // If the categories are typed in, show an error message
        if (updatedBook.getCategories() != null) {
            return new ResponseEntity<>("Cannot change the categories, use the category setter instead", HttpStatus.BAD_REQUEST);
        }

        // If the quantity is set to a negative number, return an error message
        if (updatedBook.getQuantity() != null) {
            if (updatedBook.getQuantity() < 0) {
                return new ResponseEntity<>("Quantity can't be negative", HttpStatus.BAD_REQUEST);
            }
            book.setQuantity(updatedBook.getQuantity());
        }

        // Check if reserved status is being changed
        if (updatedBook.isReserved() != null && !updatedBook.isReserved().equals(book.isReserved())) {
            return new ResponseEntity<>("Cannot change the reservation status or the book is already reserved (check the reservation status)", HttpStatus.BAD_REQUEST);
        }

        // Check if user is being changed
        if (updatedBook.getUser() != null) {
            return new ResponseEntity<>("Cannot update the user, use the reservation system instead", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("Book '" + book.getTitle() + "' updated", HttpStatus.OK);
    }

    @PutMapping("/assignCategory/{bookId}")
    public ResponseEntity<String> assignBookToCategory(@PathVariable UUID bookId, @RequestBody List<UUID> categoryIds) {
        Book book = library.getBook(bookId);

        if (book == null) {
            return new ResponseEntity<>("Book with ID " + bookId + " not found", HttpStatus.NOT_FOUND);
        }


        List<Category> categories = new ArrayList<>();

        // Get categories by ID
        for (UUID categoryId : categoryIds) {
            Category category = library.getCategory(categoryId);
            if (category != null) {
                categories.add(category);
            }
        }

        // Check if categories exist
        if (categories.isEmpty()) {
            return new ResponseEntity<>("No valid categories found", HttpStatus.BAD_REQUEST);
        }

        // Append the new categories to the existing list of categories for the book
        List<Category> existingCategories = book.getCategories();
        existingCategories.addAll(categories);
        book.setCategories(existingCategories);

        return new ResponseEntity<>("Book  '" + book.getTitle() + "' assigned to given categories", HttpStatus.OK);
    }

    @PutMapping("/removeFromCategory/{bookId}")
    public ResponseEntity<String> removeBookFromCategories(@PathVariable UUID bookId, @RequestBody List<UUID> categoryIds) {
        Book book = library.getBook(bookId);

        if (book == null) {
            return new ResponseEntity<>("Book with ID " + bookId + " not found", HttpStatus.NOT_FOUND);
        }

        // Get the list of categories for the book
        List<Category> categories = book.getCategories();

        boolean removedFromAnyCategory = false;

        // Remove the book from categories
        for (UUID categoryId : categoryIds) {
            Category category = library.getCategory(categoryId);
            if (category == null) {
                return new ResponseEntity<>("Category with ID " + categoryId + " not found", HttpStatus.NOT_FOUND);
            }
            if (categories.contains(category)) {
                categories.remove(category);
                removedFromAnyCategory = true;
            }
        }

        if (!removedFromAnyCategory) {
            return new ResponseEntity<>("Book '" + book.getTitle() + "' is not associated with any of the specified categories", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("Book '" + book.getTitle() + "' removed from specified categories", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable UUID id) {
        Book bookToDelete = library.getBook(id);

        if (bookToDelete == null) {
            return new ResponseEntity<>("Book with ID " + id + " not found", HttpStatus.NOT_FOUND);
        }

        String deletedBookName = bookToDelete.getTitle();
        boolean deleted = library.deleteBook(id);

        if (!deleted) {
            return new ResponseEntity<>("Book with ID " + id + " not found", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>("Book '" + deletedBookName + "' deleted", HttpStatus.OK);
    }
}
