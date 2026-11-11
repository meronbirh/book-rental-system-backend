
package com.example.bookrentalsystem.controller;

import com.example.bookrentalsystem.entity.Rental;
import com.example.bookrentalsystem.service.RentalService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDate;
import com.example.bookrentalsystem.dto.RentalReturnResponse;

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
@RequestMapping("/rentals")
public class RentalController {
    private final RentalService rentalService;
    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }
    // Get all rentals
    @GetMapping
    public List<Rental> getAllRentals() {
        return rentalService.getAllRentals();
    }

    // Get one rental
    @GetMapping("/{id}")
    public Rental getRentalById(@PathVariable Long id) {
        Rental rental = rentalService.getRentalById(id);

        if (rental == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Rental not found."
            );
        }

        return rental;
    }

    // Rent a book immediately without approval
    @PostMapping
    public Rental createRental(@RequestBody Rental rental) {
        return rentalService.createRental(rental);
    }

    // Return a rented book
    // Return a rented book and provide a summary of the result.
    @PutMapping("/{id}/return")
    public RentalReturnResponse returnRental(@PathVariable Long id) {
        return rentalService.returnRental(id);
    }

    // Delete a rental
    @DeleteMapping("/{id}")
    public void deleteRental(@PathVariable Long id) {
        rentalService.deleteRental(id);
    }
}
