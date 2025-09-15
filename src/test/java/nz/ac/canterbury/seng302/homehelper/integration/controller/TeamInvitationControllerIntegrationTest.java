package nz.ac.canterbury.seng302.homehelper.integration.controller;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "jane@doe.nz")
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
        contractor = userRepository.save(contractor);
        contractor.grantAuthority("ROLE_USER");

        team = new Team(renovationRecord);
        team = teamsRepository.save(team);
    }

    @Test
    @WithMockUser(username = "Steve@doe.nz")
    void viewInvitation_contractorOnTeam_alreadyAccepted_returns404() throws Exception {
        team.addRole(new Role(contractor, Skill.ELECTRICAL, true));
        team = teamsRepository.save(team);

        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/invitations/" + team.getId())
                        .param("id", Long.toString(renovationRecord.getId())))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Team invitation link is no longer valid."));
    }

    @Test
    @WithMockUser(username = "Steve@doe.nz")
    void viewInvitation_contractorNotOnTeam_returns404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/invitations/" + team.getId())
                        .param("id", Long.toString(renovationRecord.getId())))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "Steve@doe.nz")
    void viewInvitation_teamNotFound_returns404() throws Exception {
        long nonexistentId = 999L;

        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/invitations/" + nonexistentId))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Team invitation link is no longer valid."));
    }

    @Test
    @WithMockUser(username = "Steve@doe.nz")
    void acceptInvitation_alreadyAccepted_returns404() throws Exception {
        team.addRole(new Role(contractor, Skill.ELECTRICAL, true));
        team = teamsRepository.save(team);

        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/team/invitations/" + team.getId() + "/accept")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Unable to accept invitation, link is no longer valid."));
    }

    @Test
    @WithMockUser(username = "Steve@doe.nz")
    void declineInvitation_alreadyAccepted_returns404n() throws Exception {
        team.addRole(new Role(contractor, Skill.ELECTRICAL, true));
        team = teamsRepository.save(team);

        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/team/invitations/" + team.getId() + "/decline")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Unable to decline invitation, link is no longer valid."));
    }

    @Test
    @WithMockUser(username = "Steve@doe.nz")
    void acceptInvitation_notApartOfTeam_returnNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/team/invitations/" + team.getId() + "/accept")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Unable to accept invitation, link is no longer valid."));
    }

    @Test
    @WithMockUser(username = "Steve@doe.nz")
    void declineInvitation_notApartOfTeam_returnNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/team/invitations/" + team.getId() + "/decline")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Unable to decline invitation, link is no longer valid."));
    }


    @Test
    @WithMockUser(username = "bob.doe@doe.nz")
    void viewInvitation_validTeam_returnsInfo() throws Exception {
        Team team = teamsRepository.findByRenovationRecord(renovationRecord);
        Role role = new Role(Skill.CARPENTRY);
        Contractor contractor = new Contractor("Bob", "Doe", "bob.doe@doe.nz", "password");
        contractor = userRepository.save(contractor);
        role.setContractor(contractor);
        team.addRole(role);
        team = teamsRepository.save(team);
        mockMvc.perform(get("/renovations/team/invitations/" + team.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("skill", "Carpentry"))
                .andExpect(model().attribute("renovationName", "test renovation"))
                .andExpect(model().attribute("ownerName", "Jane Doe"))
                .andExpect(model().attribute("profilePicture", "default/default.jpg"))
                .andExpect(model().attribute("teamId", team.getId()))
                .andExpect(model().attribute("renovationId", renovationRecord.getId()))
                .andExpect(view().name("joinTeamInbox"));
    }
}
