
package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    // Count every physical copy registered for a book.
    long countByBookId(Long bookId);

    // Count copies by their current status, such as AVAILABLE or DISPOSED.
    long countByBookIdAndStatus(Long bookId, String status);

    boolean existsByCopyNumber(String copyNumber);

    boolean existsByBarcode(String barcode);
}
