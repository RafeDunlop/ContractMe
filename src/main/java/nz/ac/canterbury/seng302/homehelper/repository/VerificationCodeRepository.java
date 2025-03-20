package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

/**
 * Repository interface for accessing verfication tokens.
 * Extends Spring Data JPA's {@link CrudRepository} to provide basic CRUD operations
 * for the {@link VerificationCode} entity.
 */
@Repository
public interface VerificationCodeRepository extends CrudRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByCode(String code);

    void deleteByExpiryDateBefore(Date date);
}
