package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.RoleStatus;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.service.TeamInvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class TeamInvitationServiceTest {

    private TeamInvitationService teamInvitationService;
    private TeamsRepository teamsRepository;
    Team team;
    Contractor contractor;

    @BeforeEach
    void setUp() {
        teamsRepository = Mockito.mock(TeamsRepository.class);
        teamInvitationService = new TeamInvitationService(teamsRepository);
        contractor = Mockito.spy(new Contractor("Greg", "Smith", "greg@test.com", "Password123!"));
        when(contractor.getId()).thenReturn(1L);
        team = Mockito.mock(Team.class);
    }

    @Test
    void findAssignedRole_roleExists_returnsCorrectRole() {
        Role matchingRole = new Role(contractor, Skill.ELECTRICAL,  RoleStatus.ACCEPTED);
        Mockito.when(team.getRoles()).thenReturn(List.of(matchingRole));

        Role result = teamInvitationService.findAssignedRole(team, contractor);
        assertThat(result).isEqualTo(matchingRole);
    }

    @Test
    void findAssignedRole_doesntExist_ThrowsException() {
        Mockito.when(team.getRoles()).thenReturn(List.of());

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> teamInvitationService.findAssignedRole(team, contractor));
        assertEquals("Contractor is not apart of this team.", e.getMessage());
    }

    @Test
    void findAssignedRole_contractorAssignedTwice_ThrowsException() {
        Role testRole = new Role(contractor, Skill.ELECTRICAL,  RoleStatus.ACCEPTED);
        Mockito.when(team.getRoles()).thenReturn(List.of(testRole, testRole));

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> teamInvitationService.findAssignedRole(team, contractor));
        assertEquals("Contractor is in more then one role in the team.", e.getMessage());
    }
}
