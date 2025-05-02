package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing {@link Tag} entities.
 * Extends {@link CrudRepository} to provide basic CRUD operations.
 * @author Rafe Dunlop
 */
@Repository
public interface TagRepository extends CrudRepository<Tag, Long> {

    @Query("SELECT t FROM Tag t WHERE LOWER(t.tagName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Tag> findByNameContainingIgnoreCase(@Param("name") String name);

}
