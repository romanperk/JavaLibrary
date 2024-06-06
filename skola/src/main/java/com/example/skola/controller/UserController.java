package com.example.skola.controller;

import java.util.List;
import com.example.skola.dto.Library;
import com.example.skola.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    private final Library library;

    @Autowired
    public UserController(Library library) {
        this.library = library;
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createUser(@RequestBody User user) {
        // Check if filled in data is complete
        if (user == null || user.getName() == null || user.getEmail() == null) {
            return new ResponseEntity<>("User data is incomplete", HttpStatus.BAD_REQUEST);
        }

        // Check if user already exists
        for (User existingUser : library.getUsers()) {
            if (existingUser.getEmail().equalsIgnoreCase(user.getEmail())) {
                return new ResponseEntity<>("User with the same email already exists", HttpStatus.BAD_REQUEST);
            }
        }

        library.addUser(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/get/{email}")
    public ResponseEntity<Object> getUserByEmail(@PathVariable String email) {
        User user = library.getUserByEmail(email);

        // Check if user with given email exists
        if (user == null) {
            return new ResponseEntity<>("User with email " + email + " not found", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<Object> getUsers() {
        List<User> users = library.getUsers();

        // Check if there are any users in the database
        if (users.isEmpty()) {
            return new ResponseEntity<>("No users in the database", HttpStatus.OK);
        }

        return new ResponseEntity<>(users, HttpStatus.OK);
    }


    @PutMapping("/update/{email}")
    public ResponseEntity<String> updateUser(@PathVariable String email, @RequestBody User updatedUser) {
        User user = library.getUserByEmail(email);

        // Check if user with given email exists
        if (user == null) {
            return new ResponseEntity<>("User with email " + email + " not found", HttpStatus.NOT_FOUND);
        }

        // Check if edits are filled in
        if (updatedUser.getName() == null && updatedUser.getEmail() == null) {
            return new ResponseEntity<>("No changes to be made", HttpStatus.BAD_REQUEST);
        }

        // Update user's name if provided
        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }

        // Update user's email if provided and not already taken
        if (updatedUser.getEmail() != null) {
            // Check if the new email already exists
            User existingUserWithEmail = library.getUserByEmail(updatedUser.getEmail());
            if (existingUserWithEmail != null && !existingUserWithEmail.getId().equals(user.getId())) {
                return new ResponseEntity<>("Email " + updatedUser.getEmail() + " is already in use", HttpStatus.BAD_REQUEST);
            }
            user.setEmail(updatedUser.getEmail());
        }

        return new ResponseEntity<>("User with email " + email + " updated", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email) {
        boolean deleted = library.deleteUser(email);

        // Check if user with given email exists
        if (!deleted) {
            return new ResponseEntity<>("User with email " + email + " not found", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>("User with email " + email + " deleted", HttpStatus.OK);
    }
}
