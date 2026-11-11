
package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.BookDisposalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookDisposalRequestRepository
        extends JpaRepository<BookDisposalRequest, Long> {

    // Get disposal requests submitted by a specific staff member.
    List<BookDisposalRequest> findByRequestedBy_Id(Long userId);

    // Get requests with a particular status, such as PENDING.
    List<BookDisposalRequest> findByStatus(String status);

    // Get all requests, newest first, for the OWNER's review history.
    List<BookDisposalRequest> findAllByOrderByRequestedAtDesc();

    // Count requests by decision status for report cards.
    long countByStatus(String status);

    // Prevent multiple unresolved requests for the same physical copy.
    boolean existsByBookCopy_IdAndStatus(
            Long bookCopyId,
            String status
    );
}
