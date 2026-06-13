package pl.pwr.miasi.equipmentrental.inventory.domain;

import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.time.Instant;
import java.util.UUID;

public class DamageReport {

    private final UUID id;
    private final String description;
    private final Instant reportedAt;

    public DamageReport(UUID id, String description, Instant reportedAt) {
        if (id == null) {
            throw new BusinessException("Damage report id cannot be null");
        }

        if (description == null || description.isBlank()) {
            throw new BusinessException("Damage report description cannot be empty");
        }

        if (reportedAt == null) {
            throw new BusinessException("Damage report date cannot be null");
        }

        this.id = id;
        this.description = description;
        this.reportedAt = reportedAt;
    }

    public static DamageReport create(String description) {
        return new DamageReport(
                UUID.randomUUID(),
                description,
                Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }
}