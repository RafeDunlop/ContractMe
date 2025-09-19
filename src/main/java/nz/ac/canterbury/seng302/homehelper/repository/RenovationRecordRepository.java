package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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
     * Finds all renovation records owned by the given user
     * @param user The user whose renovation records to retrieve.
     * @param pageable The pagination information, can be {@code null} for unpaged results.
     * @return A {@link Page} of the user's renovation records.
     */
    @Query("SELECT r FROM RenovationRecord r " +
            "WHERE r.user = :user " +
            "ORDER BY r.createdDate DESC")
    Page<RenovationRecord> findUserRecords(@Param("user") User user, @Nullable Pageable pageable);

    /**
     * Finds renovation records owned by the given user that contain the search term
     * in their name or description (case-insensitive)
     * @param user The user whose renovation records to search.
     * @param term The term to search for in name or description.
     * @param pageable The pagination information, can be {@code null} for unpaged results.
     * @return A {@link Page} of the user's renovation records matching the search term.
     */
    @Query("SELECT r FROM RenovationRecord r " +
            "WHERE (" +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :term, '%')) " +
            ") AND r.user = :user " +
            "ORDER BY r.createdDate DESC")
    Page<RenovationRecord> findUserRecordsBySearch(@Param("user") User user, @Param("term") String term,
                                                   @Nullable Pageable pageable);

    /**
     * Finds renovation records owned by the given user that contain at least one of the specified tags
     * @param user The user whose renovation records to search.
     * @param tags A list of tags to filter records by.
     * @param pageable The pagination information, can be {@code null} for unpaged results.
     * @return A {@link Page} of the user's renovation records matching the specified tags.
     */
    @Query("SELECT r FROM RenovationRecord r " +
            "JOIN r.tags t " +
            "WHERE t IN :tags " +
            "AND r.user = :user " +
            "ORDER BY r.createdDate DESC")
    Page<RenovationRecord> findUserRecordsByTag(@Param("user") User user, @Param("tags") List<Tag> tags,
                                                @Nullable Pageable pageable);

    /**
     * Finds renovation records owned by the given user that either match the search term in their
     * name/description or have any of the specified tags
     * @param user The user whose renovation records to search.
     * @param term The search term for name/description.
     * @param tags A list of tags to filter records by.
     * @param pageable The pagination information, can be {@code null} for unpaged results.
     * @return A {@link Page} of the user's renovation records matching the term or tags.
     */
    @Query("SELECT DISTINCT r FROM RenovationRecord r " +
            "LEFT JOIN r.tags t " +
            "WHERE (" +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR t IN :tags" +
            ") AND r.user = :user " +
            "ORDER BY r.createdDate DESC")
    Page<RenovationRecord> findUserRecordsBySearchOrTag(@Param("user") User user,
                                                        @Param("term") String term,
                                                        @Param("tags") List<Tag> tags,
                                                        @Nullable Pageable pageable);


    /**
     * Finds all public renovation records
     * @param pageable The pagination information, can be {@code null} for unpaged results.
     * @return A {@link Page} of public renovation records.
     */
    @Query("SELECT r FROM RenovationRecord r " +
            "WHERE r.isPublic = true " +
            "ORDER BY r.createdDate DESC")
    Page<RenovationRecord> findPublicRecords(@Nullable Pageable pageable);

    /**
     * Finds public renovation records where the name or description matches the given search term (case-insensitive)
     * @param term The search term for name/description.
     * @param pageable The pagination information, can be {@code null} for unpaged results.
     * @return A {@link Page} of public renovation records matching the search term.
     */
    @Query("SELECT DISTINCT r FROM RenovationRecord r " +
            "WHERE (" +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :term, '%')) " +
            ") AND r.isPublic = true " +
            "ORDER BY r.createdDate DESC")
    Page<RenovationRecord> findPublicRecordsBySearch(@Param("term") String term, @Nullable Pageable pageable);

    /**
     * Finds public renovation records that contain at least one of the specified tags
     * @param tags A list of tags to filter records by.
     * @param pageable The pagination information, can be {@code null} for unpaged results.
     * @return A {@link Page} of public renovation records matching the tags.
     */
    @Query("SELECT DISTINCT r FROM RenovationRecord r " +
            "JOIN r.tags t " +
            "WHERE t IN :tags " +
            "AND r.isPublic = true " +
            "ORDER BY r.createdDate DESC")
    Page<RenovationRecord> findPublicRecordsByTag(@Param("tags") List<Tag> tags, @Nullable Pageable pageable);

    /**
     * Finds public renovation records that either match the search term in their
     * name/description or have any of the specified tags
     * @param term The search term for name/description.
     * @param tags A list of tags to filter records by.
     * @param pageable The pagination information, can be {@code null} for unpaged results.
     * @return A {@link Page} of public renovation records matching the term or tags.
     */
    @Query("SELECT DISTINCT r FROM RenovationRecord r " +
            "LEFT JOIN r.tags t " +
            "WHERE (" +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR t IN :tags" +
            ") AND r.isPublic = true " +
            "ORDER BY r.createdDate DESC")
    Page<RenovationRecord> findPublicRecordsBySearchOrTag(@Param("term") String term, @Param("tags") List<Tag> tags,
                                                          @Nullable Pageable pageable);

    /**
     * Finds all renovation records that are either public or owned by the given user
     * @param user The current user to include their private records.
     * @param pageable The pagination information, can be {@code null} for unpaged results.
     * @return A {@link Page} of renovation records visible to the user.
     */
    @Query("SELECT r FROM RenovationRecord r " +
            "WHERE " + "r.isPublic = true OR r.user = :user " +
            "ORDER BY r.createdDate DESC")
    Page<RenovationRecord> findVisibleRecords(@Param("user") User user, @Nullable Pageable pageable);

    /**
     * Finds renovation records visible to the user (their own or public) that contain the given search term
     * in the name or description (case-insensitive)
     * @param user The current user to include their own records.
     * @param term The search term for name/description.
     * @param pageable The pagination information, can be {@code null} for unpaged results.
     * @return A {@link Page} of visible renovation records matching the term.
     */
    @Query("SELECT r FROM RenovationRecord r " +
            "WHERE (" +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :term, '%')) " +
            ") AND (r.isPublic = true OR r.user = :user)" +
            "ORDER BY r.createdDate DESC")
    Page<RenovationRecord> findVisibleRecordsBySearch(@Param("user") User user, @Param("term") String term,
                                                      @Nullable Pageable pageable);

    /**
     * Finds renovation records visible to the user (their own or public) that have at least one of the specified tags
     * @param user The current user to include their own records.
     * @param tags A list of tags to filter records by.
     * @param pageable The pagination information, can be {@code null} for unpaged results.
     * @return A {@link Page} of visible renovation records matching the tags.
     */
    @Query("SELECT r FROM RenovationRecord r " +
            "JOIN r.tags t " +
            "WHERE t IN :tags " +
            "AND (r.isPublic = true OR r.user = :user)" +
            "ORDER BY r.createdDate DESC")
    Page<RenovationRecord> findVisibleRecordsByTag(@Param("user") User user, @Param("tags") List<Tag> tags,
                                                   @Nullable Pageable pageable);

    /**
     * Finds renovation records visible to the user (their own or public) that either match the search term
     * in the name/description or have any of the specified tags
     * @param user The current user to include their own records.
     * @param term The search term for name/description.
     * @param tags A list of tags to filter records by.
     * @param pageable The pagination information, can be {@code null} for unpaged results.
     * @return A {@link Page} of visible renovation records matching the term or tags.
     */
    @Query("SELECT r FROM RenovationRecord r " +
            "LEFT JOIN r.tags t " +
            "WHERE (" +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR t IN :tags" +
            ") AND (r.isPublic = true OR r.user = :user)" +
            "ORDER BY r.createdDate DESC")
    Page<RenovationRecord> findVisibleRecordsBySearchOrTag(@Param("user") User user, @Param("term") String term,
                                                           @Param("tags") List<Tag> tags, @Nullable Pageable pageable);

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
     * Gets renovations within the rectangle represented by the specified pair of coordinates which are owned by the specified
     * @param user The {@link User} whose renovations are retrieved
     * @param minLat Lower bound for latitude of renovations retrieved
     * @param minLon Lower bound for longitude of renovations retrieved
     * @param maxLat Upper bound for latitude of renovations retrieved
     * @param maxLon Upper bound for longitude of renovations retrieved
     * @return Renovations within the rectangle represented by the specified pair of coordinates which are owned by the specified
     */
    @Query("SELECT r FROM RenovationRecord r" +
            " WHERE (r.user) = (:user)" +
            " AND r.location IS NOT NULL" +
            " AND (r.location.latitude) <= (:maxLat)" +
            " AND (r.location.latitude) >= (:minLat)" +
            " AND (r.location.longitude) <= (:maxLon)" +
            " AND (r.location.longitude) >= (:minLon)")
    Collection<RenovationRecord> findOwnedWithinBox(@Param("user") User user,
                                             @Param("minLat") Double minLat,
                                             @Param("minLon") Double minLon,
                                             @Param("maxLat") Double maxLat,
                                             @Param("maxLon") Double maxLon);

    /**
     * Gets renovations within the rectangle represented by the specified pair of coordinates with public publicity
     * @param minLat Lower bound for latitude of renovations retrieved
     * @param minLon Lower bound for longitude of renovations retrieved
     * @param maxLat Upper bound for latitude of renovations retrieved
     * @param maxLon Upper bound for longitude of renovations retrieved
     * @return Renovations within the rectangle represented by the specified pair of coordinates with public publicity
     */
    @Query("SELECT r FROM RenovationRecord r" +
            " WHERE r.isPublic" +
            " AND r.location IS NOT NULL" +
            " AND (r.location.latitude) <= (:maxLat)" +
            " AND (r.location.latitude) >= (:minLat)" +
            " AND (r.location.longitude) <= (:maxLon)" +
            " AND (r.location.longitude) >= (:minLon)")
    Collection<RenovationRecord> findPublicWithinBox(@Param("minLat") Double minLat,
                                                     @Param("minLon") Double minLon,
                                                     @Param("maxLat") Double maxLat,
                                                     @Param("maxLon") Double maxLon);

    /**
     * Deletes a record from the renovations record table by its id. The id cannot be null/
     * @param id The record id
     */
    @Modifying
    @Query("DELETE FROM RenovationRecord f WHERE f.id = :id")
    void deleteById(@Param("id") @Nullable Long id);
}
