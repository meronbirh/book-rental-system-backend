
package com.example.bookrentalsystem.controller;

import com.example.bookrentalsystem.entity.Book;
import com.example.bookrentalsystem.service.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@CrossOrigin(
        origins = "http://localhost:4200",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // Get all books.
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    // Get a book by ID.
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    // Create a book.
    @PostMapping
    public Book createBook(@RequestBody Book book) {
        return bookService.createBook(book);
    }

    // Update a book.
    @PutMapping("/{id}")
    public Book updateBook(
            @PathVariable Long id,
            @RequestBody Book book) {
        return bookService.updateBook(id, book);
    }

    // Delete a book.
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }

    // Get up to five featured books.
    @GetMapping("/featured")
    public List<Book> getFeaturedBooks() {
        return bookService.getFeaturedBooks();
    }

    // Get the five most popular books.
    @GetMapping("/popular")
    public List<Book> getPopularBooks() {
        return bookService.getPopularBooks();
    }

    // Get the five newest catalogue additions.
    @GetMapping("/new-additions")
    public List<Book> getNewAdditions() {
        return bookService.getNewAdditions();
    }
}
