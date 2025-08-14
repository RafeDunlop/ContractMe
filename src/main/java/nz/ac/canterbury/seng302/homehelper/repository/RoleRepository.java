package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * A repository for managing and retrieving roles.
 * Extends {@link CrudRepository} to provide basic CRUD operations. This can be used to retrieve outstanding role
 * requests which have been assigned to particular contractors.
 */
public interface RoleRepository extends CrudRepository<Role, Long> {

    /**
     * Find roles assigned to a given contractor which have or have not yet been accepted.
     *
     * @param contractor the contractor assigned to the roles
     * @param accepted whether the contractor has accepted the role
     * @return the list of matching roles
     */
    List<Role> findByContractorAndAccepted(Contractor contractor, boolean accepted);
}
