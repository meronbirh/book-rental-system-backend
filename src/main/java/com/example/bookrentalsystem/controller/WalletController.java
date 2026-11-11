
package com.example.bookrentalsystem.controller;

import com.example.bookrentalsystem.dto.PaymentResult;
import com.example.bookrentalsystem.entity.Wallet;
import com.example.bookrentalsystem.service.WalletService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@CrossOrigin(
        origins = "http://localhost:4200",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public List<Wallet> getAllWallets() {
        return walletService.getAllWallets();
    }

    @GetMapping("/{id}")
    public Wallet getWalletById(@PathVariable Long id) {
        Wallet wallet = walletService.getWalletById(id);

        if (wallet == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Wallet not found."
            );
        }

        return wallet;
    }

    @PostMapping
    public Wallet createWallet(@RequestBody Wallet wallet) {
        return walletService.createWallet(wallet);
    }

    // Existing top-up endpoint preserved.
    @PostMapping("/{id}/top-up")
    public Wallet topUpWallet(
            @PathVariable Long id,
            @RequestBody TopUpRequest request) {

        if (request == null
                || request.amount == null
                || !Double.isFinite(request.amount)
                || request.amount <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Top-up amount must be greater than zero."
            );
        }

        return walletService.topUpWallet(id, request.amount);
    }

    // New simulated payment endpoint.
    @PostMapping("/{id}/simulate-payment")
    public PaymentResult simulatePayment(
            @PathVariable Long id,
            @RequestBody TopUpRequest request) {

        if (request == null
                || request.amount == null
                || !Double.isFinite(request.amount)
                || request.amount <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Top-up amount must be greater than zero."
            );
        }

        return walletService.simulatePayment(id, request.amount);
    }

    public static class TopUpRequest {
        public Double amount;
    }

    @DeleteMapping("/{id}")
    public void deleteWallet(@PathVariable Long id) {
        walletService.deleteWallet(id);
    }
}
