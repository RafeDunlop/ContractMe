package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Teams;
import org.springframework.data.repository.CrudRepository;

public interface TeamsRepository extends CrudRepository<Teams, Long>{

    boolean existsByRenovationRecordId(Long id);
}
