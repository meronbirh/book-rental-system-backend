package com.example.bookrentalsystem.service;

import com.example.bookrentalsystem.entity.DonationItem;
import com.example.bookrentalsystem.repository.DonationItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonationItemService {

    private final DonationItemRepository donationItemRepository;

    public DonationItemService(DonationItemRepository donationItemRepository) {
        this.donationItemRepository = donationItemRepository;
    }

    public List<DonationItem> getAllDonationItems() {
        return donationItemRepository.findAll();
    }

    public DonationItem getDonationItemById(Long id) {
        return donationItemRepository.findById(id).orElse(null);
    }

    public DonationItem createDonationItem(DonationItem donationItem) {
        return donationItemRepository.save(donationItem);
    }

    public void deleteDonationItem(Long id) {
        donationItemRepository.deleteById(id);
    }
}