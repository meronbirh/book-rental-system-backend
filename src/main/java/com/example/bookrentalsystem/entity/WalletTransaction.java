
package com.example.bookrentalsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private String type;

    private LocalDate transactionDate;

    // Payment outcome: SUCCESS or FAILED.
    private String status = "SUCCESS";

    // Unique reference identifying the payment, when available.
    @Column(unique = true)
    private String paymentReference;

    // Explanation provided when a payment fails.
    @Column(length = 500)
    private String failureReason;

    // Wallet balance immediately before this transaction.
    // Populated for new top-up transactions.
    private Double previousBalance;

    // Wallet balance immediately after this transaction.
    // Populated for new top-up transactions.
    private Double resultingBalance;

    @ManyToOne
    private Wallet wallet;

    // Rental associated with this transaction, when applicable.
    @ManyToOne
    @JoinColumn(name = "rental_id", nullable = true)
    private Rental rental;

    public WalletTransaction() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Double getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(Double previousBalance) {
        this.previousBalance = previousBalance;
    }

    public Double getResultingBalance() {
        return resultingBalance;
    }

    public void setResultingBalance(Double resultingBalance) {
        this.resultingBalance = resultingBalance;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Rental getRental() {
        return rental;
    }

    public void setRental(Rental rental) {
        this.rental = rental;
    }
}
