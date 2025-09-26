package nz.ac.canterbury.seng302.homehelper.repository.userRepositories;

import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for managing {@link Contractor} entities.
 * Extends {@link UserBaseRepository} to inherit common query methods,
 * basic CRUD operations from {@link CrudRepository}.
 * This repository handles Contractors
 */
public interface ContractorRepository extends UserBaseRepository<Contractor> {
    /**
     * Finds the nearest available contractor with the given skill, within a specified maximum distance
     * from a given latitude and longitude, excluding a set of contractor IDs.
     * The search uses the Haversine formula to calculate distances in kilometers.
     * Contractors must be marked as available and have the specified skill.
     * If {@code excludedIds} is {@code null} or empty, no IDs are excluded.
     *
     * @param lat         Latitude of the reference location.
     * @param lon         Longitude of the reference location.
     * @param skill       Required skill of the contractor.
     * @param maxDistance Maximum allowed distance (in kilometers) from the reference location.
     * @param excludedIds Set of contractor IDs to exclude from the search. Should not be null, use
     *                    findNearestWithinDistance in that case.
     * @return The nearest matching {@link Contractor}, or {@code null} if none found within the distance.
     */
    @Query(value = """
                SELECT c.*,
                (6371 * acos(
                    cos(radians(:lat)) * cos(radians(c.latitude)) *
                    cos(radians(c.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(c.latitude))
                )) AS distance
                FROM user_details c
                JOIN contractor_skills s ON c.user_id = s.contractor_id
                WHERE s.skill = :requiredSkill
                  AND c.available = true
                  AND (c.user_id NOT IN (:excludedIds))
                  AND (6371 * acos(
                    cos(radians(:lat)) * cos(radians(c.latitude)) *
                    cos(radians(c.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(c.latitude))
                )) <= :maxDistance
                ORDER BY distance ASC
                LIMIT 1
            """, nativeQuery = true)
    Contractor findNearestWithinDistanceExcluding(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("requiredSkill") String skill,
            @Param("maxDistance") double maxDistance,
            @Param("excludedIds") java.util.Set<Long> excludedIds
    );

    /**
     * Finds the nearest available contractor with the given skill, within a specified maximum distance
     * from a given latitude and longitude.
     * The search uses the Haversine formula to calculate distances in kilometers.
     * Contractors must be marked as available and have the specified skill.
     * If {@code excludedIds} is {@code null} or empty, no IDs are excluded.
     *
     * @param lat         Latitude of the reference location.
     * @param lon         Longitude of the reference location.
     * @param skill       Required skill of the contractor.
     * @param maxDistance Maximum allowed distance (in kilometers) from the reference location.
     * @return The nearest matching {@link Contractor}, or {@code null} if none found within the distance.
     */
    @Query(value = """
                SELECT c.*,
                (6371 * acos(
                    cos(radians(:lat)) * cos(radians(c.latitude)) *
                    cos(radians(c.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(c.latitude))
                )) AS distance
                FROM user_details c
                JOIN contractor_skills s ON c.user_id = s.contractor_id
                WHERE s.skill = :requiredSkill
                  AND c.available = true
                  AND (6371 * acos(
                    cos(radians(:lat)) * cos(radians(c.latitude)) *
                    cos(radians(c.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(c.latitude))
                )) <= :maxDistance
                ORDER BY distance ASC
                LIMIT 1
            """, nativeQuery = true)
    Contractor findNearestWithinDistance(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("requiredSkill") String skill,
            @Param("maxDistance") double maxDistance
    );

    /**
     * Finds all contractors with the given skill within max distance from the given latitude and longitude coordinates.
     * This is used to find eligible contractors when selecting contractors via the map.
     * @param skill the required skill to match against the contractors
     * @param lat the latitude of the specified location, i.e. the renovation location
     * @param lon the longitude of the specified location
     * @param maxDistance the maximum distance contractors must be within (inclusive)
     * @return the list of matching contractors
     */
    @Query(value = """
                SELECT c.*
                FROM user_details c
                JOIN contractor_skills s ON c.user_id = s.contractor_id
                WHERE s.skill = :requiredSkill
                  AND c.available = true
                  AND (6371 * acos(
                    cos(radians(:lat)) * cos(radians(c.latitude)) *
                    cos(radians(c.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(c.latitude))
                )) <= :maxDistance
            """, nativeQuery = true)
    List<Contractor> findEligible(@Param("requiredSkill") String skill, @Param("lat") double lat,
                                  @Param("lon") double lon, @Param("maxDistance") double maxDistance);

    /**
     * Finds all contractors with the given skill within max distance from the given latitude and longitude coordinates.
     * This is used to find eligible contractors when selecting contractors via the map.
     * @param skill the required skill to match against the contractors
     * @param lat the latitude of the specified location, i.e. the renovation location
     * @param lon the longitude of the specified location
     * @param maxDistance the maximum distance contractors must be within (inclusive)
     * @return the list of matching contractors
     */
    @Query(value = """
                SELECT c.*
                FROM user_details c
                JOIN contractor_skills s ON c.user_id = s.contractor_id
                WHERE s.skill = :requiredSkill
                  AND c.available = true
                  AND (c.user_id NOT IN (:excludedIds))
                  AND (6371 * acos(
                    cos(radians(:lat)) * cos(radians(c.latitude)) *
                    cos(radians(c.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(c.latitude))
                )) <= :maxDistance
            """, nativeQuery = true)
    List<Contractor> findEligibleExcluding(@Param("requiredSkill") String skill, @Param("lat") double lat,
                                           @Param("lon") double lon, @Param("maxDistance") double maxDistance,
                                           @Param("excludedIds") java.util.Set<Long> excludedIds);


}
