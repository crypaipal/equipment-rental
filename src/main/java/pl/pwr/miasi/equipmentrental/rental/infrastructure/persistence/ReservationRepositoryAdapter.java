package pl.pwr.miasi.equipmentrental.rental.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.domain.RentalPeriod;
import pl.pwr.miasi.equipmentrental.rental.domain.Reservation;
import pl.pwr.miasi.equipmentrental.rental.domain.ReservationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReservationRepositoryAdapter implements ReservationRepository {

    private final ReservationSpringDataRepository springDataRepository;

    public ReservationRepositoryAdapter(ReservationSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationJpaEntity entity = toEntity(reservation);
        ReservationJpaEntity savedEntity = springDataRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public boolean existsActiveReservationForAsset(UUID assetId, RentalPeriod rentalPeriod) {
        List<ReservationStatus> activeStatuses = List.of(
                ReservationStatus.PENDING,
                ReservationStatus.APPROVED,
                ReservationStatus.FULFILLED
        );

        long count = springDataRepository.countOverlappingReservations(
                assetId,
                activeStatuses,
                rentalPeriod.from(),
                rentalPeriod.to()
        );

        return count > 0;
    }

    @Override
    public List<UUID> findReservedAssetIds(RentalPeriod rentalPeriod) {
        List<ReservationStatus> activeStatuses = List.of(
                ReservationStatus.PENDING,
                ReservationStatus.APPROVED,
                ReservationStatus.FULFILLED
        );

        return springDataRepository.findReservedAssetIds(
                activeStatuses,
                rentalPeriod.from(),
                rentalPeriod.to()
        );
    }

    @Override
    public List<Reservation> findAll() {
        return springDataRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private ReservationJpaEntity toEntity(Reservation reservation) {
        return new ReservationJpaEntity(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getAssetId(),
                reservation.getRentalPeriod().from(),
                reservation.getRentalPeriod().to(),
                reservation.getStatus(),
                reservation.getRejectionReason(),
                reservation.getCreatedAt()
        );
    }

    private Reservation toDomain(ReservationJpaEntity entity) {
        return new Reservation(
                entity.getId(),
                entity.getUserId(),
                entity.getAssetId(),
                new RentalPeriod(entity.getPeriodFrom(), entity.getPeriodTo()),
                entity.getStatus(),
                entity.getRejectionReason(),
                entity.getCreatedAt()
        );
    }
}