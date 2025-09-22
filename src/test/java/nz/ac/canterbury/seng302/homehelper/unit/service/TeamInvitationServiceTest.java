package nz.ac.canterbury.seng302.homehelper.unit.service;

import java.util.Set;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.RoleStatus;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.service.TeamInvitationService;
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TeamInvitationServiceTest {

    private TeamInvitationService teamInvitationService;
    private TeamsRepository teamsRepository;
    Team team;
    @Mock
    private TeamsService teamsService;
    Contractor contractor;

    @BeforeEach
    void setUp() {
        teamsRepository = mock(TeamsRepository.class);
        teamsService = mock(TeamsService.class);
        teamInvitationService = new TeamInvitationService(teamsRepository,teamsService);
        contractor = Mockito.spy(new Contractor("Greg", "Smith", "greg@test.com", "Password123!"));
        when(contractor.getId()).thenReturn(1L);
        team = mock(Team.class);
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

    @Test
    void rerunAlgorithm_invokesAssignmentForEachIncompleteTeam() {
        Team t1 = mock(Team.class);
        Team t2 = mock(Team.class);

        RenovationRecord rr1 = mock(RenovationRecord.class);
        RenovationRecord rr2 = mock(RenovationRecord.class);
        Location loc1 = mock(Location.class);
        Location loc2 = mock(Location.class);

        when(rr1.getLocation()).thenReturn(loc1);
        when(rr2.getLocation()).thenReturn(loc2);
        when(t1.getRenovationRecord()).thenReturn(rr1);
        when(t2.getRenovationRecord()).thenReturn(rr2);

        when(teamsRepository.getIncompleteTeams()).thenReturn(Set.of(t1, t2));

        teamInvitationService.rerunAlgorithm();

        verify(teamsService).runAlgorithmAgain(t1, loc1);
        verify(teamsService).runAlgorithmAgain(t2, loc2);
        verifyNoMoreInteractions(teamsService);
    }


}
