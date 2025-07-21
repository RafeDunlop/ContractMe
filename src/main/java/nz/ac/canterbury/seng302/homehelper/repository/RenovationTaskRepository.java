package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
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

    /**
     * Removes all tasks associated with a given {@code RenovationRecord}
     * @param id The id of the {@code RenovationRecord} whose tasks are to be deleted
     */
    @Modifying
    @Query("DELETE FROM RenovationTask rt WHERE rt.renovationRecord.id = :id")
    void deleteTaskById(long id);

    /**
     * Gets all {@code RenovationTask objects} whose due dates fall between the specified {@code LocalDate} objects, for the specified {@code RenovationRecord}
     * @param startDate The minimal due date for which to retrieve tasks
     * @param endDate The maximal due date for which to retrieve tasks
     * @param renovationRecord The {@code RenovationRecord} whose associated {@code RenovationTask} objects should be retrieved
     * @return The list of all {@code RenovationTask} objects whose due dates fall between the specified dates
     */
    @Query("SELECT rt FROM RenovationTask rt WHERE rt.renovationRecord = :renovationRecord AND rt.dueDate BETWEEN :startDate AND :endDate ORDER BY rt.dueDate ASC")
    List<RenovationTask> getByDueDateBetween(LocalDate startDate, LocalDate endDate, RenovationRecord renovationRecord);
}
