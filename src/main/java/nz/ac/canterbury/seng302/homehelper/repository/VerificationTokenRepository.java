package nz.ac.canterbury.seng302.homehelper.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import nz.ac.canterbury.seng302.homehelper.entity.VerificationToken;

/**
 * Repository interface for accessing verfication tokens.
 * Extends Spring Data JPA's {@link CrudRepository} to provide basic CRUD operations
 * for the {@link VerificationToken} entity.
 */
@Repository
public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long> {

}

