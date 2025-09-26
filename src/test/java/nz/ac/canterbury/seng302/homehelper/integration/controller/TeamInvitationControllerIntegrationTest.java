package nz.ac.canterbury.seng302.homehelper.integration.controller;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.*;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "Steve@doe.nz")
@Transactional
public class TeamInvitationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RenovationRecordRepository renovationRecordRepository;

    @Autowired
    TeamsRepository teamsRepository;

    private Contractor contractor;
    private RenovationRecord renovationRecord;
    private Team team;

    @BeforeEach
    public void setup() {
        User owner = new User("Jane", "Doe", "jane@doe.nz", "password");
        owner = userRepository.save(owner);
        owner.grantAuthority("ROLE_USER");

        renovationRecord = new RenovationRecord(owner, "test renovation", "test description", List.of());
        renovationRecord = renovationRecordRepository.save(renovationRecord);

        contractor = new Contractor("Steve", "Jobs", "Steve@doe.nz", "password123!");
        contractor.setSkills(Set.of(Skill.ACOUSTIC_INSULATION));
        contractor = userRepository.save(contractor);
        contractor.grantAuthority("ROLE_USER");

        team = new Team(renovationRecord);
        team = teamsRepository.save(team);
    }

    @Test
    void viewInvitation_contractorOnTeam_alreadyAccepted_returns404() throws Exception {
        team.addRole(new Role(contractor, Skill.ELECTRICAL,  RoleStatus.ACCEPTED));
        team = teamsRepository.save(team);

        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/invitations/" + team.getId())
                        .param("id", Long.toString(renovationRecord.getId())))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Team invitation link is no longer valid."));
    }

    @Test
    void viewInvitation_contractorNotOnTeam_returns404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/invitations/" + team.getId())
                        .param("id", Long.toString(renovationRecord.getId())))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void viewInvitation_teamNotFound_returns404() throws Exception {
        long nonexistentId = 999L;

        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/invitations/" + nonexistentId))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Team invitation link is no longer valid."));
    }

    @Test
    void acceptInvitation_alreadyAccepted_returns404() throws Exception {
        team.addRole(new Role(contractor, Skill.ELECTRICAL,  RoleStatus.ACCEPTED));
        team = teamsRepository.save(team);

        mockMvc.perform(post("/renovations/team/invitations/" + team.getId() + "/accept")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Unable to accept invitation, link is no longer valid."));
    }

    @Test
    void declineInvitation_alreadyAccepted_returns404n() throws Exception {
        team.addRole(new Role(contractor, Skill.ELECTRICAL,  RoleStatus.ACCEPTED));
        team = teamsRepository.save(team);

        mockMvc.perform(post("/renovations/team/invitations/" + team.getId() + "/decline")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Unable to decline invitation, link is no longer valid."));
    }

    @Test
    void acceptInvitation_notApartOfTeam_returnNotFound() throws Exception {
        mockMvc.perform(post("/renovations/team/invitations/" + team.getId() + "/accept")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Unable to accept invitation, link is no longer valid."));
    }

    @Test
    void declineInvitation_notApartOfTeam_returnNotFound() throws Exception {
        mockMvc.perform(post("/renovations/team/invitations/" + team.getId() + "/decline")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Unable to decline invitation, link is no longer valid."));
    }


    @Test
    @WithMockUser(username = "bob.doe@doe.nz")
    void viewInvitation_validTeam_returnsInfo() throws Exception {
        Team team1 = teamsRepository.findByRenovationRecord(renovationRecord);
        Role role1 = new Role(Skill.CARPENTRY);
        Contractor contractor1 = new Contractor("Bob", "Doe", "bob.doe@doe.nz", "password");
        contractor1 = userRepository.save(contractor1);
        role1.setContractor(contractor1);
        team1.addRole(role1);
        team1 = teamsRepository.save(team1);
        mockMvc.perform(get("/renovations/team/invitations/" + team1.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("skill", "Carpentry"))
                .andExpect(model().attribute("renovationName", "test renovation"))
                .andExpect(model().attribute("ownerName", "Jane Doe"))
                .andExpect(model().attribute("profilePicture", "default/default.jpg"))
                .andExpect(model().attribute("teamId", team1.getId()))
                .andExpect(model().attribute("renovationId", renovationRecord.getId()))
                .andExpect(view().name("joinTeamInbox"));
    }

    @Test
    @WithMockUser("jane@doe.nz")
    void inviteContractor_teamOwner() throws Exception {
        Role role = new Role(Skill.ACOUSTIC_INSULATION);
        team.addRole(role);
        team = teamsRepository.save(team);

        Long teamId = team.getId();
        Long contractorId = contractor.getId();
        Skill skill = role.getSkill();

        mockMvc.perform(post("/renovations/team/invitations/invite")
                        .param("teamId", teamId.toString())
                        .param("contractorId", contractorId.toString())
                        .param("skill", skill.toString())
                        .with(csrf()))
                .andExpect(status().isOk());

        Team currentTeam = teamsRepository.findByRenovationRecord(renovationRecord);
        List<Long> roleIds = currentTeam.getRoles().stream().map(Role::getContractorId).toList();

        Assertions.assertTrue(roleIds.contains(contractorId));
    }

    @Test
    void inviteContractor_userNotTeamOwner_returnNotFound() throws Exception {
        Role role = new Role(Skill.ACOUSTIC_INSULATION);
        team.addRole(role);
        team = teamsRepository.save(team);

        Long teamId = team.getId();
        Long contractorId = contractor.getId();
        Skill skill = role.getSkill();

        mockMvc.perform(post("/renovations/team/invitations/invite")
                        .param("teamId", teamId.toString())
                        .param("contractorId", contractorId.toString())
                        .param("skill", skill.toString())
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
