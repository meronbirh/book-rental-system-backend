
package com.example.bookrentalsystem.controller;

import com.example.bookrentalsystem.entity.FineWaiverRequest;
import com.example.bookrentalsystem.entity.WalletTransaction;
import com.example.bookrentalsystem.service.FineWaiverService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(
        origins = "http://localhost:4200",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.OPTIONS
        }
)
@RestController
@RequestMapping("/fine-waivers")
public class FineWaiverController {

    private final FineWaiverService fineWaiverService;

    public FineWaiverController(FineWaiverService fineWaiverService) {
        this.fineWaiverService = fineWaiverService;
    }

    // STAFF: Get fines that do not already have waiver requests.
    @GetMapping("/eligible-fines/{staffUserId}")
    public List<WalletTransaction> getEligibleFineTransactions(
            @PathVariable Long staffUserId) {

        return fineWaiverService.getEligibleFineTransactions(staffUserId);
    }

    // STAFF: Submit a fine-waiver request.
    @PostMapping
    public FineWaiverRequest submitRequest(
            @RequestParam Long fineTransactionId,
            @RequestParam Long staffUserId,
            @RequestParam Double requestedAmount,
            @RequestParam String reason) {

        return fineWaiverService.submitRequest(
                fineTransactionId,
                staffUserId,
                requestedAmount,
                reason
        );
    }

    // STAFF: View their submitted requests.
    @GetMapping("/staff/{staffUserId}")
    public List<FineWaiverRequest> getStaffRequests(
            @PathVariable Long staffUserId) {

        return fineWaiverService.getRequestsByStaff(staffUserId);
    }

    // OWNER: View pending requests.
    @GetMapping("/pending/{ownerUserId}")
    public List<FineWaiverRequest> getPendingRequests(
            @PathVariable Long ownerUserId) {

        return fineWaiverService.getPendingRequests(ownerUserId);
    }

    // OWNER: Approve or reject a request.
    @PutMapping("/{requestId}/review")
    public FineWaiverRequest reviewRequest(
            @PathVariable Long requestId,
            @RequestParam Long ownerUserId,
            @RequestParam String decision,
            @RequestParam(required = false) String reviewComment) {

        return fineWaiverService.reviewRequest(
                requestId,
                ownerUserId,
                decision,
                reviewComment
        );
    }

    // OWNER: View reviewed request history.
    @GetMapping("/history/{ownerUserId}")
    public ResponseEntity<?> getHistoryRequests(
            @PathVariable Long ownerUserId) {

        return ResponseEntity.ok(
                fineWaiverService.getHistoryRequests(ownerUserId)
        );
    }
}
