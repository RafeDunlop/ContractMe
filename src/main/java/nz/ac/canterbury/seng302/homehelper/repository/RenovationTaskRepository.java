package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface RenovationTaskRepository extends CrudRepository<RenovationTask, Long> {


    Optional<RenovationTask> findById(long id);
}
