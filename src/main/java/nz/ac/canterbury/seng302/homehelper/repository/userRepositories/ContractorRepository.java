package nz.ac.canterbury.seng302.homehelper.repository.userRepositories;

import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for managing {@link Contractor} entities.
 * Extends {@link UserBaseRepository} to inherit common query methods,
 * basic CRUD operations from {@link CrudRepository}.
 * This repository handles Contractors
 */
public interface ContractorRepository extends UserBaseRepository<Contractor> {

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

}
