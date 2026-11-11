
package com.example.bookrentalsystem.controller;

import com.example.bookrentalsystem.entity.BookCopy;
import com.example.bookrentalsystem.service.BookCopyService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/book-copies")
@CrossOrigin(
        origins = "http://localhost:4200",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        },
        allowedHeaders = "*"
)
public class BookCopyController {

    private final BookCopyService bookCopyService;

    public BookCopyController(BookCopyService bookCopyService) {
        this.bookCopyService = bookCopyService;
    }

    @GetMapping
    public List<BookCopy> getAllBookCopies() {
        return bookCopyService.getAllBookCopies();
    }

    @GetMapping("/{id}")
    public BookCopy getBookCopyById(@PathVariable Long id) {
        return bookCopyService.getBookCopyById(id);
    }

    // Existing single-copy endpoint.
    @PostMapping
    public BookCopy createBookCopy(@RequestBody BookCopy bookCopy) {
        return bookCopyService.createBookCopy(bookCopy);
    }

    // New endpoint for registering multiple copies.
    @PostMapping("/bulk")
    public List<BookCopy> createBookCopiesBulk(
            @RequestBody BulkBookCopyRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body is required."
            );
        }

        return bookCopyService.createBookCopiesBulk(
                request.getBookId(),
                request.getQuantity(),
                request.getCondition()
        );
    }

    @PutMapping("/{id}")
    public BookCopy updateBookCopy(
            @PathVariable Long id,
            @RequestBody BookCopy bookCopy) {
        return bookCopyService.updateBookCopy(id, bookCopy);
    }

    @DeleteMapping("/{id}")
    public void deleteBookCopy(@PathVariable Long id) {
        bookCopyService.deleteBookCopy(id);
    }

    // Request body for bulk registration.
    public static class BulkBookCopyRequest {

        private Long bookId;
        private int quantity;
        private String condition;

        public Long getBookId() {
            return bookId;
        }

        public void setBookId(Long bookId) {
            this.bookId = bookId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }
    }
}
