
package com.example.bookrentalsystem.service;

import com.example.bookrentalsystem.dto.RentalReturnResponse;
import com.example.bookrentalsystem.entity.BookCopy;
import com.example.bookrentalsystem.entity.Member;
import com.example.bookrentalsystem.entity.Notification;
import com.example.bookrentalsystem.entity.Rental;
import com.example.bookrentalsystem.entity.UserAccount;
import com.example.bookrentalsystem.entity.Wallet;
import com.example.bookrentalsystem.entity.WalletTransaction;
import com.example.bookrentalsystem.repository.BookCopyRepository;
import com.example.bookrentalsystem.repository.MemberRepository;
import com.example.bookrentalsystem.repository.RentalRepository;
import com.example.bookrentalsystem.repository.UserAccountRepository;
import com.example.bookrentalsystem.repository.WalletRepository;
import com.example.bookrentalsystem.repository.WalletTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RentalService {

    private static final double RENTAL_FEE = 50.0;
    private static final double DAILY_FINE = 10.0;
    private static final int RENTAL_DAYS = 7;

    private final RentalRepository rentalRepository;
    private final BookCopyRepository bookCopyRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final MemberRepository memberRepository;
    private final UserAccountRepository userAccountRepository;
    private final NotificationService notificationService;
    private final WalletService walletService;

    public RentalService(
            RentalRepository rentalRepository,
            BookCopyRepository bookCopyRepository,
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            MemberRepository memberRepository,
            UserAccountRepository userAccountRepository,
            NotificationService notificationService,
            WalletService walletService) {

        this.rentalRepository = rentalRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.memberRepository = memberRepository;
        this.userAccountRepository = userAccountRepository;
        this.notificationService = notificationService;
        this.walletService = walletService;
    }

    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    public Rental getRentalById(Long id) {
        return rentalRepository.findById(id).orElse(null);
    }

    @Transactional
    public Rental createRental(Rental rental) {

        // Existing rental: handle a return.
        if (rental.getId() != null) {
            return updateExistingRental(rental);
        }

        // Validate the member.
        if (rental.getMember() == null
                || rental.getMember().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A member must be selected."
            );
        }

        Member member = memberRepository.findById(
                rental.getMember().getId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Member not found."
        ));

        LocalDate today = LocalDate.now();

        // Prevent another rental while an overdue rental is active.
        boolean hasOverdueRental = rentalRepository.findAll()
                .stream()
                .anyMatch(existingRental ->
                        existingRental.getMember() != null
                                && existingRental.getMember().getId() != null
                                && existingRental.getMember().getId()
                                .equals(member.getId())
                                && "ACTIVE".equals(existingRental.getStatus())
                                && existingRental.getDueDate() != null
                                && existingRental.getDueDate().isBefore(today)
                );

        if (hasOverdueRental) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please return your overdue rental before requesting another rental."
            );
        }

        // Ensure the member is linked to a login account.
        if (member.getUserAccount() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This member is not linked to a user account. "
                            + "Please link the member to an account before renting."
            );
        }

        // Validate the book copy.
        if (rental.getBookCopy() == null
                || rental.getBookCopy().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A book copy must be selected."
            );
        }

        BookCopy bookCopy = bookCopyRepository.findById(
                rental.getBookCopy().getId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Book copy not found."
        ));

        if (!"AVAILABLE".equals(bookCopy.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This book copy is not available for rent."
            );
        }

        // Find the member's wallet.
        Wallet wallet = walletRepository.findByMemberId(member.getId());

        if (wallet == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You need a wallet to rent a book."
            );
        }

        double balance = wallet.getBalance() == null
                ? 0.0
                : wallet.getBalance();

        if (balance < RENTAL_FEE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient wallet balance. You need at least 50 Birr."
            );
        }

        // Set rental details.
        rental.setMember(member);
        rental.setBookCopy(bookCopy);
        rental.setRentalDate(today);
        rental.setDueDate(today.plusDays(RENTAL_DAYS));
        rental.setReturnDate(null);
        rental.setStatus("ACTIVE");

        // Mark the physical copy unavailable.
        bookCopy.setStatus("UNAVAILABLE");
        bookCopyRepository.save(bookCopy);

        // Save the rental before processing its payment.
        Rental savedRental = rentalRepository.save(rental);

        walletService.processRentalPayment(
                wallet.getId(),
                RENTAL_FEE,
                savedRental
        );

        // Notify the member about the successful rental.
        Notification notification = new Notification();
        notification.setMessage(
                "Your book rental was successful. "
                        + "Please return the book by "
                        + savedRental.getDueDate() + "."
        );
        notification.setCreatedDate(LocalDateTime.now());
        notification.setStatus("UNREAD");
        notification.setUserAccount(member.getUserAccount());

        notificationService.createNotification(notification);

        return savedRental;
    }

    // Return a rental and provide the summary expected by the controller.
    @Transactional
    public RentalReturnResponse returnRental(Long id) {

        Rental existing = rentalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rental not found."
                ));

        boolean alreadyReturned =
                "RETURNED".equals(existing.getStatus());

        if (!alreadyReturned) {
            Rental returnRequest = new Rental();
            returnRequest.setId(id);
            returnRequest.setStatus("RETURNED");

            existing = updateExistingRental(returnRequest);
        }

        long overdueDays = 0;

        if (existing.getDueDate() != null
                && existing.getReturnDate() != null) {
            overdueDays = Math.max(
                    0L,
                    ChronoUnit.DAYS.between(
                            existing.getDueDate(),
                            existing.getReturnDate()
                    )
            );
        }

        double fineAmountDeducted = overdueDays * DAILY_FINE;

        Double walletBalanceAfter = null;

        if (existing.getMember() != null
                && existing.getMember().getId() != null) {

            Wallet wallet = walletRepository.findByMemberId(
                    existing.getMember().getId()
            );

            if (wallet != null) {
                walletBalanceAfter = wallet.getBalance();
            }
        }

        return new RentalReturnResponse(
                existing,
                overdueDays > 0,
                overdueDays,
                fineAmountDeducted,
                walletBalanceAfter,
                alreadyReturned
        );
    }

    @Transactional
    public Rental updateExistingRental(Rental rental) {

        Rental existing = rentalRepository.findById(
                rental.getId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Rental not found."
        ));

        // Prevent processing the same return or fine twice.
        if ("RETURNED".equals(existing.getStatus())) {
            return existing;
        }

        if (!"ACTIVE".equals(existing.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only active rentals can be returned."
            );
        }

        if (!"RETURNED".equals(rental.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported rental update."
            );
        }

        LocalDate returnDate = rental.getReturnDate() != null
                ? rental.getReturnDate()
                : LocalDate.now();

        if (returnDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Return date cannot be in the future."
            );
        }

        existing.setReturnDate(returnDate);
        existing.setStatus("RETURNED");

        // Make the book copy available again.
        BookCopy bookCopy = existing.getBookCopy();

        if (bookCopy != null) {
            bookCopy.setStatus("AVAILABLE");
            bookCopyRepository.save(bookCopy);
        }

        // Apply and record the late fine, if any.
        applyLateFine(existing, returnDate);

        Rental savedRental = rentalRepository.save(existing);

        Member member = savedRental.getMember();

        // Notify the member.
        if (member != null && member.getUserAccount() != null) {
            Notification notification = new Notification();

            notification.setMessage(
                    "Your book has been returned successfully. "
                            + "Thank you for returning it to the library."
            );
            notification.setCreatedDate(LocalDateTime.now());
            notification.setStatus("UNREAD");
            notification.setUserAccount(member.getUserAccount());

            notificationService.createNotification(notification);
        }

        // Prepare details for staff notifications.
        String bookTitle = "a book";

        if (bookCopy != null
                && bookCopy.getBook() != null
                && bookCopy.getBook().getTitle() != null) {
            bookTitle = bookCopy.getBook().getTitle();
        }

        String memberName = "a member";

        if (member != null && member.getUserAccount() != null) {
            String firstName = member.getUserAccount().getFirstName();

            if (firstName != null && !firstName.isBlank()) {
                memberName = firstName;
            }
        }

        // Notify every active STAFF account.
        List<UserAccount> activeStaff = userAccountRepository.findAll()
                .stream()
                .filter(UserAccount::isActive)
                .filter(user -> user.getRole() != null
                        && "STAFF".equalsIgnoreCase(user.getRole().trim()))
                .toList();

        for (UserAccount staff : activeStaff) {
            Notification staffNotification = new Notification();

            staffNotification.setMessage(
                    "The book \"" + bookTitle
                            + "\" was returned by " + memberName + "."
            );
            staffNotification.setCreatedDate(LocalDateTime.now());
            staffNotification.setStatus("UNREAD");
            staffNotification.setUserAccount(staff);

            notificationService.createNotification(staffNotification);
        }

        return savedRental;
    }

    private void applyLateFine(Rental rental, LocalDate returnDate) {

        if (rental.getDueDate() == null
                || rental.getMember() == null
                || rental.getMember().getId() == null) {
            return;
        }

        long overdueDays = ChronoUnit.DAYS.between(
                rental.getDueDate(),
                returnDate
        );

        if (overdueDays <= 0) {
            return;
        }

        double fine = overdueDays * DAILY_FINE;

        Wallet wallet = walletRepository.findByMemberId(
                rental.getMember().getId()
        );

        if (wallet == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot process the late fine because the member has no wallet."
            );
        }

        double balance = wallet.getBalance() == null
                ? 0.0
                : wallet.getBalance();

        // Deduct the full fine, even if the balance becomes negative.
        wallet.setBalance(balance - fine);
        walletRepository.save(wallet);

        // Record the fine transaction.
        WalletTransaction transaction = new WalletTransaction();
        transaction.setAmount(fine);
        transaction.setType("FINE");
        transaction.setTransactionDate(returnDate);
        transaction.setWallet(wallet);
        transaction.setRental(rental);

        walletTransactionRepository.save(transaction);
    }

    public void deleteRental(Long id) {
        rentalRepository.deleteById(id);
    }
}
