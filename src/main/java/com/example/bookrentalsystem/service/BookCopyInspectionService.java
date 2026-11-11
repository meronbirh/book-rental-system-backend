
package com.example.bookrentalsystem.service;

import com.example.bookrentalsystem.entity.BookCopy;
import com.example.bookrentalsystem.entity.BookCopyInspection;
import com.example.bookrentalsystem.entity.UserAccount;
import com.example.bookrentalsystem.repository.BookCopyInspectionRepository;
import com.example.bookrentalsystem.repository.BookCopyRepository;
import com.example.bookrentalsystem.repository.UserAccountRepository;
import com.example.bookrentalsystem.entity.Notification;
import com.example.bookrentalsystem.service.NotificationService;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Set;

@Service
public class BookCopyInspectionService {

    private static final Set<String> VALID_CONDITIONS =
            Set.of("NEW", "GOOD", "FAIR", "POOR");

    private final BookCopyInspectionRepository inspectionRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserAccountRepository userAccountRepository;
    private final NotificationService notificationService;
    public BookCopyInspectionService(
            BookCopyInspectionRepository inspectionRepository,
            BookCopyRepository bookCopyRepository,
            UserAccountRepository userAccountRepository,
            NotificationService notificationService) {

        this.inspectionRepository = inspectionRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.userAccountRepository = userAccountRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public BookCopyInspection recordInspection(
            Long copyId,
            String observedCondition,
            String notes,
            Long inspectedByUserId) {

        if (copyId == null || inspectedByUserId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Book copy and inspecting user are required."
            );
        }

        String normalizedCondition =
                validateCondition(observedCondition);

        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book copy not found."
                ));

        if ("DISPOSED".equalsIgnoreCase(copy.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A disposed book copy cannot be inspected."
            );
        }

        UserAccount staff = userAccountRepository
                .findById(inspectedByUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inspecting user not found."
                ));

        if (!staff.isActive()
                || !"STAFF".equalsIgnoreCase(staff.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only active Staff accounts can record inspections."
            );
        }

        String previousCondition = copy.getCondition();

        if (previousCondition == null
                || previousCondition.isBlank()) {
            previousCondition = "UNKNOWN";
        }

        BookCopyInspection inspection = new BookCopyInspection();
        inspection.setBookCopy(copy);
        inspection.setPreviousCondition(previousCondition);
        inspection.setObservedCondition(normalizedCondition);
        inspection.setNotes(notes == null ? null : notes.trim());
        inspection.setInspectedBy(staff);

        // Update only the physical condition.
        // Do not change availability or rental status.
        copy.setCondition(normalizedCondition);

        bookCopyRepository.save(copy);

        BookCopyInspection savedInspection =
                inspectionRepository.save(inspection);

        Notification notification = new Notification();
        notification.setMessage(
                "Your inventory inspection was saved successfully."
        );
        notification.setCreatedDate(LocalDateTime.now());
        notification.setStatus("UNREAD");
        notification.setUserAccount(staff);

        notificationService.createNotification(notification);

        return savedInspection;    }

    @Transactional(readOnly = true)
    public List<BookCopyInspection> getCopyHistory(Long copyId) {

        if (!bookCopyRepository.existsById(copyId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Book copy not found."
            );
        }

        return inspectionRepository
                .findByBookCopyIdOrderByInspectedAtDesc(copyId);
    }

    @Transactional(readOnly = true)
    public List<BookCopyInspection> getStaffHistory(Long userId) {

        UserAccount staff = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found."
                ));

        if (!"STAFF".equalsIgnoreCase(staff.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This history is available only for Staff accounts."
            );
        }

        return inspectionRepository
                .findByInspectedByIdOrderByInspectedAtDesc(userId);
    }

    private String validateCondition(String condition) {

        if (condition == null || condition.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Observed condition is required."
            );
        }

        String normalized = condition.trim().toUpperCase();

        if (!VALID_CONDITIONS.contains(normalized)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Condition must be NEW, GOOD, FAIR, or POOR."
            );
        }

        return normalized;
    }
}
