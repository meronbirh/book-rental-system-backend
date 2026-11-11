
package com.example.bookrentalsystem.service;

import com.example.bookrentalsystem.entity.FineWaiverRequest;
import com.example.bookrentalsystem.entity.UserAccount;
import com.example.bookrentalsystem.entity.Wallet;
import com.example.bookrentalsystem.entity.WalletTransaction;
import com.example.bookrentalsystem.repository.FineWaiverRequestRepository;
import com.example.bookrentalsystem.repository.UserAccountRepository;
import com.example.bookrentalsystem.repository.WalletRepository;
import com.example.bookrentalsystem.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
@Service
public class FineWaiverService {
    private final FineWaiverRequestRepository fineWaiverRequestRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserAccountRepository userAccountRepository;
    private final WalletRepository walletRepository;
    public FineWaiverService(
            FineWaiverRequestRepository fineWaiverRequestRepository,
            WalletTransactionRepository walletTransactionRepository,
            UserAccountRepository userAccountRepository,
            WalletRepository walletRepository) {
        this.fineWaiverRequestRepository = fineWaiverRequestRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.userAccountRepository = userAccountRepository;
        this.walletRepository = walletRepository;
    }
    /**
     * STAFF: Return fines that do not already have a waiver request.
     * Original transactions are preserved in the database.
     */
    @Transactional(readOnly = true)
    public List<WalletTransaction> getEligibleFineTransactions(
            Long staffUserId) {

        if (staffUserId == null) {
            throw new IllegalArgumentException(
                    "Staff user ID is required.");
        }
        UserAccount staff = userAccountRepository.findById(staffUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Staff account was not found."));
        if (!"STAFF".equalsIgnoreCase(staff.getRole())
                || !staff.isActive()) {
            throw new IllegalArgumentException(
                    "Only active STAFF accounts can view eligible fines.");
        }
        Set<Long> existingRequestFineIds =
                fineWaiverRequestRepository.findAllFineTransactionIds()
                        .stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
        return walletTransactionRepository.findAll()
                .stream()
                .filter(transaction ->
                        transaction != null
                                && "FINE".equalsIgnoreCase(
                                transaction.getType()))
                .filter(transaction ->
                        transaction.getId() != null
                                && !existingRequestFineIds.contains(
                                transaction.getId()))
                .sorted((a, b) ->
                        Long.compare(b.getId(), a.getId()))
                .toList();
    }
    @Transactional
    public FineWaiverRequest submitRequest(
            Long fineTransactionId,
            Long staffUserId,
            Double requestedAmount,
            String reason) {

        if (fineTransactionId == null || staffUserId == null) {
            throw new IllegalArgumentException(
                    "Fine transaction ID and staff user ID are required.");
        }
        if (requestedAmount == null
                || !Double.isFinite(requestedAmount)
                || requestedAmount <= 0) {
            throw new IllegalArgumentException(
                    "Requested amount must be a positive, finite number.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "A reason for the fine waiver is required.");
        }
        if (reason.trim().length() > 1000) {
            throw new IllegalArgumentException(
                    "The reason cannot exceed 1000 characters.");
        }
        UserAccount staff = userAccountRepository.findById(staffUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Staff account was not found."));
        if (!"STAFF".equalsIgnoreCase(staff.getRole())
                || !staff.isActive()) {
            throw new IllegalArgumentException(
                    "Only active STAFF accounts can submit fine-waiver requests.");
        }
        WalletTransaction fineTransaction =
                walletTransactionRepository.findById(fineTransactionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Fine transaction was not found."));

        if (!"FINE".equalsIgnoreCase(fineTransaction.getType())) {
            throw new IllegalArgumentException(
                    "The selected transaction is not a fine.");
        }

        if (fineTransaction.getWallet() == null
                || fineTransaction.getWallet().getId() == null) {
            throw new IllegalArgumentException(
                    "The fine transaction is not linked to a valid wallet.");
        }

        Double originalFine = fineTransaction.getAmount();

        if (originalFine == null
                || !Double.isFinite(originalFine)
                || originalFine <= 0) {
            throw new IllegalArgumentException(
                    "The original fine amount is invalid.");
        }

        if (requestedAmount > originalFine) {
            throw new IllegalArgumentException(
                    "The requested amount cannot exceed the original fine.");
        }

        if (fineWaiverRequestRepository
                .existsByFineTransactionId(fineTransactionId)) {
            throw new IllegalArgumentException(
                    "A waiver request already exists for this fine.");
        }

        FineWaiverRequest request = new FineWaiverRequest();
        request.setFineTransaction(fineTransaction);
        request.setRequestedAmount(requestedAmount);
        request.setReason(reason.trim());
        request.setStatus("PENDING");
        request.setRequestedBy(staff);

        return fineWaiverRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<FineWaiverRequest> getRequestsByStaff(
            Long staffUserId) {

        if (staffUserId == null) {
            throw new IllegalArgumentException(
                    "Staff user ID is required.");
        }

        UserAccount staff = userAccountRepository.findById(staffUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Staff account was not found."));

        if (!"STAFF".equalsIgnoreCase(staff.getRole())) {
            throw new IllegalArgumentException(
                    "This account is not a STAFF account.");
        }

        return fineWaiverRequestRepository
                .findByRequestedByIdOrderByRequestedAtDesc(staffUserId);
    }

    @Transactional(readOnly = true)
    public List<FineWaiverRequest> getPendingRequests(
            Long ownerUserId) {

        if (ownerUserId == null) {
            throw new IllegalArgumentException(
                    "Owner user ID is required.");
        }

        UserAccount owner = userAccountRepository.findById(ownerUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Owner account was not found."));

        if (!"OWNER".equalsIgnoreCase(owner.getRole())
                || !owner.isActive()) {
            throw new IllegalArgumentException(
                    "Only an active OWNER can view pending fine-waiver requests.");
        }

        return fineWaiverRequestRepository
                .findByStatusOrderByRequestedAtAsc("PENDING");
    }

    @Transactional(readOnly = true)
    public List<FineWaiverRequest> getHistoryRequests(
            Long ownerUserId) {

        if (ownerUserId == null) {
            throw new IllegalArgumentException(
                    "Owner user ID is required.");
        }

        UserAccount owner = userAccountRepository.findById(ownerUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Owner account was not found."));

        if (!"OWNER".equalsIgnoreCase(owner.getRole())
                || !owner.isActive()) {
            throw new IllegalArgumentException(
                    "Only active OWNER accounts can view fine-waiver history.");
        }

        return fineWaiverRequestRepository
                .findByReviewedByIdOrderByReviewedAtDesc(ownerUserId);
    }

    @Transactional
    public FineWaiverRequest reviewRequest(
            Long requestId,
            Long ownerUserId,
            String decision,
            String reviewComment) {

        if (requestId == null || ownerUserId == null) {
            throw new IllegalArgumentException(
                    "Request ID and owner user ID are required.");
        }

        UserAccount owner = userAccountRepository.findById(ownerUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Owner account was not found."));

        if (!"OWNER".equalsIgnoreCase(owner.getRole())
                || !owner.isActive()) {
            throw new IllegalArgumentException(
                    "Only an active OWNER can review requests.");
        }

        FineWaiverRequest request =
                fineWaiverRequestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Fine-waiver request was not found."));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException(
                    "This request has already been reviewed.");
        }

        if (request.getRequestedBy() != null
                && request.getRequestedBy().getId().equals(ownerUserId)) {
            throw new IllegalArgumentException(
                    "You cannot review your own request.");
        }

        if (decision == null
                || (!"APPROVE".equalsIgnoreCase(decision)
                && !"REJECT".equalsIgnoreCase(decision))) {
            throw new IllegalArgumentException(
                    "Decision must be APPROVE or REJECT.");
        }

        if (reviewComment != null && reviewComment.length() > 1000) {
            throw new IllegalArgumentException(
                    "Review comment cannot exceed 1000 characters.");
        }

        request.setReviewedBy(owner);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewComment(
                reviewComment == null || reviewComment.isBlank()
                        ? null
                        : reviewComment.trim());

        if ("APPROVE".equalsIgnoreCase(decision)) {

            WalletTransaction fine = request.getFineTransaction();

            if (fine == null
                    || fine.getWallet() == null
                    || fine.getWallet().getId() == null) {
                throw new IllegalArgumentException(
                        "The original fine transaction has no valid wallet.");
            }

            if (!"FINE".equalsIgnoreCase(fine.getType())) {
                throw new IllegalArgumentException(
                        "The original transaction is not a fine.");
            }

            Double amount = request.getRequestedAmount();
            Double originalFine = fine.getAmount();

            if (amount == null
                    || !Double.isFinite(amount)
                    || amount <= 0
                    || originalFine == null
                    || !Double.isFinite(originalFine)
                    || originalFine <= 0
                    || amount > originalFine) {
                throw new IllegalArgumentException(
                        "The requested waiver amount is invalid.");
            }

            Long walletId = fine.getWallet().getId();

            Wallet wallet = walletRepository.findById(walletId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "The member wallet was not found."));

            if (wallet.getBalance() == null
                    || !Double.isFinite(wallet.getBalance())) {
                throw new IllegalArgumentException(
                        "The member wallet balance is invalid.");
            }

            // Credit the approved waiver amount to the member's wallet.
            wallet.setBalance(wallet.getBalance() + amount);
            walletRepository.save(wallet);

            // Preserve the original fine and record the refund separately.
            WalletTransaction refund = new WalletTransaction();
            refund.setAmount(amount);
            refund.setType("FINE_WAIVER_REFUND");
            refund.setTransactionDate(LocalDate.now());
            refund.setWallet(wallet);
            refund.setRental(fine.getRental());

            walletTransactionRepository.save(refund);

            request.setStatus("APPROVED");

        } else {
            request.setStatus("REJECTED");
        }

        return fineWaiverRequestRepository.save(request);
    }
}
