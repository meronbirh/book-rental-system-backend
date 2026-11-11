
package com.example.bookrentalsystem.dto;

public class PaymentResult {

    private String status;
    private String message;
    private String paymentReference;
    private Double walletBalance;

    public PaymentResult() {
    }

    public PaymentResult(
            String status,
            String message,
            String paymentReference,
            Double walletBalance) {

        this.status = status;
        this.message = message;
        this.paymentReference = paymentReference;
        this.walletBalance = walletBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public Double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(Double walletBalance) {
        this.walletBalance = walletBalance;
    }
}
