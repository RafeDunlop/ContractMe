package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.FormResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FormResult repository accessor using Spring's @link{CrudRepository}.
 * These (basic) methods are provided for us without the need to write our own implementations
 */
@Repository
public interface FormRepository extends CrudRepository<FormResult, Long> {
    Optional<FormResult> findById(long id);
    List<FormResult> findAll();

    @Query("SELECT f FROM FormResult f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<FormResult> findByNameContainingIgnoreCase(@Param("name") String name);
}
