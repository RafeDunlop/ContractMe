package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Finds all renovation records where the current user on the application matches the owner of the renovation.
     * @param user The current user
     * @return A list of all the renovation records from the user
     */
    @Query("SELECT f FROM RenovationRecord f " +
            "WHERE f.user = :user " +
            "ORDER BY f.createdDate DESC")
    List<RenovationRecord> findByUser(@Param("user") User user);

    /**
     * Finds all of user's renovation records not case-sensitive that are like the given string
     * @param user The current user
     * @param term to search for records like it
     * @return list of all of user's records containing the string in its name or description
     */
    @Query("SELECT r FROM RenovationRecord r " +
            "WHERE " + "(LOWER(r.name) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR " + "LOWER(r.description) LIKE LOWER(CONCAT('%', :term, '%'))) " +
            "AND " + "r.user = :user " +
            "ORDER BY r.createdDate DESC")
    List<RenovationRecord> findByUserTrueSearchContainingNameOrDescriptionIgnoreCase(@Param("user") User user, @Param("term") String term);

    @Query("SELECT r FROM RenovationRecord r " +
            "WHERE " + "(LOWER(r.name) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR " + "LOWER(r.description) LIKE LOWER(CONCAT('%', :term, '%'))) " +
            "AND " + "r.user = :user " +
            "ORDER BY r.createdDate DESC")
    Page<RenovationRecord> searchNameOrDescriptionContainingIgnoreCasePaginated(@Param("user") User user, @Param("term") String term, @Nullable Pageable pageable);

    /**
     * Finds all public renovation records
     * @return A list of all the public renovation records
     */
    @Query("SELECT f FROM RenovationRecord f " +
            "WHERE f.isPublic = true " +
            "ORDER BY f.createdDate DESC")
    List<RenovationRecord> findByIsPublicTrue();
    /**
     * Finds all public renovation records not case-sensitive that are like the given string
     * @param term to search for records like it
     * @return list of all public records containing the string in its name
     */
    @Query("SELECT r FROM RenovationRecord r " +
            "WHERE " + "(LOWER(r.name) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR " + "LOWER(r.description) LIKE LOWER(CONCAT('%', :term, '%'))) " +
            "AND " + "r.isPublic = true " +
            "ORDER BY r.createdDate DESC")
    List<RenovationRecord> findByIsPublicTrueSearchContainingNameOrDescriptionIgnoreCase(@Param("term") String term);

    /**
     * Finds all public or user's own renovation records
     * @param user The current user
     * @return A list of all public or user's own renovation records
     */
    @Query("SELECT f FROM RenovationRecord f " +
            "WHERE " + "f.isPublic = true OR f.user = :user " +
            "ORDER BY f.createdDate DESC")
    List<RenovationRecord> findAllVisibleToUser(@Param("user") User user);

    /**
     * Finds all public or user's own renovation records not case-sensitive that are like the given string
     * @param user The current user
     * @param term to search for records like it
     * @return list of all public or user's own renovation records containing the string in its name
     */
    @Query("SELECT r FROM RenovationRecord r " +
            "WHERE " + "(LOWER(r.name) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR " + "LOWER(r.description) LIKE LOWER(CONCAT('%', :term, '%'))) " +
            "AND " + "(r.isPublic = true OR r.user = :user) " +
            "ORDER BY r.createdDate DESC")
    List<RenovationRecord> findAllVisibleToUserSearchContainingNameOrDescriptionIgnoreCase(@Param("user") User user, @Param("term") String term);


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
    Page<RenovationRecord> findByUser(@Param("user") User user, @Nullable Pageable pageable);

    /**
     * Deletes a record from the renovations record table by its id. The id cannot be null/
     * @param id The record id
     */
    @Modifying
    @Query("DELETE FROM RenovationRecord f WHERE f.id = :id")
    void deleteById(@Param("id") @Nullable Long id);

    /**
     * Finds all records with OR logic with matching tags.
     * @param tags objects in a list to search for
     * @return list of renovation records matching the provided tags
     */
    @Query("SELECT r FROM RenovationRecord r JOIN r.tags t WHERE t IN :tags")
    List<RenovationRecord> findAllByTags(@Param("tags") List<Tag> tags);

    /**
     * Finds all public records with OR logic for matching tags
     * It is primarily ordered by number of matching tags, then secondary matched by date created.
     * @param tags the list of tags matching the query.
     * @return list of tags found.
     */
    @Query("SELECT r FROM RenovationRecord r " +
            "JOIN r.tags t " +
            "WHERE r.isPublic = true AND t IN :tags " +
            "GROUP BY r " +
            "ORDER BY COUNT(t) DESC, r.createdDate DESC")
    List<RenovationRecord> findAllPublicByTagsOrderByTagCountAndDate(@Param("tags") List<Tag> tags);

}
