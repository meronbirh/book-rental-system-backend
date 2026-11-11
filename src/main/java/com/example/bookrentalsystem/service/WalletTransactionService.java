package com.example.bookrentalsystem.service;

import com.example.bookrentalsystem.entity.WalletTransaction;
import com.example.bookrentalsystem.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;

    public WalletTransactionService(WalletTransactionRepository walletTransactionRepository) {
        this.walletTransactionRepository = walletTransactionRepository;
    }

    public List<WalletTransaction> getAllTransactions() {
        return walletTransactionRepository.findAll();
    }

    public WalletTransaction getTransactionById(Long id) {
        return walletTransactionRepository.findById(id).orElse(null);
    }

    public WalletTransaction createTransaction(WalletTransaction transaction) {
        return walletTransactionRepository.save(transaction);
    }

    public void deleteTransaction(Long id) {
        walletTransactionRepository.deleteById(id);
    }
}