
package com.example.bookrentalsystem.service;

import com.example.bookrentalsystem.entity.Book;
import com.example.bookrentalsystem.entity.BookCopy;
import com.example.bookrentalsystem.repository.BookCopyRepository;
import com.example.bookrentalsystem.repository.BookRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;

    public BookCopyService(
            BookCopyRepository bookCopyRepository,
            BookRepository bookRepository) {
        this.bookCopyRepository = bookCopyRepository;
        this.bookRepository = bookRepository;
    }

    public List<BookCopy> getAllBookCopies() {
        return bookCopyRepository.findAll();
    }

    public BookCopy getBookCopyById(Long id) {
        return bookCopyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book copy not found."
                ));
    }

    // Register one physical copy of an existing book.
    @Transactional
    public BookCopy createBookCopy(BookCopy bookCopy) {

        if (bookCopy.getBook() == null
                || bookCopy.getBook().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select an existing book."
            );
        }

        Long bookId = bookCopy.getBook().getId();

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "The selected book does not exist."
                ));

        bookCopy.setCopyNumber(generateCopyNumber(bookId));
        bookCopy.setBarcode(generateBarcode());
        bookCopy.setBook(book);
        bookCopy.setStatus("AVAILABLE");

        if (bookCopy.getCondition() == null
                || bookCopy.getCondition().isBlank()) {
            bookCopy.setCondition("GOOD");
        } else {
            bookCopy.setCondition(
                    validateCondition(bookCopy.getCondition())
            );
        }

        return bookCopyRepository.save(bookCopy);
    }

    // Register multiple physical copies in a single transaction.
    @Transactional
    public List<BookCopy> createBookCopiesBulk(
            Long bookId,
            int quantity,
            String condition) {

        if (bookId == null || bookId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select an existing book."
            );
        }

        if (quantity < 1 || quantity > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be between 1 and 500."
            );
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "The selected book does not exist."
                ));

        String selectedCondition =
                condition == null || condition.isBlank()
                        ? "GOOD"
                        : validateCondition(condition);

        List<BookCopy> copies = new ArrayList<>(quantity);

        // Prevent duplicate identifiers within this batch.
        Set<String> batchCopyNumbers = new HashSet<>();
        Set<String> batchBarcodes = new HashSet<>();

        long nextNumber =
                bookCopyRepository.countByBookId(bookId) + 1;

        for (int i = 0; i < quantity; i++) {

            String copyNumber;

            do {
                copyNumber = String.format(
                        "COPY-%d-%03d",
                        bookId,
                        nextNumber++
                );
            } while (
                    bookCopyRepository.existsByCopyNumber(copyNumber)
                            || !batchCopyNumbers.add(copyNumber)
            );

            String barcode;

            do {
                barcode = generateBarcode();
            } while (
                    bookCopyRepository.existsByBarcode(barcode)
                            || !batchBarcodes.add(barcode)
            );

            BookCopy copy = new BookCopy();
            copy.setBook(book);
            copy.setCopyNumber(copyNumber);
            copy.setBarcode(barcode);
            copy.setCondition(selectedCondition);
            copy.setStatus("AVAILABLE");

            copies.add(copy);
        }

        // All records are saved within the same transaction.
        return bookCopyRepository.saveAll(copies);
    }

    private String validateCondition(String condition) {

        String normalized = condition.trim().toUpperCase();

        if (!Set.of("NEW", "GOOD", "FAIR", "POOR")
                .contains(normalized)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Condition must be NEW, GOOD, FAIR, or POOR."
            );
        }

        return normalized;
    }

    // Update an existing copy without creating another one.
    @Transactional
    public BookCopy updateBookCopy(Long id, BookCopy updatedCopy) {

        BookCopy existingCopy = bookCopyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book copy not found."
                ));

        // Keep the original book association unchanged.
        if (updatedCopy.getBook() != null
                && updatedCopy.getBook().getId() != null
                && !updatedCopy.getBook().getId()
                .equals(existingCopy.getBook().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A copy cannot be reassigned to a different book here."
            );
        }

        if (updatedCopy.getCopyNumber() != null
                && !updatedCopy.getCopyNumber()
                .equals(existingCopy.getCopyNumber())
                && bookCopyRepository.existsByCopyNumber(
                updatedCopy.getCopyNumber())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "That copy number is already in use."
            );
        }

        if (updatedCopy.getBarcode() != null
                && !updatedCopy.getBarcode()
                .equals(existingCopy.getBarcode())
                && bookCopyRepository.existsByBarcode(
                updatedCopy.getBarcode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "That barcode is already in use."
            );
        }

        existingCopy.setCopyNumber(updatedCopy.getCopyNumber());
        existingCopy.setBarcode(updatedCopy.getBarcode());

        if (updatedCopy.getCondition() != null
                && !updatedCopy.getCondition().isBlank()) {
            existingCopy.setCondition(
                    validateCondition(updatedCopy.getCondition())
            );
        }

        // Preserve existing rental availability behavior.
        // Rental and return operations should control this status.

        return bookCopyRepository.save(existingCopy);
    }

    private String generateCopyNumber(Long bookId) {

        long nextNumber =
                bookCopyRepository.countByBookId(bookId) + 1;

        String copyNumber;

        do {
            copyNumber = String.format(
                    "COPY-%d-%03d",
                    bookId,
                    nextNumber++
            );
        } while (
                bookCopyRepository.existsByCopyNumber(copyNumber)
        );

        return copyNumber;
    }

    private String generateBarcode() {

        String barcode;

        do {
            barcode = "BC-" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();
        } while (bookCopyRepository.existsByBarcode(barcode));

        return barcode;
    }

    @Transactional
    public void deleteBookCopy(Long id) {

        if (!bookCopyRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Book copy not found."
            );
        }

        bookCopyRepository.deleteById(id);
    }
}
