package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * basic CRUD repository for {@link RenovationTask}
 */
public interface RenovationTaskRepository extends CrudRepository<RenovationTask, Long> {

    /**
     * Gets the task identified by the specified id
     * @param id The key of the renovation task to get
     * @return Optional.empty if none was found or an Optional of the specified task
     */
    Optional<RenovationTask> findById(long id);
}
