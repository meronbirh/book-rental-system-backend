
package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RentalRepository
        extends JpaRepository<Rental, Long> {

    boolean existsByBookCopy_Book_IdAndStatus(
            Long bookId,
            String status
    );

    // Check whether a specific physical copy has a rental
    // with the given status, such as ACTIVE.
    boolean existsByBookCopy_IdAndStatus(
            Long bookCopyId,
            String status
    );

    // Count rentals for all physical copies of a book
    // with the given status, such as ACTIVE.
    long countByBookCopy_Book_IdAndStatus(
            Long bookId,
            String status
    );

    // Get book IDs and successful rental counts,
    // ranked from most rented to least rented.
    @Query("""
        SELECT r.bookCopy.book.id, COUNT(r.id)
        FROM Rental r
        WHERE r.status IN ('ACTIVE', 'RETURNED')
        GROUP BY r.bookCopy.book.id
        ORDER BY COUNT(r.id) DESC
        """)
    List<Object[]> findPopularBookRentalCounts();
}
