
package com.example.bookrentalsystem.controller;

import com.example.bookrentalsystem.entity.UserAccount;
import com.example.bookrentalsystem.service.UserAccountService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserAccountController {

    private final UserAccountService userAccountService;

    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping
    public List<UserAccount> getAllUsers() {
        return userAccountService.getAllUsers();
    }

    @GetMapping("/pending")
    public List<UserAccount> getPendingUsers() {
        return userAccountService.getPendingUsers();
    }

    @GetMapping("/{id}")
    public UserAccount getUserById(@PathVariable Long id) {
        return userAccountService.getUserById(id);
    }

    @PostMapping
    public UserAccount createUser(@RequestBody UserAccount userAccount) {
        return userAccountService.createUser(userAccount);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userAccountService.deleteUser(id);
    }

    @PutMapping("/{id}/approve")
    public UserAccount approveUser(@PathVariable Long id) {
        return userAccountService.updateStatus(id, "APPROVED");
    }

    @PutMapping("/{id}/reject")
    public UserAccount rejectUser(@PathVariable Long id) {
        return userAccountService.updateStatus(id, "REJECTED");
    }

    @PutMapping("/{id}/activation")
    public UserAccount updateActivation(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {

        Boolean active = request.get("active");

        if (active == null) {
            throw new IllegalArgumentException(
                    "Active status is required."
            );
        }

        return userAccountService.updateActiveStatus(id, active);
    }

    @PutMapping("/{id}/change-password")
    public Map<String, String> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        userAccountService.changePassword(
                id,
                currentPassword,
                newPassword
        );

        return Map.of(
                "message",
                "Password changed successfully."
        );
    }

    @PostMapping("/staff")
    public ResponseEntity<?> createStaffAccount(
            @RequestBody UserAccount userAccount) {

        UserAccount staff =
                userAccountService.createStaffAccount(userAccount);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Staff account created successfully.",
                        "user", staff,
                        "temporaryPassword", staff.getPassword()
                )
        );
    }
}
