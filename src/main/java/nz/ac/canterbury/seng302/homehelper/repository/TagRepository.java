package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Tag} entities.
 * Extends {@link CrudRepository} to provide basic CRUD operations.
 * @author Rafe Dunlop
 */
@Repository
public interface TagRepository extends CrudRepository<Tag, Long> {
}
