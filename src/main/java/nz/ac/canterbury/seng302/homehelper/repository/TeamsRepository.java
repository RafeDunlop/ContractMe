package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import org.springframework.data.repository.CrudRepository;

public interface TeamsRepository extends CrudRepository<Team, Long>{

    boolean existsByRenovationRecordId(Long id);

    Team findByRenovationRecord(RenovationRecord renovationRecord);
}
