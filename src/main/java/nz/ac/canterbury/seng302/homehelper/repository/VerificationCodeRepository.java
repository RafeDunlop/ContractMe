package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

/**
 * Repository interface for accessing {@link VerificationCode} entities.
 * Extends Spring Data JPA's {@link CrudRepository} to provide basic CRUD operations
 * for the {@link VerificationCode} entity.
 */
@Repository
public interface VerificationCodeRepository extends CrudRepository<VerificationCode, Long> {

    /**
     * Gets the {@link VerificationCode} object which corresponds to the specified {@code String} code
     * @param code The {@code String} code to get the {@link VerificationCode} entity of
     * @return an {@code Optional} of the {@link VerificationCode} entity, which is empty if none was found
     */
    Optional<VerificationCode> findByCode(String code);

}
