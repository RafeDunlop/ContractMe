package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


@SpringBootTest
@ActiveProfiles("test")
public class TeamsServiceIntegrationTest {
    @Autowired
    private ContractorRepository contractorRepository;
    @Autowired
    private TeamsRepository teamsRepository;
    @Autowired
    private TeamsService teamsService;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private UserRepository userRepository;

    private User user;
    private RenovationRecord renovation;
    private Location location;


    @Transactional
    @BeforeEach
    void setUp() {
        String uniqueEmail = "Test" + System.nanoTime() + "@test.test";
        user = new User("Test", "test", uniqueEmail, "test");
        user.activate();
        userRepository.save(user);
        renovation = new RenovationRecord(user, "Test renovation", "", new ArrayList<>());
        location = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", 43.53, 172.63);
        renovation.setLocation(location);
        renovationRecordRepository.save(renovation);
    }

    @Transactional
    @Test
    void validTeamAndLocation_assignContractorsToTeam_fillsTeam() {
        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.ELECTRICAL);
        team.addRole(role1);
        team.addRole(role2);
        teamsRepository.save(team);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor1 = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor1.setLocation(location);
        contractor1.addSkill(Skill.PLUMBING);
        contractor1.activate();
        contractorRepository.save(contractor1);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        contractor2.setLocation(location);
        contractor2.addSkill(Skill.ELECTRICAL);
        contractor2.activate();
        contractorRepository.save(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor1, team.getRoles().get(0).getContractor());
        assertEquals(contractor2, team.getRoles().get(1).getContractor());
    }

    @Test
    void teamWithNoRoles_assignContractorsToTeam_fillsTeam() {
        Team team = new Team(renovation);
        teamsRepository.save(team);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor1 = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor1.setLocation(location);
        contractor1.addSkill(Skill.PLUMBING);
        contractor1.activate();
        contractorRepository.save(contractor1);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
    }

    @Transactional
    @Test
    void contractorOutsideDistanceLimit_shouldNotBeAssigned_cantFillTeam() {
        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.ELECTRICAL);
        team.addRole(role1);
        team.addRole(role2);
        teamsRepository.save(team);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor1 = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        Location contractorLocation = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", 2, 1);
        contractor1.setLocation(contractorLocation);
        contractor1.addSkill(Skill.PLUMBING);
        contractor1.activate();
        contractorRepository.save(contractor1);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertNull(team.getRoles().get(0).getContractor());
    }

    @Transactional
    @Test
    void duplicateContractor_notAssignedToMultipleRoles_fillsTeam() {
        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.PLUMBING);
        team.addRole(role1);
        team.addRole(role2);
        teamsRepository.save(team);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor1 = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor1.addSkill(Skill.PLUMBING);
        contractor1.activate();
        contractorRepository.save(contractor1);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        contractor2.addSkill(Skill.PLUMBING);
        contractor2.activate();
        contractorRepository.save(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor1, team.getRoles().get(0).getContractor());
        assertEquals(contractor2, team.getRoles().get(1).getContractor());
    }

    @Transactional
    @Test
    void multipleSkillsOneContractor_contractorOnlyAssignedToOne_fillsTeam() {
        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.ELECTRICAL);
        team.addRole(role1);
        team.addRole(role2);
        teamsRepository.save(team);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor.setLocation(location);
        contractor.addSkill(Skill.PLUMBING);
        contractor.addSkill(Skill.ELECTRICAL);
        contractor.activate();
        contractorRepository.save(contractor);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        contractor2.setLocation(location);
        contractor2.activate();
        contractorRepository.save(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor, team.getRoles().get(0).getContractor());
        assertEquals(contractor2, team.getRoles().get(1).getContractor());
    }

    @Transactional
    @Test
    void firstContractorGreedy_findsOptimalSolution_fullsTeam() {
        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.ELECTRICAL);
        team.addRole(role1);
        team.addRole(role2);
        teamsRepository.save(team);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor.setLocation(location);
        contractor.addSkill(Skill.PLUMBING);
        contractor.addSkill(Skill.ELECTRICAL);
        contractor.activate();
        contractorRepository.save(contractor);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        contractor2.setLocation(location);
        contractor2.addSkill(Skill.PLUMBING);
        contractor2.activate();
        contractorRepository.save(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor, team.getRoles().get(0).getContractor());
        assertEquals(contractor2, team.getRoles().get(1).getContractor());
    }

    @Transactional
    @Test
    void firstContractorGreedy2_findsOptimalSolution_fullsTeam() {
        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.ELECTRICAL);
        team.addRole(role1);
        team.addRole(role2);
        teamsRepository.save(team);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");

        contractor.setLocation(location);
        contractor.addSkill(Skill.PLUMBING);
        contractor.addSkill(Skill.ELECTRICAL);
        contractor.activate();
        contractorRepository.save(contractor);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        Location contractor2Location = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", 43.52, 172.63);
        contractor2.setLocation(contractor2Location);
        contractor2.addSkill(Skill.PLUMBING);
        contractor2.activate();
        contractorRepository.save(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor, team.getRoles().get(0).getContractor());
        assertEquals(contractor2, team.getRoles().get(1).getContractor());
    }
}
