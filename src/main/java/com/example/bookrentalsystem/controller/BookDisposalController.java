package com.example.bookrentalsystem.controller;
import com.example.bookrentalsystem.entity.BookDisposalRequest;
import com.example.bookrentalsystem.service.BookDisposalService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/book-disposal-requests")
@CrossOrigin(origins = "http://localhost:4200")
public class BookDisposalController {
    private final BookDisposalService bookDisposalService;
    public BookDisposalController(
            BookDisposalService bookDisposalService) {
        this.bookDisposalService = bookDisposalService;
    }
    // STAFF submits a disposal request.
    @PostMapping
    public BookDisposalRequest submitRequest(
            @RequestBody Map<String, String> request) {
        Long bookCopyId = Long.valueOf(request.get("bookCopyId"));
        Long staffUserId = Long.valueOf(request.get("staffUserId"));
        String reason = request.get("reason");
        return bookDisposalService.submitRequest(
                bookCopyId, staffUserId, reason);
    }
    // STAFF views their own requests.
    @GetMapping("/staff/{staffUserId}")
    public List<BookDisposalRequest> getStaffRequests(
            @PathVariable Long staffUserId) {
        return bookDisposalService.getStaffRequests(staffUserId);
    }
    // OWNER views requests awaiting a decision.
    @GetMapping("/pending/{ownerUserId}")
    public List<BookDisposalRequest> getPendingRequests(
            @PathVariable Long ownerUserId) {
        return bookDisposalService.getPendingRequests(ownerUserId);
    }
    // OWNER views all requests, including completed reviews.
    @GetMapping("/history/{ownerUserId}")
    public List<BookDisposalRequest> getAllRequests(
            @PathVariable Long ownerUserId) {

        return bookDisposalService.getAllRequests(ownerUserId);
    }
    // OWNER gets actual request counts for report cards.
    @GetMapping("/summary/{ownerUserId}")
    public Map<String, Long> getRequestSummary(
            @PathVariable Long ownerUserId) {
        return bookDisposalService.getRequestSummary(ownerUserId);
    }
    // OWNER gets book and inventory details for one request.
    @GetMapping("/{requestId}/inventory/{ownerUserId}")
    public Map<String, Object> getInventoryDetails(
            @PathVariable Long requestId,
            @PathVariable Long ownerUserId) {

        return bookDisposalService.getInventoryDetails(
                ownerUserId, requestId);
    }
    // OWNER approves or rejects a request.
    @PutMapping("/{requestId}/review")
    public BookDisposalRequest reviewRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> request) {
        Long ownerUserId = Long.valueOf(request.get("ownerUserId"));
        String decision = request.get("decision");
        String reviewComment = request.get("reviewComment");
        return bookDisposalService.reviewRequest(
                requestId, ownerUserId, decision, reviewComment);
    }
}
