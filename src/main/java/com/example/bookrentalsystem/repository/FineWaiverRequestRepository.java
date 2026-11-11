
package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.FineWaiverRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FineWaiverRequestRepository
        extends JpaRepository<FineWaiverRequest, Long> {

    List<FineWaiverRequest> findByRequestedByIdOrderByRequestedAtDesc(
            Long requestedById
    );

    List<FineWaiverRequest> findByStatusOrderByRequestedAtAsc(
            String status
    );

    boolean existsByFineTransactionId(Long fineTransactionId);

    List<FineWaiverRequest> findByReviewedByIdOrderByReviewedAtDesc(
            Long reviewedById
    );

    @Query("SELECT r.fineTransaction.id FROM FineWaiverRequest r")
    List<Long> findAllFineTransactionIds();
}
