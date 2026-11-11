
package com.example.bookrentalsystem.controller;

import com.example.bookrentalsystem.entity.BookCopyInspection;
import com.example.bookrentalsystem.service.BookCopyInspectionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/book-copy-inspections")
@CrossOrigin(origins = "http://localhost:4200")
public class BookCopyInspectionController {

    private final BookCopyInspectionService inspectionService;

    public BookCopyInspectionController(
            BookCopyInspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    // Record an inspection of a book copy.
    @PostMapping
    public ResponseEntity<BookCopyInspection> recordInspection(
            @RequestBody InspectionRequest request) {

        if (request.copyId == null
                || request.inspectedByUserId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Book copy ID and Staff user ID are required."
            );
        }

        BookCopyInspection inspection =
                inspectionService.recordInspection(
                        request.copyId,
                        request.observedCondition,
                        request.notes,
                        request.inspectedByUserId
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inspection);
    }

    // View inspection history for a particular book copy.
    @GetMapping("/copy/{copyId}")
    public ResponseEntity<List<BookCopyInspection>> getCopyHistory(
            @PathVariable Long copyId) {

        return ResponseEntity.ok(
                inspectionService.getCopyHistory(copyId)
        );
    }

    // View inspection history for a Staff user.
    @GetMapping("/staff/{userId}")
    public ResponseEntity<List<BookCopyInspection>> getStaffHistory(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                inspectionService.getStaffHistory(userId)
        );
    }

    public static class InspectionRequest {
        public Long copyId;
        public String observedCondition;
        public String notes;
        public Long inspectedByUserId;
    }
}
