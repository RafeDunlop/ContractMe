package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.dto.MappedContractor;
import nz.ac.canterbury.seng302.homehelper.dto.TeamRequestDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.*;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TeamsServiceIntegrationTest {
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
    @MockBean
    private EmailService emailService;

    private RenovationRecord renovation;
    private Location location;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        teamsRepository.deleteAll();
        renovationRecordRepository.deleteAll();
        String uniqueEmail = "Test" + System.nanoTime() + "@test.test";
        User user = new User("Test", "test", uniqueEmail, "test");
        user.activate();
        userRepository.save(user);
        renovation = new RenovationRecord(user, "Test renovation", "", new ArrayList<>());
        location = new Location("20 Kirkwood Avenue", "NZ", "8041", "Christchurch", "Riccarton", -43.528066, 172.584604);
        renovation.setLocation(location);
        renovationRecordRepository.save(renovation);
    }

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
        contractor1.setAvailable(true);
        contractorRepository.save(contractor1);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        contractor2.setLocation(location);
        contractor2.addSkill(Skill.ELECTRICAL);
        contractor2.activate();
        contractor2.setAvailable(true);
        contractorRepository.save(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor1.getId(), team.getRoles().get(0).getContractorId());
        assertEquals(contractor2.getId(), team.getRoles().get(1).getContractorId());

    }

    @Test
    void validTeamAndLocation_assignContractorsToTeam_fillsTeamAndSendsEmails() {
        TeamRequestDTO teamRequestDTO = new TeamRequestDTO();
        teamRequestDTO.setInvitesAutomatic(true);
        teamRequestDTO.setSkills(List.of(Skill.PLUMBING.toString(), Skill.ELECTRICAL.toString()));
        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor1 = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor1.setLocation(location);
        contractor1.addSkill(Skill.PLUMBING);
        contractor1.activate();
        contractor1.setAvailable(true);
        contractorRepository.save(contractor1);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        contractor2.setLocation(location);
        contractor2.addSkill(Skill.ELECTRICAL);
        contractor2.activate();
        contractor2.setAvailable(true);
        contractorRepository.save(contractor2);

        teamsService.createNewTeam(renovation, teamRequestDTO);

        //If any skills are added in the future, change this threshold to match the number of skills present
        verify(emailService, times(2)).sendRequestToContractor(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), any(Locale.class),Mockito.anyLong());

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

    @Test
    void teamWithNoRoles_assignContractorsToTeam_fillsTeamAndSendsNoEmails() {
        TeamRequestDTO teamRequestDTO = new TeamRequestDTO();
        teamRequestDTO.setSkills(List.of());
        teamRequestDTO.setInvitesAutomatic(true);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor1 = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor1.setLocation(location);
        contractor1.addSkill(Skill.PLUMBING);
        contractor1.activate();
        contractorRepository.save(contractor1);

        teamsService.createNewTeam(renovation, teamRequestDTO);



        verify(emailService, Mockito.never()).sendRequestToContractor(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), any(Locale.class),Mockito.anyLong());
    }

    @Test
    void teamWithRoles_rolesUnfilled_andSendsNoEmails() {
        TeamRequestDTO teamRequestDTO = new TeamRequestDTO();
        teamRequestDTO.setSkills(List.of(Skill.PLUMBING.toString(), Skill.ELECTRICAL.toString()));
        teamRequestDTO.setInvitesAutomatic(true);
        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor1 = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor1.setLocation(location);
        contractor1.addSkill(Skill.PLUMBING);
        contractor1.activate();
        contractorRepository.save(contractor1);

        teamsService.createNewTeam(renovation, teamRequestDTO);

        verify(emailService, Mockito.never()).sendRequestToContractor(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), any(Locale.class),Mockito.anyLong());
    }

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
        contractor1.setAvailable(true);
        contractorRepository.save(contractor1);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertNull(team.getRoles().get(0).getContractorId());
    }

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
        contractor1.setAvailable(true);
        contractor1.setLocation(location);
        contractorRepository.save(contractor1);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        contractor2.addSkill(Skill.PLUMBING);
        contractor2.activate();
        contractor2.setAvailable(true);
        contractor2.setLocation(location);
        contractorRepository.save(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor1.getId(), team.getRoles().get(0).getContractorId());
        assertEquals(contractor2.getId(), team.getRoles().get(1).getContractorId());
    }

    @Test
    void multipleSkillsOneContractor_contractorOnlyAssignedToOne_fillOne() {
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
        contractor.setAvailable(true);
        contractorRepository.save(contractor);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        contractor2.setLocation(location);
        contractor2.activate();
        contractor2.setAvailable(true);
        contractorRepository.save(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertEquals(contractor.getId(), team.getRoles().get(0).getContractorId());
        assertNull(team.getRoles().get(1).getContractorId());
    }

    @Test
    void firstContractorGreedy_findsOptimalSolution_fillsTeam() {
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
        contractor.setAvailable(true);
        contractorRepository.save(contractor);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        contractor2.setLocation(location);
        contractor2.addSkill(Skill.PLUMBING);
        contractor2.activate();
        contractor2.setAvailable(true);
        contractorRepository.save(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor2.getId(), team.getRoles().get(0).getContractorId());
        assertEquals(contractor.getId(), team.getRoles().get(1).getContractorId());
    }

    @Test
    void firstContractorGreedy2_findsOptimalSolution_fillsTeam() {
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
        contractor.setAvailable(true);
        contractorRepository.save(contractor);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        Location contractor2Location = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", -43.52, 172.63);
        contractor2.setLocation(contractor2Location);
        contractor2.addSkill(Skill.PLUMBING);
        contractor2.activate();
        contractor2.setAvailable(true);
        contractorRepository.save(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor2.getId(), team.getRoles().get(0).getContractorId());
        assertEquals(contractor.getId(), team.getRoles().get(1).getContractorId());
    }

    @Test
    void oneContractorTwoRoles_noInfiniteLoop_fillsOne() {
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
        contractor.setAvailable(true);
        contractorRepository.save(contractor);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertEquals(contractor.getId(), team.getRoles().get(0).getContractorId());
    }

    @Test
    void oneContractorTwoRoles2_noInfiniteLoop_fillsOne() {
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
        contractor.activate();
        contractor.setAvailable(true);
        contractorRepository.save(contractor);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertEquals(contractor.getId(), team.getRoles().get(0).getContractorId());
    }

    @Test
    void oneContractorTwoRoles3_noInfiniteLoop_fillsOne() {
        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.PLUMBING);
        team.addRole(role1);
        team.addRole(role2);
        teamsRepository.save(team);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");

        contractor.setLocation(location);
        contractor.addSkill(Skill.PLUMBING);
        contractor.activate();
        contractor.setAvailable(true);
        contractorRepository.save(contractor);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertEquals(contractor.getId(), team.getRoles().get(0).getContractorId());
    }

    @Test
    void oneContractorTwoRoles4_noInfiniteLoop_fillsOne() {
        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.ELECTRICAL);
        team.addRole(role1);
        team.addRole(role2);
        teamsRepository.save(team);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");

        contractor.setLocation(location);
        contractor.addSkill(Skill.ELECTRICAL);
        contractor.activate();
        contractor.setAvailable(true);
        contractorRepository.save(contractor);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertEquals(contractor.getId(), team.getRoles().get(1).getContractorId());
    }

    @Test
    void twoContractorsThreeRoles_maximumBacktracing_fillsTwo() {
        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.ELECTRICAL);
        Role role3 = new Role(Skill.CARPENTRY);
        team.addRole(role1);
        team.addRole(role2);
        team.addRole(role3);
        teamsRepository.save(team);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor.setLocation(location);
        contractor.addSkill(Skill.PLUMBING);
        contractor.addSkill(Skill.ELECTRICAL);
        contractor.addSkill(Skill.CARPENTRY);
        contractor.activate();
        contractor.setAvailable(true);
        contractorRepository.save(contractor);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        contractor2.setLocation(location);
        contractor2.addSkill(Skill.PLUMBING);
        contractor2.addSkill(Skill.ELECTRICAL);
        contractor2.addSkill(Skill.CARPENTRY);
        contractor2.activate();
        contractor2.setAvailable(true);
        contractorRepository.save(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertEquals(contractor.getId(), team.getRoles().get(0).getContractorId());
        assertEquals(contractor2.getId(), team.getRoles().get(1).getContractorId());
    }

    @Test
    void threeContractorsFourRoles_maximumBacktracing_fillsThree() {
        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.PLUMBING);
        Role role3 = new Role(Skill.PLUMBING);
        Role role4 = new Role(Skill.PLUMBING);
        team.addRole(role1);
        team.addRole(role2);
        team.addRole(role3);
        team.addRole(role4);
        teamsRepository.save(team);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor.setLocation(location);
        contractor.addSkill(Skill.PLUMBING);
        contractor.activate();
        contractor.setAvailable(true);
        contractorRepository.save(contractor);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        contractor2.setLocation(location);
        contractor2.addSkill(Skill.PLUMBING);
        contractor2.activate();
        contractor2.setAvailable(true);
        contractorRepository.save(contractor2);

        String chrisUniqueEmail = "chris" + System.nanoTime() + "@doe.com";
        Contractor contractor3 = new Contractor("Chris", "Doe", chrisUniqueEmail, "encoded");
        contractor3.setLocation(location);
        contractor3.addSkill(Skill.PLUMBING);
        contractor3.activate();
        contractor3.setAvailable(true);
        contractorRepository.save(contractor3);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertEquals(contractor.getId(), team.getRoles().get(0).getContractorId());
        assertEquals(contractor2.getId(), team.getRoles().get(1).getContractorId());
        assertEquals(contractor3.getId(), team.getRoles().get(2).getContractorId());
    }

    @Test
    void fourContractorsFiveRoles_maximumBacktracing_fillsFour() {
        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.ELECTRICAL);
        Role role3 = new Role(Skill.CARPENTRY);
        Role role4 = new Role(Skill.FLOORING);
        Role role5 = new Role(Skill.WELDING);
        team.addRole(role1);
        team.addRole(role2);
        team.addRole(role3);
        team.addRole(role4);
        team.addRole(role5);
        teamsRepository.save(team);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor.setLocation(location);
        contractor.addSkill(Skill.PLUMBING);
        contractor.addSkill(Skill.ELECTRICAL);
        contractor.addSkill(Skill.CARPENTRY);
        contractor.addSkill(Skill.FLOORING);
        contractor.addSkill(Skill.WELDING);
        contractor.activate();
        contractor.setAvailable(true);
        contractorRepository.save(contractor);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        contractor2.setLocation(location);
        contractor2.addSkill(Skill.PLUMBING);
        contractor2.addSkill(Skill.ELECTRICAL);
        contractor2.addSkill(Skill.CARPENTRY);
        contractor2.addSkill(Skill.FLOORING);
        contractor2.addSkill(Skill.WELDING);
        contractor2.activate();
        contractor2.setAvailable(true);
        contractorRepository.save(contractor2);

        String chrisUniqueEmail = "chris" + System.nanoTime() + "@doe.com";
        Contractor contractor3 = new Contractor("Chris", "Doe", chrisUniqueEmail, "encoded");
        contractor3.setLocation(location);
        contractor3.addSkill(Skill.PLUMBING);
        contractor3.addSkill(Skill.ELECTRICAL);
        contractor3.addSkill(Skill.CARPENTRY);
        contractor3.addSkill(Skill.FLOORING);
        contractor3.addSkill(Skill.WELDING);
        contractor3.activate();
        contractor3.setAvailable(true);
        contractorRepository.save(contractor3);

        String jennyUniqueEmail = "jenny" + System.nanoTime() + "@doe.com";
        Contractor contractor4 = new Contractor("Jenny", "Doe", jennyUniqueEmail, "encoded");
        contractor4.setLocation(location);
        contractor4.addSkill(Skill.PLUMBING);
        contractor4.addSkill(Skill.ELECTRICAL);
        contractor4.addSkill(Skill.CARPENTRY);
        contractor4.addSkill(Skill.FLOORING);
        contractor4.addSkill(Skill.WELDING);
        contractor4.activate();
        contractor4.setAvailable(true);
        contractorRepository.save(contractor4);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertEquals(contractor.getId(), team.getRoles().get(0).getContractorId());
        assertEquals(contractor2.getId(), team.getRoles().get(1).getContractorId());
        assertEquals(contractor3.getId(), team.getRoles().get(2).getContractorId());
        assertEquals(contractor4.getId(), team.getRoles().get(3).getContractorId());
    }

    @Test
    void oneContractorsFiveRoles_maximumBacktracing_fillsOne() {
        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.PLUMBING);
        Role role3 = new Role(Skill.PLUMBING);
        Role role4 = new Role(Skill.PLUMBING);
        Role role5 = new Role(Skill.PLUMBING);
        team.addRole(role1);
        team.addRole(role2);
        team.addRole(role3);
        team.addRole(role4);
        team.addRole(role5);
        teamsRepository.save(team);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor.setLocation(location);
        contractor.addSkill(Skill.PLUMBING);
        contractor.activate();
        contractor.setAvailable(true);
        contractorRepository.save(contractor);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertEquals(contractor.getId(), team.getRoles().get(0).getContractorId());
    }

    @Test
    void contractorInBlacklist_shouldNotBeAssigned_cantFillTeam() {
        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor1 = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor1.setLocation(location);
        contractor1.addSkill(Skill.PLUMBING);
        contractor1.activate();
        contractor1.setAvailable(true);
        contractorRepository.save(contractor1);

        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        team.addRole(role1);
        team.addBlacklistId(contractor1.getId());
        teamsRepository.save(team);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertNull(team.getRoles().get(0).getContractorId());
    }

    @Test
    void contractorInBlacklist_findNextClosest_fillsOne() {
        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor contractor1 = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        contractor1.setLocation(location);
        contractor1.addSkill(Skill.PLUMBING);
        contractor1.activate();
        contractor1.setAvailable(true);
        contractorRepository.save(contractor1);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor contractor2 = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        Location contractorLocation = new Location("143 Kirkwood Avenue", "NZ", "8041", "Christchurch", "Hornby", -43.52, 172.70);
        contractor2.setLocation(contractorLocation);
        contractor2.addSkill(Skill.PLUMBING);
        contractor2.activate();
        contractor2.setAvailable(true);
        contractorRepository.save(contractor2);

        Team team = new Team(renovation);
        Role role1 = new Role(Skill.PLUMBING);
        team.addRole(role1);
        team.addBlacklistId(contractor1.getId());
        teamsRepository.save(team);


        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor2.getId(), team.getRoles().get(0).getContractorId());
    }

    @Test
    void removeContractor_teamHasAnotherContractor_oneEmailSent() {
        Team team = new Team(renovation);

        String aliceUniqueEmail = "alice" + System.nanoTime() + "@doe.com";
        Contractor alice = new Contractor("Alice", "Doe", aliceUniqueEmail, "encoded");
        alice.setLocation(location);
        alice.addSkill(Skill.PLUMBING);
        alice.activate();
        alice.setAvailable(true);
        alice = contractorRepository.save(alice);
        Role role = new Role(Skill.PLUMBING);
        role.setContractor(alice);
        role.setStatus(RoleStatus.ACCEPTED);
        team.addRole(role);

        String bobUniqueEmail = "bob" + System.nanoTime() + "@doe.com";
        Contractor bob = new Contractor("Bob", "Doe", bobUniqueEmail, "encoded");
        bob.setLocation(location);
        bob.addSkill(Skill.ANTIQUE_RESTORATION);
        bob.activate();
        bob.setAvailable(true);
        bob = contractorRepository.save(bob);
        role = new Role(Skill.ANTIQUE_RESTORATION);
        role.setContractor(bob);
        team.addRole(role);

        String janeUniqueEmail = "chris" + System.nanoTime() + "@doe.com";
        Contractor jane = new Contractor("Chris", "Doe", janeUniqueEmail, "encoded");
        jane.setLocation(location);
        jane.addSkill(Skill.ANTIQUE_RESTORATION);
        jane.activate();
        jane.setAvailable(true);
        contractorRepository.save(jane);

        team = teamsRepository.save(team);

        teamsService.deleteContractorFromTeam(team, bob);
        verify(emailService, times(1)).sendRequestToContractor(
                any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void deleteTeam_teamDeletedAndRenovationNotDeleted() {
        Team team = teamsRepository.save(new Team(renovation));
        assertDoesNotThrow(() -> teamsService.deleteTeam(team));
        assertNull(teamsRepository.findByRenovationRecord(renovation));
    }

    @Test
    void findEligibleContractors_eligibleContractorsExist_returnsList() {
        Contractor contractor1 = new Contractor("Bob", "Builder", "bob@builder.com", "password");
        contractor1.setSkills(Set.of(Skill.ARCHITECTURE, Skill.RESOURCE_CONSENT_COMPLIANCE));
        Location location1 = new Location("", "", "", "", "", -42.297332, 173.748173);
        contractor1.setLocation(location1);
        contractor1.setAvailable(true);
        contractor1 = contractorRepository.save(contractor1);
        Contractor contractor2 = new Contractor("Alice", "Builder", "alice@builder.com", "password");
        contractor2.setSkills(Set.of(Skill.ARCHITECTURE, Skill.PLUMBING));
        Location location2 = new Location("", "", "", "", "", -43.592, 172.381);
        contractor2.setLocation(location2);
        contractor2.setAvailable(true);
        contractor2 = contractorRepository.save(contractor2);
        Team team = new Team(renovation);
        team.addRole(new Role(Skill.ARCHITECTURE));
        teamsRepository.save(team);
        List<MappedContractor> expectedContractors = List.of(new MappedContractor(contractor1, Skill.ARCHITECTURE), new MappedContractor(contractor2, Skill.ARCHITECTURE));
        List<MappedContractor> actualContractors = teamsService.getEligibleContractors(Skill.ARCHITECTURE, renovation.getLocation(), team);
        assertEquals(expectedContractors, actualContractors);
    }

    @Test
    void findEligibleContractors_oneOutsideOneUnavailable_returnsCorrectList() {
        Contractor contractor = new Contractor("Alice", "Builder", "alice@builder.com", "password");
        contractor.setSkills(Set.of(Skill.ARCHITECTURE));
        Location locationOutsideMaxDistance = new Location("", "", "", "", "", -42.03505105485703, 173.97414793244704);
        contractor.setLocation(locationOutsideMaxDistance);
        contractor.setAvailable(true);
        contractorRepository.save(contractor);
        Contractor unAvailable = new Contractor("Bob", "Builder", "bob@builder.com", "password");
        unAvailable.setSkills(Set.of(Skill.ARCHITECTURE));
        unAvailable.setAvailable(false);
        unAvailable.setLocation(location);
        contractorRepository.save(unAvailable);
        Contractor contractor1 = new Contractor("Bob", "Builder", "bob2@builder.com", "password");
        contractor1.setSkills(Set.of(Skill.ARCHITECTURE, Skill.RESOURCE_CONSENT_COMPLIANCE));
        Location location1 = new Location("", "", "", "", "", -42.297332, 173.748173);
        contractor1.setLocation(location1);
        contractor1.setAvailable(true);
        contractor1 = contractorRepository.save(contractor1);
        Team team = new Team(renovation);
        teamsRepository.save(team);
        List<MappedContractor> expectedContractors = List.of(new MappedContractor(contractor1, Skill.ARCHITECTURE));
        List<MappedContractor> actualContractors = teamsService.getEligibleContractors(Skill.ARCHITECTURE, renovation.getLocation(), team);
        assertEquals(expectedContractors, actualContractors);
    }
}
