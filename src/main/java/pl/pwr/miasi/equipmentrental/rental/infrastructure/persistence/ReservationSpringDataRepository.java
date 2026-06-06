package pl.pwr.miasi.equipmentrental.rental.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.pwr.miasi.equipmentrental.rental.domain.ReservationStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface ReservationSpringDataRepository extends JpaRepository<ReservationJpaEntity, UUID> {

    @Query("""
            select count(r)
            from ReservationJpaEntity r
            where r.assetId = :assetId
              and r.status in :statuses
              and r.periodFrom < :periodTo
              and r.periodTo > :periodFrom
            """)
    long countOverlappingReservations(
            UUID assetId,
            Collection<ReservationStatus> statuses,
            Instant periodFrom,
            Instant periodTo
    );
}