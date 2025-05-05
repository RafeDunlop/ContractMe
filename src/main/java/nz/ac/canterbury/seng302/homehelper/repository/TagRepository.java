package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Tag} entities.
 * Extends {@link CrudRepository} to provide basic CRUD operations.
 * @author Rafe Dunlop
 */
@Repository
public interface TagRepository extends CrudRepository<Tag, Long> {
    /**
     * Finds all tags whose names contain the given substring, case-insensitive.
     * @param name of the substring
     * @return a list of all matching tags like the given name
     */
    @Query("SELECT t FROM Tag t WHERE LOWER(t.tagName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Tag> findByNameContainingIgnoreCase(@Param("name") String name);


    @Query("SELECT f FROM Tag f WHERE (f.tagName) = (:name)")
    Optional<Tag> findExactMatchTagByTagName(@Param("name") String name);
}
