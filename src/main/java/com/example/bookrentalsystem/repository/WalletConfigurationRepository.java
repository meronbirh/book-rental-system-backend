
package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.WalletConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletConfigurationRepository
        extends JpaRepository<WalletConfiguration, Long> {
}
