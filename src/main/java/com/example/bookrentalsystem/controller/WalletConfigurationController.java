
package com.example.bookrentalsystem.controller;

import com.example.bookrentalsystem.entity.WalletConfiguration;
import com.example.bookrentalsystem.service.WalletConfigurationService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet-config")
@CrossOrigin(
        origins = "http://localhost:4200",
        methods = {
                RequestMethod.GET,
                RequestMethod.PUT,
                RequestMethod.OPTIONS
        }
)
public class WalletConfigurationController {

    private final WalletConfigurationService configurationService;

    public WalletConfigurationController(
            WalletConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    // Retrieve the current wallet configuration.
    @GetMapping
    public WalletConfiguration getConfiguration() {
        return configurationService.getConfiguration();
    }

    // Update the wallet configuration.
    @PutMapping
    public WalletConfiguration updateConfiguration(
            @RequestBody WalletConfiguration configuration) {
        return configurationService.updateConfiguration(configuration);
    }
}
