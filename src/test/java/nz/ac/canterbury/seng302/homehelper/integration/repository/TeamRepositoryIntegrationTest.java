package nz.ac.canterbury.seng302.homehelper.integration.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.*;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TeamRepositoryIntegrationTest {

    @Autowired
    TeamsRepository teamsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RenovationRecordRepository renovationRecordRepository;

    @Test
    void contractor_on_different_team_returns_false() {
        User owner = userRepository.save(new User("Steve","Jobs","steve@test.com","Password123!"));
        RenovationRecord renovationRecord = renovationRecordRepository.save( new RenovationRecord(owner, "Test Renovation", "one", Collections.emptyList()));
        Contractor contractor = userRepository.save( new Contractor("Greg", "Smith", "greg@test.com", "Password123!"));

        Team team = new Team(renovationRecord);

        RenovationRecord secondRecord = renovationRecordRepository.save( new RenovationRecord(owner, "Test Renovation 2", "two", Collections.emptyList()));
        Team secondTeam = new Team(secondRecord);
        secondTeam.addRole(new Role(contractor, Skill.ELECTRICAL,  RoleStatus.ACCEPTED));

        teamsRepository.save(team);
        teamsRepository.save(secondTeam);

        boolean result = teamsRepository.checkIfUserBelongsToRecordTeam(renovationRecord, contractor.getId());
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void contractor_on_team_for_record_accepted_parameterized_returns_true(boolean accepted) {
        User owner = userRepository.save(new User("Steve","Jobs","steve@test.com","Password123!"));
        RenovationRecord renovationRecord = renovationRecordRepository.save( new RenovationRecord(owner, "Test Renovation", "a description", Collections.emptyList()));
        Team team = new Team(renovationRecord);

        Contractor contractor = userRepository.save( new Contractor("Greg", "Smith", "greg@test.com", "Password123!"));
        team.addRole(new Role(contractor, Skill.ELECTRICAL, accepted ? RoleStatus.ACCEPTED :  RoleStatus.WAITING));

        teamsRepository.save(team);

        boolean result = teamsRepository.checkIfUserBelongsToRecordTeam(renovationRecord, contractor.getId());
        assertThat(result).isTrue();
    }


    @Test
    void user_not_contractor_no_team_on_record_returns_false() {
        User owner = userRepository.save(new User("Bob","Smith","bob@test.com","Password123!"));
        RenovationRecord renovationRecord = renovationRecordRepository.save( new RenovationRecord(owner, "Bobs Renovation", "Desc", Collections.emptyList()));

        User regularUser = userRepository.save( new User("Greg", "Smith", "greg@test.com", "Password123!"));

        boolean result = teamsRepository.checkIfUserBelongsToRecordTeam(renovationRecord, regularUser.getId());
        assertThat(result).isFalse();
    }
}
