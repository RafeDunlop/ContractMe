package nz.ac.canterbury.seng302.homehelper.repository.userRepositories;

import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository interface for managing {@link Contractor} entities.
 * Extends {@link UserBaseRepository} to inherit common query methods,
 * basic CRUD operations from {@link CrudRepository}.
 * This repository handles Contractors
 */
public interface ContractorRepository extends UserBaseRepository<Contractor> {

}
