package nz.ac.canterbury.seng302.homehelper.repository.userReposoitories;

import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * A generic base repository interface for all user-related entities.
 * This interface provides common data access operations for entities
 * that extend {@link User}, including a shared method for finding
 * users by email (case-insensitive).
 * Extends Spring Data JPA's {@link CrudRepository} to provide basic CRUD operations
 * Tt is intended to be extended by more specific repositories
 * such as {@code UserRepository} or {@code ContractorRepository}.
 */
@NoRepositoryBean
public interface UserBaseRepository<T extends User> extends CrudRepository<T, Long> {

    Optional<T> findByEmailIgnoreCase(String email);

}
