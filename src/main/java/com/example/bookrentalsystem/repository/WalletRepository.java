package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Wallet findByMemberId(Long memberId);
}