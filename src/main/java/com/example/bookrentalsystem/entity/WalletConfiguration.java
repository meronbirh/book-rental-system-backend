
package com.example.bookrentalsystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "wallet_configuration")
public class WalletConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean topUpsEnabled = true;

    @Column(nullable = false)
    private Double minimumTopUpAmount = 10.0;

    @Column(nullable = false)
    private Double maximumTopUpAmount = 10000.0;

    public WalletConfiguration() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isTopUpsEnabled() {
        return topUpsEnabled;
    }

    public void setTopUpsEnabled(boolean topUpsEnabled) {
        this.topUpsEnabled = topUpsEnabled;
    }

    public Double getMinimumTopUpAmount() {
        return minimumTopUpAmount;
    }

    public void setMinimumTopUpAmount(Double minimumTopUpAmount) {
        this.minimumTopUpAmount = minimumTopUpAmount;
    }

    public Double getMaximumTopUpAmount() {
        return maximumTopUpAmount;
    }

    public void setMaximumTopUpAmount(Double maximumTopUpAmount) {
        this.maximumTopUpAmount = maximumTopUpAmount;
    }
}
