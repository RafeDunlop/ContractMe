package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
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
     * @param contractor the contractor to match
     * @return the list of matching teams
     */
        @Query("SELECT t FROM Team t JOIN FETCH t.roles r WHERE r.contractor = :contractor AND r.accepted = FALSE ORDER BY r.creationDate DESC")
        List<Team> findByRoleContractor(@Param("contractor") Contractor contractor);
}
