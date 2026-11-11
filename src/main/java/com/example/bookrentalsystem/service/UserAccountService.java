
package com.example.bookrentalsystem.service;

import com.example.bookrentalsystem.entity.UserAccount;
import com.example.bookrentalsystem.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.List;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public List<UserAccount> getAllUsers() {
        return userAccountRepository.findAll();
    }

    public UserAccount getUserById(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found."));
    }

    public UserAccount createUser(UserAccount userAccount) {
        return userAccountRepository.save(userAccount);
    }

    public void deleteUser(Long id) {
        UserAccount user = getUserById(id);
        protectAdmin(user);
        userAccountRepository.delete(user);
    }

    public List<UserAccount> getPendingUsers() {
        return userAccountRepository.findAll()
                .stream()
                .filter(user -> "PENDING".equals(user.getStatus()))
                .toList();
    }

    public UserAccount updateStatus(Long id, String status) {
        UserAccount user = getUserById(id);

        if (!List.of("APPROVED", "REJECTED").contains(status)) {
            throw new RuntimeException("Invalid registration status.");
        }

        protectAdmin(user);

        user.setStatus(status);
        return userAccountRepository.save(user);
    }

    public UserAccount updateActiveStatus(Long id, boolean active) {
        UserAccount user = getUserById(id);

        protectAdmin(user);

        if (!"APPROVED".equals(user.getStatus())) {
            throw new RuntimeException(
                    "Only approved users can be activated or deactivated."
            );
        }

        user.setActive(active);
        return userAccountRepository.save(user);
    }

    public UserAccount createStaffAccount(UserAccount userAccount) {

        // Check whether the username already exists
        if (userAccountRepository
                .findByUsername(userAccount.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists.");
        }

        // Check whether the email already exists
        if (userAccount.getEmail() != null &&
                userAccountRepository
                        .findByEmail(userAccount.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists.");
        }

        // Generate a temporary password
        String temporaryPassword = UUID.randomUUID()
                .toString()
                .substring(0, 10);

        // Configure the staff account
        userAccount.setRole("STAFF");
        userAccount.setStatus("APPROVED");
        userAccount.setActive(true);
        userAccount.setMustChangePassword(true);
        userAccount.setPassword(temporaryPassword);

        return userAccountRepository.save(userAccount);
    }

    private void protectAdmin(UserAccount user) {
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException(
                    "Admin accounts cannot be deactivated or deleted here."
            );
        }
    }

    public void changePassword(
            Long id,
            String currentPassword,
            String newPassword) {

        UserAccount user = getUserById(id);

        // Validate the current password
        if (currentPassword == null ||
                currentPassword.isEmpty() ||
                !user.getPassword().equals(currentPassword)) {
            throw new RuntimeException("Current password is incorrect.");
        }

        // Validate the new password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("New password cannot be empty.");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException(
                    "New password must be at least 6 characters long."
            );
        }

        // Save the new password
        user.setPassword(newPassword);

        // Allow the user to log in without changing it again
        user.setMustChangePassword(false);

        userAccountRepository.save(user);
    }
}
