
package com.example.bookrentalsystem.service;

import com.example.bookrentalsystem.dto.PaymentResult;
import com.example.bookrentalsystem.entity.Rental;
import com.example.bookrentalsystem.entity.Wallet;
import com.example.bookrentalsystem.entity.WalletConfiguration;
import com.example.bookrentalsystem.entity.WalletTransaction;
import com.example.bookrentalsystem.repository.WalletRepository;
import com.example.bookrentalsystem.repository.WalletTransactionRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletConfigurationService configurationService;

    @Value("${wallet.payment.success-rate:0.9}")
    private double paymentSuccessRate;

    public WalletService(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            WalletConfigurationService configurationService) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.configurationService = configurationService;
    }

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id).orElse(null);
    }

    public Wallet createWallet(Wallet wallet) {
        if (wallet.getBalance() == null) {
            wallet.setBalance(0.0);
        }

        if (!Double.isFinite(wallet.getBalance()) || wallet.getBalance() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Wallet balance cannot be negative or invalid."
            );
        }

        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet topUpWallet(Long walletId, Double amount) {
        validateAmount(amount);
        validateTopUpConfiguration(amount);

        Wallet wallet = findWallet(walletId);
        double currentBalance =
                wallet.getBalance() == null ? 0.0 : wallet.getBalance();

        double newBalance = currentBalance + amount;

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setAmount(amount);
        transaction.setType("TOP_UP");
        transaction.setStatus("SUCCESS");
        transaction.setTransactionDate(LocalDate.now());
        transaction.setWallet(wallet);

        // Save receipt balance details.
        transaction.setPreviousBalance(currentBalance);
        transaction.setResultingBalance(newBalance);

        walletTransactionRepository.save(transaction);

        return wallet;
    }

    @Transactional
    public PaymentResult simulatePayment(Long walletId, Double amount) {
        validateAmount(amount);
        validateTopUpConfiguration(amount);

        if (!Double.isFinite(paymentSuccessRate)
                || paymentSuccessRate < 0
                || paymentSuccessRate > 1) {
            throw new IllegalStateException(
                    "Payment success rate must be between 0 and 1."
            );
        }

        Wallet wallet = findWallet(walletId);
        double currentBalance =
                wallet.getBalance() == null ? 0.0 : wallet.getBalance();

        String paymentReference =
                "PAY-" + UUID.randomUUID().toString().toUpperCase();

        WalletTransaction transaction = new WalletTransaction();
        transaction.setAmount(amount);
        transaction.setType("TOP_UP");
        transaction.setTransactionDate(LocalDate.now());
        transaction.setWallet(wallet);
        transaction.setPaymentReference(paymentReference);

        // Record the balance before processing the payment.
        transaction.setPreviousBalance(currentBalance);

        boolean paymentSucceeded = Math.random() < paymentSuccessRate;

        if (!paymentSucceeded) {
            transaction.setStatus("FAILED");
            transaction.setFailureReason(
                    "Payment failed during simulated processing."
            );

            // The wallet balance is unchanged.
            transaction.setResultingBalance(currentBalance);

            walletTransactionRepository.save(transaction);

            return new PaymentResult(
                    "FAILED",
                    "Payment failed. Your wallet balance has not changed.",
                    paymentReference,
                    currentBalance
            );
        }

        double newBalance = currentBalance + amount;

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        transaction.setStatus("SUCCESS");
        transaction.setFailureReason(null);
        transaction.setResultingBalance(newBalance);

        WalletTransaction savedTransaction =
                walletTransactionRepository.save(transaction);

        return new PaymentResult(
                "SUCCESS",
                "Payment completed successfully. Your wallet has been credited.",
                savedTransaction.getPaymentReference(),
                wallet.getBalance()
        );
    }

    @Transactional
    public WalletTransaction processRentalPayment(
            Long walletId, Double amount, Rental rental) {

        if (amount == null || !Double.isFinite(amount) || amount <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rental payment amount must be greater than zero."
            );
        }

        if (rental == null || rental.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A saved rental is required for the payment."
            );
        }

        Wallet wallet = findWallet(walletId);
        double currentBalance =
                wallet.getBalance() == null ? 0.0 : wallet.getBalance();

        if (currentBalance < amount) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient wallet balance to pay the rental fee."
            );
        }

        wallet.setBalance(currentBalance - amount);
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setAmount(amount);
        transaction.setType("RENTAL_PAYMENT");
        transaction.setStatus("SUCCESS");
        transaction.setTransactionDate(LocalDate.now());
        transaction.setPaymentReference(
                "RENTAL-" + UUID.randomUUID().toString().toUpperCase()
        );
        transaction.setFailureReason(null);
        transaction.setWallet(wallet);
        transaction.setRental(rental);

        // Keep balance details for this transaction too.
        transaction.setPreviousBalance(currentBalance);
        transaction.setResultingBalance(wallet.getBalance());

        return walletTransactionRepository.save(transaction);
    }

    private void validateTopUpConfiguration(Double amount) {
        WalletConfiguration configuration =
                configurationService.getConfiguration();

        if (!configuration.isTopUpsEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Wallet top-ups are currently disabled by the administrator."
            );
        }

        double minimum = configuration.getMinimumTopUpAmount();
        double maximum = configuration.getMaximumTopUpAmount();

        if (amount < minimum) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Top-up amount must be at least " + minimum + "."
            );
        }

        if (amount > maximum) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Top-up amount cannot exceed " + maximum + "."
            );
        }
    }

    private void validateAmount(Double amount) {
        if (amount == null || !Double.isFinite(amount) || amount <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Top-up amount must be greater than zero."
            );
        }
    }

    private Wallet findWallet(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Wallet not found."
                        )
                );
    }

    @Transactional
    public void deleteWallet(Long id) {
        if (!walletRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Wallet not found."
            );
        }

        walletRepository.deleteById(id);
    }
}
