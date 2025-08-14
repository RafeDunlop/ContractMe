package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.Before;
import jakarta.persistence.EntityManager;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
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
    private TeamsRepository teamsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;
    /** Entities must be deleted in order of children to parent */
    @Before
    @Transactional
    public void clearDatabaseBeforeScenario() {
        entityManager.createQuery("DELETE FROM Authority").executeUpdate();

        entityManager.createQuery("DELETE FROM RenovationTask ").executeUpdate();
        renovationTaskRepository.deleteAll();

        // Team has to be deleted before renovation record as it's a child.
        entityManager.createQuery("DELETE FROM Team ").executeUpdate();
        teamsRepository.deleteAll();

        entityManager.createQuery("DELETE FROM RenovationRecord").executeUpdate();
        renovationRecordRepository.deleteAll();

        entityManager.createQuery("DELETE FROM Tag").executeUpdate();
        tagRepository.deleteAll();

        entityManager.createQuery("DELETE FROM User").executeUpdate();
        userRepository.deleteAll();

        entityManager.flush();
        entityManager.clear();
    }
}