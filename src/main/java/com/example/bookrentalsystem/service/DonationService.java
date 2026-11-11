package com.example.bookrentalsystem.service;

import com.example.bookrentalsystem.entity.Book;
import com.example.bookrentalsystem.entity.BookCopy;
import com.example.bookrentalsystem.entity.Donation;
import com.example.bookrentalsystem.entity.DonationItem;
import com.example.bookrentalsystem.entity.Notification;
import com.example.bookrentalsystem.entity.UserAccount;
import com.example.bookrentalsystem.repository.BookCopyRepository;
import com.example.bookrentalsystem.repository.BookRepository;
import com.example.bookrentalsystem.repository.DonationItemRepository;
import com.example.bookrentalsystem.repository.DonationRepository;
import com.example.bookrentalsystem.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonationItemRepository donationItemRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final NotificationRepository notificationRepository;

    public DonationService(
            DonationRepository donationRepository,
            DonationItemRepository donationItemRepository,
            BookRepository bookRepository,
            BookCopyRepository bookCopyRepository,
            NotificationRepository notificationRepository) {

        this.donationRepository = donationRepository;
        this.donationItemRepository = donationItemRepository;
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    public Donation getDonationById(Long id) {
        return donationRepository.findById(id).orElse(null);
    }

    public Donation createDonation(Donation donation) {

        Donation savedDonation = donationRepository.save(donation);

        if ("PENDING".equals(donation.getStatus())) {

            Notification notification = new Notification();

            notification.setMessage(
                    "A new donation request has been submitted."
            );

            notification.setCreatedDate(LocalDateTime.now());
            notification.setStatus("UNREAD");

            UserAccount admin = new UserAccount();
            admin.setId(2L);

            notification.setUserAccount(admin);

            notificationRepository.save(notification);
        }

        return savedDonation;
    }

    public void deleteDonation(Long id) {
        donationRepository.deleteById(id);
    }

    public Donation updateDonation(Donation donation) {

        Donation existingDonation =
                donationRepository.findById(donation.getId())
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Donation not found."
                                )
                        );

        if ("ACCEPTED".equals(existingDonation.getStatus())
                && "ACCEPTED".equals(donation.getStatus())) {

            throw new RuntimeException(
                    "This donation has already been accepted."
            );
        }

        String oldStatus = existingDonation.getStatus();

        if (donation.getStatus() != null) {
            existingDonation.setStatus(donation.getStatus());
        }

        Donation savedDonation =
                donationRepository.save(existingDonation);

        if (!"ACCEPTED".equals(oldStatus)
                && "ACCEPTED".equals(savedDonation.getStatus())) {

            createBooksFromDonation(savedDonation);
        }

        return savedDonation;
    }

    private void createBooksFromDonation(Donation donation) {

        List<DonationItem> items =
                donationItemRepository.findByDonationId(
                        donation.getId()
                );

        for (DonationItem item : items) {

            Book book = new Book();

            book.setTitle(item.getBookTitle());
            book.setAuthor(item.getAuthor());
            book.setIsbn(item.getIsbn());
            book.setPublicationDate(item.getPublicationDate());
            book.setDescription(item.getDescription());
            book.setCategory(item.getCategory());

            Book savedBook =
                    bookRepository.save(book);

            BookCopy bookCopy = new BookCopy();

            bookCopy.setCopyNumber(
                    "COPY-" + savedBook.getId() + "-001"
            );

            bookCopy.setBarcode(
                    "DON-" + savedBook.getId()
            );

            bookCopy.setCondition(
                    item.getCondition()
            );

            bookCopy.setStatus("AVAILABLE");

            bookCopy.setBook(savedBook);

            bookCopyRepository.save(bookCopy);
        }
    }
}