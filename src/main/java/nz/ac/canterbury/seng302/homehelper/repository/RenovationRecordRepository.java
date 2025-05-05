package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link RenovationRecord} entities.
 * Extends {@link CrudRepository} to provide basic CRUD operations.
 * @author Jake Connolly
 */
@Repository
public interface RenovationRecordRepository extends CrudRepository<RenovationRecord, Long> {
    /**
     *  Finds a record from the repository by id
     * @param id of the record to find
     * @return an Optional containing the renovation record if it exists
     */
    Optional<RenovationRecord> findById(long id);

    /**
     * Retrieves all renovation records
     * @return list of all renovation records
     */
    List<RenovationRecord> findAll();

    /**
     * Gets all renovation records not case-sensitive that are like the given string
     * @param user The current user
     * @param term to search for records like it
     * @return list off all records containing the string in its name
     */
    @Query("SELECT r FROM RenovationRecord r WHERE (LOWER(r.name) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :term, '%'))) AND r.user = :user")
    List<RenovationRecord> searchNameOrDescriptionContainingIgnoreCase(@Param("user") User user, @Param("term") String term);

    /**
     * Finds a renovation record with a matching name not case-sensitive if it exists.
     * Searches all records
     * @param name of the record being searched for
     * @return an Optional that is the matching record if it exists
     */
    @Query("SELECT f FROM RenovationRecord f WHERE (f.name) = (:name)")
    Optional<RenovationRecord> findExactMatchAllUsers(@Param("name") String name);

    /**
     * Finds a renovation record with a matching name not case-sensitive if it exists. Only searches
     * {@link RenovationRecord} objects owned by teh specified user
     * @param name of the record being searched for
     * @param user  The {@link User} whose {@link RenovationRecord} objects should be searched
     */
    @Query("SELECT f FROM RenovationRecord f WHERE (f.name) = (:name) AND (f.user) = (:user)")
    Optional<RenovationRecord> findExactMatch(@Param("name") String name, @Param("user") User user);

    /**
     * Finds all renovation records where the current user on the application matches the owner of the renovation.
     * @param user The current user
     * @return A list of all the renovation records from the user
     */
    @Query("SELECT f FROM RenovationRecord f WHERE (f.user) = (:user)")
    List<RenovationRecord> findByUser(@Param("user") User user);

    /**
     * Deletes a record from the renovations record table by its id. The id cannot be null/
     * @param id The record id
     */
    @Modifying
    @Query("DELETE FROM RenovationRecord f WHERE f.id = :id")
    void deleteById(@Param("id") @Nullable Long id);
}