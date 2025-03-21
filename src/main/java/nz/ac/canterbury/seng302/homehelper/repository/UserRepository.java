package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for accessing user data.
 * Extends Spring Data JPA's {@link CrudRepository} to provide basic CRUD operations
 * for the {@link User} entity. Additional methods for retrieving users by email and
 * password are defined here.
 */

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    // Search database for users by email
    Optional<User> findByEmailIgnoreCase(String email);
}
