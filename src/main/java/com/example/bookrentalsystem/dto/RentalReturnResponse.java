
package com.example.bookrentalsystem.dto;

import com.example.bookrentalsystem.entity.Rental;

public record RentalReturnResponse(
        Rental rental,
        boolean overdue,
        long overdueDays,
        double fineAmountDeducted,
        Double walletBalanceAfter,
        boolean alreadyReturned
) {
}
