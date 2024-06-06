package com.example.skola.controller;

import com.example.skola.dto.Book;
import com.example.skola.dto.Category;
import com.example.skola.dto.Library;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/categories")
public class CategoryController {

    private final Library library;

    @Autowired
    public CategoryController(Library library) {
        this.library = library;
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createCategory(@RequestBody Category category) {
        if (category == null || category.getName() == null || category.getName().isEmpty()) {
            return new ResponseEntity<>("Category name is missing", HttpStatus.BAD_REQUEST);
        }

        // Check if any category with the same name already exists
        for (Category existingCategory : library.getCategories()) {
            if (existingCategory.getName().equalsIgnoreCase(category.getName())) {
                return new ResponseEntity<>("Category already exists", HttpStatus.BAD_REQUEST);
            }
        }

        library.addCategory(category);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllCategories() {
        List<Category> categories = library.getCategories();

        // Check if there are any categories in the database
        if (categories.isEmpty()) {
            return new ResponseEntity<>("No categories in the database", HttpStatus.OK);
        }

        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Object> getCategory(@PathVariable UUID id) {
        Category category = library.getCategory(id);

        if (category == null) {
            return new ResponseEntity<>("Category with ID " + id + " not found", HttpStatus.NOT_FOUND);
        }

        // Retrieve books belonging to the category from the library
        List<Book> booksInCategory = library.getBooksByCategory(category);

        // Extract only the ID, title, author and year of production of each book
        List<Map<String, Object>> simplifiedBooks = booksInCategory.stream().map(book -> {
            Map<String, Object> bookInfo = new HashMap<>();
            bookInfo.put("id", book.getId());
            bookInfo.put("title", book.getTitle());
            bookInfo.put("author", book.getAuthor());
            bookInfo.put("year", book.getYear());
            return bookInfo;
        }).collect(Collectors.toList());

        // Create a response object containing both category and its associated books
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("category", category);
        response.put("books", simplifiedBooks);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateCategory(@PathVariable UUID id, @RequestBody Category updateRequest) {
        Category categoryToUpdate = library.getCategory(id);

        if (categoryToUpdate == null) {
            return new ResponseEntity<>("Category with ID " + id + " not found", HttpStatus.NOT_FOUND);
        }

        // Check if there is a name typed in
        if (updateRequest == null || updateRequest.getName() == null || updateRequest.getName().isEmpty()) {
            return new ResponseEntity<>("Category name cannot be null or empty", HttpStatus.BAD_REQUEST);
        }

        categoryToUpdate.setName(updateRequest.getName());
        return new ResponseEntity<>("Category name changed to '" + updateRequest.getName() + "'", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable UUID id) {
        Category categoryToDelete = library.getCategory(id);

        if (categoryToDelete == null) {
            return new ResponseEntity<>("Category with ID " + id + " not found", HttpStatus.NOT_FOUND);
        }

        String deletedCategoryName = categoryToDelete.getName();
        boolean deleted = library.deleteCategory(id);
        if (!deleted) {
            return new ResponseEntity<>("Book with ID " + id + " not found", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>("Category '" + deletedCategoryName + "' deleted", HttpStatus.OK);
    }
}
