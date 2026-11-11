
package com.example.bookrentalsystem.service;

import com.example.bookrentalsystem.entity.BookCopy;
import com.example.bookrentalsystem.entity.BookDisposalRequest;
import com.example.bookrentalsystem.entity.UserAccount;
import com.example.bookrentalsystem.repository.BookCopyRepository;
import com.example.bookrentalsystem.repository.BookDisposalRequestRepository;
import com.example.bookrentalsystem.repository.RentalRepository;
import com.example.bookrentalsystem.repository.UserAccountRepository;
import com.example.bookrentalsystem.entity.Notification;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookDisposalService {

    private final BookDisposalRequestRepository disposalRequestRepository;
    private final BookCopyRepository bookCopyRepository;
    private final RentalRepository rentalRepository;
    private final UserAccountRepository userAccountRepository;
    private final NotificationService notificationService;
    public BookDisposalService(
            BookDisposalRequestRepository disposalRequestRepository,
            BookCopyRepository bookCopyRepository,
            RentalRepository rentalRepository,
            UserAccountRepository userAccountRepository,
            NotificationService notificationService) {

        this.disposalRequestRepository = disposalRequestRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.rentalRepository = rentalRepository;
        this.userAccountRepository = userAccountRepository;
        this.notificationService = notificationService;
    }

    // STAFF submits a disposal request.
    @Transactional
    public BookDisposalRequest submitRequest(
            Long bookCopyId,
            Long staffUserId,
            String reason) {

        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A disposal reason is required.");
        }

        BookCopy bookCopy = bookCopyRepository.findById(bookCopyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book copy not found."));

        UserAccount staff = userAccountRepository.findById(staffUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Staff account not found."));

        if (!"STAFF".equals(staff.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only STAFF can submit disposal requests.");
        }

        if (!staff.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This staff account is inactive.");
        }

        if ("DISPOSED".equals(bookCopy.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This book copy has already been disposed.");
        }

        if (rentalRepository.existsByBookCopy_IdAndStatus(
                bookCopyId, "ACTIVE")) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An actively rented book copy cannot be submitted for disposal.");
        }

        if (disposalRequestRepository.existsByBookCopy_IdAndStatus(
                bookCopyId, "PENDING")) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A pending disposal request already exists for this copy.");
        }

        BookDisposalRequest request = new BookDisposalRequest();
        request.setBookCopy(bookCopy);
        request.setReason(reason.trim());
        request.setStatus("PENDING");
        request.setRequestedBy(staff);
        request.setRequestedAt(LocalDateTime.now());

        return disposalRequestRepository.save(request);
    }

    // STAFF views their own disposal requests.
    public List<BookDisposalRequest> getStaffRequests(Long staffUserId) {
        UserAccount staff = userAccountRepository.findById(staffUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Staff account not found."));

        if (!"STAFF".equals(staff.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only STAFF can view requests through this operation.");
        }

        return disposalRequestRepository.findByRequestedBy_Id(staffUserId);
    }

    // OWNER views requests awaiting a decision.
    public List<BookDisposalRequest> getPendingRequests(Long ownerUserId) {
        getActiveOwner(ownerUserId);
        return disposalRequestRepository.findByStatus("PENDING");
    }

    // OWNER views the complete disposal history.
    public List<BookDisposalRequest> getAllRequests(Long ownerUserId) {
        getActiveOwner(ownerUserId);
        return disposalRequestRepository
                .findAllByOrderByRequestedAtDesc();
    }

    // OWNER views request counts for report cards.
    public Map<String, Long> getRequestSummary(Long ownerUserId) {
        getActiveOwner(ownerUserId);

        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put(
                "pending",
                disposalRequestRepository.countByStatus("PENDING"));
        summary.put(
                "approved",
                disposalRequestRepository.countByStatus("APPROVED"));
        summary.put(
                "rejected",
                disposalRequestRepository.countByStatus("REJECTED"));
        summary.put(
                "total",
                disposalRequestRepository.count());

        return summary;
    }

    // Inventory information for a particular disposal request.
    public Map<String, Object> getInventoryDetails(
            Long ownerUserId,
            Long requestId) {

        getActiveOwner(ownerUserId);

        BookDisposalRequest request = disposalRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Disposal request not found."));
        BookCopy copy = request.getBookCopy();
        if (copy == null || copy.getBook() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Book information could not be found for this request.");
        }
        Long bookId = copy.getBook().getId();
        long totalCopies = bookCopyRepository.countByBookId(bookId);
        long disposedCopies = bookCopyRepository
                .countByBookIdAndStatus(bookId, "DISPOSED");
        long circulatingCopies = totalCopies - disposedCopies;
        long activeRentals = rentalRepository
                .countByBookCopy_Book_IdAndStatus(bookId, "ACTIVE");
        boolean thisCopyAlreadyDisposed =
                "DISPOSED".equals(copy.getStatus());

        long remainingAfterApproval = circulatingCopies;

        if (!thisCopyAlreadyDisposed
                && "PENDING".equals(request.getStatus())) {
            remainingAfterApproval =
                    Math.max(0, circulatingCopies - 1);
        }

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("bookId", bookId);
        details.put("title", copy.getBook().getTitle());
        details.put("author", copy.getBook().getAuthor());
        details.put("isbn", copy.getBook().getIsbn());
        details.put("publicationDate",
                copy.getBook().getPublicationDate());
        details.put("description", copy.getBook().getDescription());
        details.put("coverImageUrl", copy.getBook().getCoverImageUrl());

        details.put("bookCopyId", copy.getId());
        details.put("copyNumber", copy.getCopyNumber());
        details.put("barcode", copy.getBarcode());
        details.put("condition", copy.getCondition());
        details.put("copyStatus", copy.getStatus());

        details.put("totalCopies", totalCopies);
        details.put("disposedCopies", disposedCopies);
        details.put("circulatingCopies", circulatingCopies);
        details.put("activeRentals", activeRentals);
        details.put("remainingAfterApproval", remainingAfterApproval);

        return details;
    }

    // OWNER approves or rejects a pending request.
    @Transactional
    public BookDisposalRequest reviewRequest(
            Long requestId,
            Long ownerUserId,
            String decision,
            String reviewComment) {

        UserAccount owner = getActiveOwner(ownerUserId);

        BookDisposalRequest request = disposalRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Disposal request not found."));

        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This request has already been reviewed.");
        }

        if (decision == null
                || (!"APPROVED".equalsIgnoreCase(decision)
                && !"REJECTED".equalsIgnoreCase(decision))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Decision must be APPROVED or REJECTED.");
        }

        BookCopy bookCopy = bookCopyRepository
                .findById(request.getBookCopy().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book copy not found."));

        String normalizedDecision = decision.toUpperCase();

        if ("APPROVED".equals(normalizedDecision)) {
            if ("DISPOSED".equals(bookCopy.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This book copy has already been disposed.");
            }

            if (rentalRepository.existsByBookCopy_IdAndStatus(
                    bookCopy.getId(), "ACTIVE")) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This copy has an active rental and cannot be disposed.");
            }

            bookCopy.setStatus("DISPOSED");
            bookCopyRepository.save(bookCopy);
        }

        request.setStatus(normalizedDecision);
        request.setReviewedBy(owner);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewComment(
                reviewComment == null ? null : reviewComment.trim());

        BookDisposalRequest savedRequest =
                disposalRequestRepository.save(request);

        UserAccount requestingStaff = savedRequest.getRequestedBy();

        if (requestingStaff != null) {
            String bookTitle = savedRequest.getBookCopy() != null
                    && savedRequest.getBookCopy().getBook() != null
                    ? savedRequest.getBookCopy().getBook().getTitle()
                    : "the book";

            Notification notification = new Notification();
            notification.setMessage(
                    "Your disposal request for \"" + bookTitle
                            + "\" was "
                            + normalizedDecision.toLowerCase(Locale.ROOT)
                            + "."
            );
            notification.setCreatedDate(LocalDateTime.now());
            notification.setStatus("UNREAD");
            notification.setUserAccount(requestingStaff);

            notificationService.createNotification(notification);
        }

        return savedRequest;    }

    private UserAccount getActiveOwner(Long ownerUserId) {
        UserAccount owner = userAccountRepository.findById(ownerUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Owner account not found."));

        if (!"OWNER".equals(owner.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the OWNER can review disposal requests.");
        }

        if (!owner.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This owner account is inactive.");
        }

        return owner;
    }
}
