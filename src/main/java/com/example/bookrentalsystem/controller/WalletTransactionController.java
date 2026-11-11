package com.example.bookrentalsystem.controller;

import com.example.bookrentalsystem.entity.WalletTransaction;
import com.example.bookrentalsystem.service.WalletTransactionService;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/wallet-transactions")
public class WalletTransactionController {

    private final WalletTransactionService walletTransactionService;

    public WalletTransactionController(WalletTransactionService walletTransactionService) {
        this.walletTransactionService = walletTransactionService;
    }

    @GetMapping
    public List<WalletTransaction> getAllTransactions() {
        return walletTransactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    public WalletTransaction getTransactionById(@PathVariable Long id) {
        return walletTransactionService.getTransactionById(id);
    }

    @PostMapping
    public WalletTransaction createTransaction(@RequestBody WalletTransaction transaction) {
        return walletTransactionService.createTransaction(transaction);
    }

    @DeleteMapping("/{id}")
    public void deleteTransaction(@PathVariable Long id) {
        walletTransactionService.deleteTransaction(id);
    }
}