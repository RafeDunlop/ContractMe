package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import nz.ac.canterbury.seng302.homehelper.validation.TeamValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

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

    @BeforeEach
    void setUp() {
        teamsService = new TeamsService(teamsRepository, teamValidation, contractorRepository);
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

        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("PLUMBING"), anyDouble(), anySet()))
                .thenReturn(contractor1);
        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("ELECTRICAL"), anyDouble(), argThat(set -> set.contains(1L))))
                .thenReturn(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor1, team.getRoles().get(0).getContractor());
        assertEquals(contractor2, team.getRoles().get(1).getContractor());
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

        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("PLUMBING"), eq(200.0), anySet()))
                .thenReturn(contractor);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor, team.getRoles().get(0).getContractor());
    }

    @Test
    void contractorOutsideDistanceLimit_shouldNotBeAssigned() {
        Team team = new Team(new RenovationRecord());
        Role role = new Role(Skill.PLUMBING);
        team.addRole(role);
        Location location = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", 43.53, 172.63);

        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("PLUMBING"), eq(200.0), anySet()))
                .thenReturn(null);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertNull(team.getRoles().get(0).getContractor());
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

        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("PLUMBING"), anyDouble(), argThat(Set::isEmpty)))
                .thenReturn(contractor1);
        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("PLUMBING"), anyDouble(), argThat(set -> set.contains(1L))))
                .thenReturn(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor1, team.getRoles().get(0).getContractor());
        assertEquals(contractor2, team.getRoles().get(1).getContractor());
    }

    @Test
    void findContractorReturnsNull_shouldReturnError() {
        Team team = new Team(new RenovationRecord());
        Role role = new Role(Skill.PLUMBING);
        team.addRole(role);
        Location location = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", 43.53, 172.63);

        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), anyString(), anyDouble(), anySet()))
                .thenReturn(null);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("Unable to find available contractors to fill team", result);
        assertNull(team.getRoles().get(0).getContractor());
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

        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("PLUMBING"), anyDouble(), anySet()))
                .thenReturn(contractor);
        when(contractorRepository.findNearestWithinDistanceExcluding(anyDouble(), anyDouble(), eq("ELECTRICAL"), anyDouble(), argThat(set -> set.contains(1L))))
                .thenReturn(contractor2);

        String result = teamsService.assignContractorsToTeam(team, location);

        assertEquals("", result);
        assertEquals(contractor, team.getRoles().get(0).getContractor());
        assertEquals(contractor2, team.getRoles().get(1).getContractor());
    }
}