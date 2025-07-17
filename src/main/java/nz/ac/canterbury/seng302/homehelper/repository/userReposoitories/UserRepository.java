package nz.ac.canterbury.seng302.homehelper.repository.userReposoitories;

import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link User} entities.
 * Extends {@link UserBaseRepository} to inherit common query methods,
 * basic CRUD operations from {@link CrudRepository}.
 * This repository handles all users, including any subclasses such as
 * contractors, since JPA single-table inheritance is used.
 */
@Repository
public interface UserRepository extends UserBaseRepository<User> {
}
