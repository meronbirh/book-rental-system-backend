
package com.example.bookrentalsystem.service;

import com.example.bookrentalsystem.entity.Book;
import com.example.bookrentalsystem.entity.BookCopy;
import com.example.bookrentalsystem.repository.BookRepository;
import com.example.bookrentalsystem.repository.BookCopyRepository;
import com.example.bookrentalsystem.repository.RentalRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final RentalRepository rentalRepository;
    private final BookCopyRepository bookCopyRepository;

    private static final int BOOK_SECTION_SIZE = 5;

    public BookService(
            BookRepository bookRepository,
            RentalRepository rentalRepository,
            BookCopyRepository bookCopyRepository) {

        this.bookRepository = bookRepository;
        this.rentalRepository = rentalRepository;
        this.bookCopyRepository = bookCopyRepository;
    }

    public List<Book> getAllBooks() {

        List<Book> books = bookRepository.findAll();

        // Fill in missing cover URLs for existing books.
        for (Book book : books) {
            if (book.getCoverImageUrl() == null
                    || book.getCoverImageUrl().isBlank()) {

                String isbn = book.getIsbn();

                if (isbn != null && !isbn.isBlank()) {
                    book.setCoverImageUrl(buildCoverImageUrl(isbn));
                }
            }
        }

        return books;
    }

    public Book getBookById(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book not found."
                ));

        // Supply a cover URL if this book does not have one.
        if ((book.getCoverImageUrl() == null
                || book.getCoverImageUrl().isBlank())
                && book.getIsbn() != null
                && !book.getIsbn().isBlank()) {

            book.setCoverImageUrl(
                    buildCoverImageUrl(book.getIsbn())
            );
        }

        return book;
    }

    // Get up to five featured books.
    public List<Book> getFeaturedBooks() {

        return bookRepository.findByFeaturedTrue(
                PageRequest.of(0, BOOK_SECTION_SIZE)
        ).getContent();
    }

    // Get the five newest additions to the catalogue.
    public List<Book> getNewAdditions() {

        return bookRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(0, BOOK_SECTION_SIZE)
        ).getContent();
    }

    // Get the five most popular books based on rental history.
    public List<Book> getPopularBooks() {

        List<Object[]> rentalCounts =
                rentalRepository.findPopularBookRentalCounts();

        List<Book> popularBooks = new ArrayList<>();

        for (Object[] result : rentalCounts) {

            if (popularBooks.size() >= BOOK_SECTION_SIZE) {
                break;
            }

            Long bookId = ((Number) result[0]).longValue();

            bookRepository.findById(bookId).ifPresent(popularBooks::add);
        }

        // Fill in missing cover URLs for popular books.
        for (Book book : popularBooks) {
            if ((book.getCoverImageUrl() == null
                    || book.getCoverImageUrl().isBlank())
                    && book.getIsbn() != null
                    && !book.getIsbn().isBlank()) {

                book.setCoverImageUrl(
                        buildCoverImageUrl(book.getIsbn())
                );
            }
        }

        return popularBooks;
    }

    // Create a new book and automatically create one available copy.
    @Transactional
    public Book createBook(Book book) {

        String isbn = normalizeAndValidateIsbn(book.getIsbn());

        if (bookRepository.existsByIsbn(isbn)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A book with this ISBN already exists."
            );
        }

        book.setIsbn(isbn);

        if (book.getCoverImageUrl() == null
                || book.getCoverImageUrl().isBlank()) {
            book.setCoverImageUrl(buildCoverImageUrl(isbn));
        }

        // Save the book first so it receives an ID.
        Book savedBook = bookRepository.save(book);

        // Automatically create one physical copy.
        BookCopy bookCopy = new BookCopy();
        bookCopy.setBook(savedBook);
        bookCopy.setBarcode(generateBarcode());
        bookCopy.setStatus("AVAILABLE");

        bookCopyRepository.save(bookCopy);

        return savedBook;
    }

    // Update an existing book without creating another copy.
    @Transactional
    public Book updateBook(Long id, Book updatedBook) {

        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book not found."
                ));

        String isbn = normalizeAndValidateIsbn(
                updatedBook.getIsbn()
        );

        // Prevent another book from using the same ISBN.
        if (bookRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another book already uses this ISBN."
            );
        }

        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setIsbn(isbn);
        existingBook.setPublicationDate(
                updatedBook.getPublicationDate()
        );
        existingBook.setDescription(updatedBook.getDescription());
        existingBook.setCategory(updatedBook.getCategory());

        // Save the owner's Featured toggle.
        existingBook.setFeatured(updatedBook.isFeatured());

        // Use the supplied URL or generate one from the ISBN.
        if (updatedBook.getCoverImageUrl() != null
                && !updatedBook.getCoverImageUrl().isBlank()) {

            existingBook.setCoverImageUrl(
                    updatedBook.getCoverImageUrl()
            );

        } else {
            existingBook.setCoverImageUrl(
                    buildCoverImageUrl(isbn)
            );
        }

        // Preserve the existing ID, creation date and physical copies.
        return bookRepository.save(existingBook);
    }

    // Build an Open Library cover URL using the book ISBN.
    private String buildCoverImageUrl(String isbn) {

        return "https://covers.openlibrary.org/b/isbn/"
                + isbn
                + "-L.jpg?default=false";
    }

    // Validate and normalize ISBN for creation and updating.
    private String normalizeAndValidateIsbn(String rawIsbn) {

        if (rawIsbn == null || rawIsbn.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ISBN is required."
            );
        }

        String isbn = rawIsbn
                .replaceAll("[\\s-]", "")
                .toUpperCase();

        if (!isbn.matches("\\d{13}|\\d{9}[\\dX]")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ISBN must be a valid ISBN-10 or ISBN-13 format."
            );
        }

        return isbn;
    }

    // Generate a barcode for a new physical book copy.
    private String generateBarcode() {

        return "BC-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }

    // Delete a book only if it has no active rental.
    @Transactional
    public void deleteBook(Long id) {

        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Book not found."
            );
        }

        boolean hasActiveRental =
                rentalRepository.existsByBookCopy_Book_IdAndStatus(
                        id,
                        "ACTIVE"
                );

        if (hasActiveRental) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This book cannot be deleted because one of its copies has an active rental."
            );
        }

        bookRepository.deleteById(id);
    }
}
