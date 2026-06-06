package pl.pwr.miasi.equipmentrental.rental.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.RentalRepository;
import pl.pwr.miasi.equipmentrental.rental.domain.Rental;

import java.util.Optional;
import java.util.UUID;

@Repository
public class RentalRepositoryAdapter implements RentalRepository {

    private final RentalSpringDataRepository springDataRepository;

    public RentalRepositoryAdapter(RentalSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Rental save(Rental rental) {
        RentalJpaEntity entity = toEntity(rental);
        RentalJpaEntity savedEntity = springDataRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Rental> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(this::toDomain);
    }

    private RentalJpaEntity toEntity(Rental rental) {
        return new RentalJpaEntity(
                rental.getId(),
                rental.getReservationId(),
                rental.getUserId(),
                rental.getAssetId(),
                rental.getCheckoutAt(),
                rental.getExpectedReturnAt(),
                rental.getReturnedAt(),
                rental.getStatus()
        );
    }

    private Rental toDomain(RentalJpaEntity entity) {
        return new Rental(
                entity.getId(),
                entity.getReservationId(),
                entity.getUserId(),
                entity.getAssetId(),
                entity.getCheckoutAt(),
                entity.getExpectedReturnAt(),
                entity.getReturnedAt(),
                entity.getStatus()
        );
    }
}