package nz.ac.canterbury.seng302.homehelper.repository.userReposoitories;

import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing user data.
 * Extends Spring Data JPA's {@link CrudRepository} to provide basic CRUD operations
 * for the {@link User} entity. Additional methods for retrieving users by email and
 * password are defined here.
 */
@Repository
public interface UserRepository extends UserBaseRepository<User> {
}
