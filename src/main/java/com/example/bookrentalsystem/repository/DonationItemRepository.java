package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.DonationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DonationItemRepository extends JpaRepository<DonationItem, Long> {
    List<DonationItem> findByDonationId(Long donationId);
}