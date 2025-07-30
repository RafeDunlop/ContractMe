package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.Before;
import jakarta.persistence.EntityManager;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class CucumberHooks {

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private RenovationTaskRepository renovationTaskRepository;

    @Autowired
    private EntityManager entityManager;

    @Before
    @Transactional
    public void clearDatabaseBeforeScenario() {

        entityManager.createQuery("DELETE FROM RenovationTask ").executeUpdate();
        renovationTaskRepository.deleteAll();
        entityManager.createQuery("DELETE FROM RenovationRecord").executeUpdate();
        renovationRecordRepository.deleteAll();
        entityManager.createQuery("DELETE FROM Tag").executeUpdate();
        tagRepository.deleteAll();


        entityManager.flush();
        entityManager.clear();
    }
}