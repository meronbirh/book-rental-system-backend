 package com.example.bookrentalsystem.controller;

import com.example.bookrentalsystem.entity.DonationItem;
import com.example.bookrentalsystem.service.DonationItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donation-items")
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
public class DonationItemController {

    private final DonationItemService donationItemService;

    public DonationItemController(DonationItemService donationItemService) {
        this.donationItemService = donationItemService;
    }

    @GetMapping
    public List<DonationItem> getAllDonationItems() {
        return donationItemService.getAllDonationItems();
    }

    @GetMapping("/{id}")
    public DonationItem getDonationItemById(@PathVariable Long id) {
        return donationItemService.getDonationItemById(id);
    }

    @PostMapping
    public DonationItem createDonationItem(@RequestBody DonationItem donationItem) {
        return donationItemService.createDonationItem(donationItem);
    }

    @DeleteMapping("/{id}")
    public void deleteDonationItem(@PathVariable Long id) {
        donationItemService.deleteDonationItem(id);
    }
}
