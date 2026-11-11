package com.example.bookrentalsystem.controller;

import com.example.bookrentalsystem.entity.UserAccount;
import com.example.bookrentalsystem.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegistrationRequest request) {
        try {
            UserAccount user = authService.register(
                    request.firstName,
                    request.lastName,
                    request.username,
                    request.password,
                    request.email,
                    request.phone,
                    request.address
            );

            return ResponseEntity.ok(user);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {
        try {
            UserAccount user = authService.login(
                    request.username,
                    request.password
            );
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(
                    request.username,
                    request.email,
                    request.newPassword,
                    request.confirmPassword
            );
            return ResponseEntity.ok(
                    "Password reset successfully."
            );
        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
    public static class RegistrationRequest {
        public String firstName;
        public String lastName;
        public String username;
        public String password;
        public String email;
        public String phone;
        public String address;
    }
    public static class LoginRequest {

        public String username;
        public String password;
    }
    public static class ResetPasswordRequest {
        public String username;
        public String email;
        public String newPassword;
        public String confirmPassword;
    }

}