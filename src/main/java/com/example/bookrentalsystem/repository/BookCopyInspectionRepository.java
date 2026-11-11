
package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.BookCopyInspection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookCopyInspectionRepository
        extends JpaRepository<BookCopyInspection, Long> {

    // Get inspection history for a specific physical book copy,
    // with the newest inspection first.
    List<BookCopyInspection> findByBookCopyIdOrderByInspectedAtDesc(
            Long bookCopyId
    );

    // Get all inspections performed by a specific user,
    // with the newest inspection first.
    List<BookCopyInspection> findByInspectedByIdOrderByInspectedAtDesc(
            Long userId
    );
}
