
package com.example.bookrentalsystem.service;

import com.example.bookrentalsystem.entity.WalletConfiguration;
import com.example.bookrentalsystem.repository.WalletConfigurationRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WalletConfigurationService {

    private final WalletConfigurationRepository configurationRepository;

    public WalletConfigurationService(
            WalletConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @Transactional
    public WalletConfiguration getConfiguration() {
        List<WalletConfiguration> configurations =
                configurationRepository.findAll();

        if (configurations.isEmpty()) {
            WalletConfiguration configuration =
                    new WalletConfiguration();

            configuration.setTopUpsEnabled(true);
            configuration.setMinimumTopUpAmount(10.0);
            configuration.setMaximumTopUpAmount(10000.0);

            return configurationRepository.save(configuration);
        }

        if (configurations.size() > 1) {
            throw new IllegalStateException(
                    "Multiple wallet configurations exist. "
                            + "Please resolve the duplicate records."
            );
        }

        return configurations.get(0);
    }

    @Transactional
    public WalletConfiguration updateConfiguration(
            WalletConfiguration updatedConfiguration) {

        if (updatedConfiguration == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Configuration data is required."
            );
        }

        Double minimum = updatedConfiguration.getMinimumTopUpAmount();
        Double maximum = updatedConfiguration.getMaximumTopUpAmount();

        if (minimum == null
                || !Double.isFinite(minimum)
                || minimum <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum top-up must be greater than zero."
            );
        }

        if (maximum == null
                || !Double.isFinite(maximum)
                || maximum < minimum) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum top-up must be greater than or equal "
                            + "to the minimum top-up."
            );
        }

        WalletConfiguration configuration = getConfiguration();

        configuration.setTopUpsEnabled(
                updatedConfiguration.isTopUpsEnabled()
        );
        configuration.setMinimumTopUpAmount(minimum);
        configuration.setMaximumTopUpAmount(maximum);

        return configurationRepository.save(configuration);
    }
}
