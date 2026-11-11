
package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransaction, Long> {

    // Retrieve all transactions belonging to a wallet.
    List<WalletTransaction> findByWalletId(Long walletId);

    // Find a transaction using its unique payment reference.
    Optional<WalletTransaction> findByPaymentReference(
            String paymentReference
    );
}
