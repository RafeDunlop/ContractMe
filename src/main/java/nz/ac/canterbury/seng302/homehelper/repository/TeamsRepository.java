package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamsRepository extends CrudRepository<Team, Long>{

    boolean existsByRenovationRecordId(Long id);

    Team findByRenovationRecord(RenovationRecord renovationRecord);


    /**
     * Find teams by contractor where the contractor has not yet accepted a role.
     *
     * @param contractorId the id of the contractor to match
     * @return the list of matching teams
     */
    @Query("SELECT t FROM Team t JOIN FETCH t.roles r WHERE r.contractorId = :contractorId AND r.accepted = FALSE ORDER BY r.creationDate DESC")
    List<Team> findByRoleContractor(Long contractorId);

    /**
     * Checks if a given user belongs to the team associated with a renovation record
     * @param renovationRecord the renovation record that we want to check the associated team
     * @param userId the id of the user to check if they belong to the team
     * @return boolean, true if the user is a contractor and belongs to the team associated with the record
     */
    @Query("""
    select count(t) > 0
    from Team t join t.roles r
    where t.renovationRecord = :renovationRecord
      and r.contractorId = :userId
      and exists (select 1 from Contractor c where c.id = :userId)
    """)
    boolean checkIfUserBelongsToRecordTeam(@Param("renovationRecord") RenovationRecord renovationRecord, @Param("userId") Long userId);

    /**
     * Delete the teams associated with the specified RenovationRecord
     * @param renovationRecord The renovation record whose associated Teams should be deleted
     */
    @Modifying
    @Query("DELETE FROM Team team WHERE team.renovationRecord = :renovationRecord")
    void deleteByRenovationRecord(RenovationRecord renovationRecord);


}
