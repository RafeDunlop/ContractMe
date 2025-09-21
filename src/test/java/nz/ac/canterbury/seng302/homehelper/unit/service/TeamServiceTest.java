package nz.ac.canterbury.seng302.homehelper.unit.service;

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
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import nz.ac.canterbury.seng302.homehelper.validation.TeamValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    @Mock
    private TeamsRepository teamsRepository;
    @Mock
    private ContractorRepository contractorRepository;
    private TeamsService teamsService;
    @Mock
    private TeamValidation teamValidation;
    @Mock
    private EmailService emailService;
    @Mock
    private RenovationRecordRepository renovationRecordRepository;


    private RenovationRecord renovationRecord;
    private Team team;
    private Location location;
    private User owner;

    @BeforeEach
    void setUp() {
        teamsService = new TeamsService(teamsRepository, teamValidation, contractorRepository, emailService, renovationRecordRepository);

        renovationRecord = mock(RenovationRecord.class);
        owner = mock(User.class);
        location = mock(Location.class);

        team = new Team(renovationRecord);
    }

    @Test
    void teamDoesNotExist_createRoles_rolesAreCreated() {
        List<Role> roles = teamsService.createRoles(List.of("ELECTRICAL", "PLUMBING"));
        assertEquals(2, roles.size());
    }

    @Test
    void teamDoesNotExist_saveTeam_callsRepositorySave() {
        Team team = new Team(new RenovationRecord());
        when(teamsRepository.save(team)).thenReturn(team);
        teamsService.saveTeam(team);
        verify(teamsRepository).save(team);
    }

    @Test
    void teamExists_whenTeamExists_returnsTrue() {
        Long renovationId = 1L;
        when(teamsRepository.existsByRenovationRecordId(renovationId)).thenReturn(true);
        boolean result = teamsService.teamExists(renovationId);
        assertTrue(result);
        verify(teamsRepository).existsByRenovationRecordId(renovationId);
    }

    @Test
    void teamExists_whenTeamDoesNotExist_returnsFalse() {
        Long renovationId = 1L;
        when(teamsRepository.existsByRenovationRecordId(renovationId)).thenReturn(false);
        boolean result = teamsService.teamExists(renovationId);
        assertFalse(result);
        verify(teamsRepository).existsByRenovationRecordId(renovationId);
    }

    @Test
    void teamExists_withNullId_returnsFalse() {
        when(teamsRepository.existsByRenovationRecordId(null)).thenReturn(false);
        boolean result = teamsService.teamExists(null);
        assertFalse(result);
        verify(teamsRepository).existsByRenovationRecordId(null);
    }

    @Test
    void validTeamAndLocation_assignContractorsToTeam_returnEmptyString() {
        Team team = new Team(new RenovationRecord());
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.ELECTRICAL);
        team.addRole(role1);
        team.addRole(role2);

        Location location = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", 43.53, 172.63);

        Contractor contractor1 = spy(new Contractor("Alice", "Doe", "alice@doe.com", "encoded"));
        contractor1.addSkill(Skill.PLUMBING);
        when(contractor1.getId()).thenReturn(1L);

        Contractor contractor2 = spy(new Contractor("Bob", "Doe", "bob@doe.com", "encoded"));
        contractor2.addSkill(Skill.ELECTRICAL);
        when(contractor2.getId()).thenReturn(2L);

        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("PLUMBING"), anyDouble(), isNull()))
                .thenReturn(contractor1);
        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("ELECTRICAL"), anyDouble(), argThat(set -> set.contains(1L))))
                .thenReturn(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor1.getId(), team.getRoles().get(0).getContractorId());
        assertEquals(contractor2.getId(), team.getRoles().get(1).getContractorId());
    }

    @Test
    void teamWithNoRoles_assignContractorsToTeam_returnEmptyString() {
        Team team = new Team(new RenovationRecord());
        Location location = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", 43.53, 172.63);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        verify(contractorRepository, never()).findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), anyString(), anyDouble(), anySet());
    }

    @Test
    void contractorAtExactDistanceLimit_shouldBeAssigned() {
        Team team = new Team(new RenovationRecord());
        Role role = new Role(Skill.PLUMBING);
        team.addRole(role);
        Location location = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", 43.53, 172.63);

        Contractor contractor = spy(new Contractor("Alice", "Doe", "alice@doe.com", "encoded"));
        contractor.addSkill(Skill.PLUMBING);
        when(contractor.getId()).thenReturn(1L);

        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("PLUMBING"), eq(200.0), isNull()))
                .thenReturn(contractor);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor.getId(), team.getRoles().get(0).getContractorId());
    }

    @Test
    void contractorOutsideDistanceLimit_shouldNotBeAssigned() {
        Team team = new Team(new RenovationRecord());
        Role role = new Role(Skill.PLUMBING);
        team.addRole(role);
        Location location = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", 43.53, 172.63);

        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("PLUMBING"), eq(200.0), isNull()))
                .thenReturn(null);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertNull(team.getRoles().get(0).getContractorId());
    }

    @Test
    void duplicateContractorNotAssignedToMultipleRoles() {
        Team team = new Team(new RenovationRecord());
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.PLUMBING);
        team.addRole(role1);
        team.addRole(role2);

        Location location = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", 43.53, 172.63);

        Contractor contractor1 = spy(new Contractor("Alice", "Doe", "alice@doe.com", "encoded"));
        contractor1.addSkill(Skill.PLUMBING);
        when(contractor1.getId()).thenReturn(1L);

        Contractor contractor2 = spy(new Contractor("Bob", "Doe", "bob@doe.com", "encoded"));
        contractor2.addSkill(Skill.PLUMBING);
        when(contractor2.getId()).thenReturn(2L);

        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("PLUMBING"), anyDouble(), isNull()))
                .thenReturn(contractor1);
        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("PLUMBING"), anyDouble(), argThat(set -> set != null && set.contains(1L))))
                .thenReturn(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor1.getId(), team.getRoles().get(0).getContractorId());
        assertEquals(contractor2.getId(), team.getRoles().get(1).getContractorId());
    }

    @Test
    void findContractorReturnsNull_shouldReturnError() {
        Team team = new Team(new RenovationRecord());
        Role role = new Role(Skill.PLUMBING);
        team.addRole(role);
        Location location = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", 43.53, 172.63);

        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), anyString(), anyDouble(), isNull()))
                .thenReturn(null);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertNull(team.getRoles().get(0).getContractorId());
    }

    @Test
    void multipleSkillsOneContractor_notAssignedTwice() {
        Team team = new Team(new RenovationRecord());
        Role role1 = new Role(Skill.PLUMBING);
        Role role2 = new Role(Skill.ELECTRICAL);
        team.addRole(role1);
        team.addRole(role2);

        Location location = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", 43.53, 172.63);

        Contractor contractor = spy(new Contractor("Alice", "Doe", "alice@doe.com", "encoded"));
        contractor.addSkill(Skill.PLUMBING);
        contractor.addSkill(Skill.ELECTRICAL);
        when(contractor.getId()).thenReturn(1L);

        Contractor contractor2 = spy(new Contractor("Bob", "Doe", "bob@doe.com", "encoded"));
        when(contractor2.getId()).thenReturn(2L);

        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("PLUMBING"), anyDouble(), isNull()))
                .thenReturn(contractor);
        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("ELECTRICAL"), anyDouble(), argThat(set -> set.contains(1L))))
                .thenReturn(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor.getId(), team.getRoles().get(0).getContractorId());
        assertEquals(contractor2.getId(), team.getRoles().get(1).getContractorId());
    }

    @Test
    void getContractorTeamRequests_userIsContractor_callRepository() {
        Contractor contractor = mock(Contractor.class);
        when(contractor.getId()).thenReturn(1L);
        when(teamsRepository.findByRoleContractor(1L)).thenReturn(List.of());
        teamsService.getContractorTeamRequests(contractor);
        verify(teamsRepository).findByRoleContractor(1L);
    }

    @Test
    void getContractorTeamRequests_userIsNotContractor_throwsException() {
        User user = new User("Jane", "Doe", "jane@doe.com", "password");
        assertThrows(IllegalArgumentException.class, () -> teamsService.getContractorTeamRequests(user));
        verify(teamsRepository, never()).findByRoleContractor(Mockito.any());
    }

    @Test
    void getContractorRole_roleAssigned_returnsRole() {
        Team team = new Team(new RenovationRecord());
        Role role = new Role(Skill.PLUMBING);
        Contractor contractor = Mockito.spy(new Contractor("Alice", "Doe", "alice@doe.com", "encoded"));
        when(contractor.getId()).thenReturn(1L);
        role.setContractor(contractor);
        team.addRole(role);
        Role result = assertDoesNotThrow(() -> teamsService.getContractorRole(contractor, team));
        assertEquals(role, result);
    }

    @Test
    void getContractorRole_roleNotAssigned_throwsException() {
        Team team = new Team(new RenovationRecord());
        Role role = new Role(Skill.PLUMBING);
        Contractor contractor = new Contractor("Alice", "Doe", "alice@doe.com", "encoded");
        role.setContractor(new Contractor("Bob", "Doe", "bob@doe.com", "encoded"));
        team.addRole(role);
        assertThrows(ResponseStatusException.class, () -> teamsService.getContractorRole(contractor, team));
    }

    @Test
    void getContractorRole_roleHasNullContractor_throwsException() {
        Team team = new Team(new RenovationRecord());
        Role role = new Role(Skill.PLUMBING);
        team.addRole(role);
        Contractor contractor = new Contractor("Alice", "Doe", "alice@doe.com", "encoded");
        assertThrows(ResponseStatusException.class, () -> teamsService.getContractorRole(contractor, team));
    }

    @Test
    void getContractorRole_roleNullOrAssigned_throwsNullPointerException() {
        Team team = new Team(new RenovationRecord());
        Role role = new Role(Skill.PLUMBING);
        team.addRole(role);
        Role otherRole = new Role(Skill.ELECTRICAL);
        otherRole.setContractor(new Contractor("Alice", "Doe", "alice@doe.com", "encoded"));
        team.addRole(otherRole);
        Contractor contractor = new Contractor("Bob", "Doe", "bob@doe.com", "encoded");
        assertThrows(ResponseStatusException.class, () -> teamsService.getContractorRole(contractor, team));
    }

    @Test
    void getContractorMap_allRolesFilled_returnsFullMap() {
        Team team = new Team(new RenovationRecord());
        team.setId(0L);
        List<Skill> skills = Arrays.asList(Skill.PLUMBING, Skill.ELECTRICAL, Skill.ACOUSTIC_INSULATION, Skill.ARCHITECTURE, Skill.ASBESTOS_REMOVAL);
        Map<Long, Contractor> expectedMap = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            Role role = new Role(skills.get(i));
            Contractor contractor = new Contractor("Bob", "Contractor", "bob" + i + "contractor@gmail.com", "encoded");
            ReflectionTestUtils.setField(contractor, "id", (long) i);
            expectedMap.put((long) i, contractor);
            role.setContractor(contractor);
            team.addRole(role);

            when(contractorRepository.findById((long) i)).thenReturn(Optional.of(contractor));

        }

        when(teamsRepository.findById(0L)).thenReturn(Optional.of(team));
        assertEquals(expectedMap, teamsService.getContractorsByTeamId(team.getId()));

    }

    @Test
    void runAlgorithmAgain_noNewContractors_noEmailsSent() {
        Role role = new Role(Skill.PLUMBING);
        team.addRole(role);

        teamsService.runAlgorithmAgain(team, location);

        verify(emailService, never()).sendRequestToContractor(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void runAlgorithmAgain_existingTeamMember_findsNewContractor_sendsEmailOnce() {
        Role role = new Role(Skill.ELECTRICAL);
        team.addRole(role);

        Role role2 = new Role(Skill.PLUMBING);
        Contractor existingTeamMember = mock(Contractor.class);

        team.addRole(role2);
        team.getRoles().get(1).setContractor(existingTeamMember);

        Contractor newContractor = mock(Contractor.class);
        when(newContractor.getId()).thenReturn(2L);

        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("ELECTRICAL"),
                anyDouble(), isNull())).thenReturn(newContractor);
        when(contractorRepository.findById(2L)).thenReturn(Optional.of(newContractor));

        when(owner.getFirstName()).thenReturn("Bob");
        when(renovationRecord.getUser()).thenReturn(owner);
        when(renovationRecord.getName()).thenReturn("Test renovation");

        teamsService.runAlgorithmAgain(team, location);

        verify(emailService, times(1)).sendRequestToContractor(
                any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deleteContractorRole_roleAssigned_deletesContractorFromRole() {
        Location location = new Location();
        location.setLatitude(-43);
        location.setLongitude(43);
        when(renovationRecord.getLocation()).thenReturn(location);
        Team team = new Team(renovationRecord);
        Role role = new Role(Skill.PLUMBING);
        team.addRole(role);
        Contractor contractor = new Contractor("Alice", "Doe", "alice@doe.com", "encoded");
        role.setContractor(contractor);
        teamsService.deleteContractorFromTeam(team,contractor);
        assertNull(role.getContractorId());
        assertFalse(role.isAccepted());
    }

}
