package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationRepository extends JpaRepository<Donation, Long> {

}