package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.ContractorContext;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.RoleStatus;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class ReRunAlgorithmSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RenovationRecordRepository renovationRecordRepository;

    @Autowired
    TeamsRepository teamsRepository;

    private Team team;
    private final ContractorContext contractorContext;
    private String ownerEmail;

    public ReRunAlgorithmSteps(ContractorContext contractorContext) {
        this.contractorContext = contractorContext;
    }

    @Before
    public void setUp() {
        ownerEmail = "Test" + System.nanoTime() + "@test.test";
        User owner = new User("Test", "test", ownerEmail, "test");
        owner.activate();
        userRepository.save(owner);

        RenovationRecord renovation = new RenovationRecord(owner, "Test renovation", "", new ArrayList<>());
        Location location = new Location("20 Kirkwood Avenue", "NZ", "8041", "Christchurch", "Riccarton", 43.53, 172.63);
        renovation.setLocation(location);
        renovationRecordRepository.save(renovation);

        team = new Team(renovation);
        teamsRepository.save(team);
    }

    @Given("A contractor has received an invitation for a role in a team")
    public void a_contractor_has_received_an_invitation_for_a_role_in_a_team() {
        team.addRole(new Role(contractorContext.getContractor(), Skill.ELECTRICAL, RoleStatus.WAITING));
        teamsRepository.save(team);
    }
    @When("The contractor rejects the invitation")
    public void the_contractor_rejects_the_invitation() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/team/invitations/" + team.getId() + "/decline")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Then("The contractor does not receive any more invitations to join a role on the team")
    public void the_contractor_does_not_receive_any_more_invitations_to_join_a_role_on_the_team() {
        Team updatedTeam = teamsRepository.findById(team.getId()).orElseThrow();

        Long currentContractorId = updatedTeam.getRoles().get(0).getContractorId();
        Long rejectedContractorId = contractorContext.getContractor().getId();

        assertNotEquals(rejectedContractorId, currentContractorId,
                "Rejected contractor invited again.");
    }

    @Given("That I am own a team with a contractor who has accepted")
    public void That_i_am_own_a_team_with_a_contractor_who_has_accepted() {
        team.addRole(new Role(contractorContext.getContractor(), Skill.ELECTRICAL, RoleStatus.ACCEPTED));
        teamsRepository.save(team);
    }

    @When("I remove a contractor from a role")
    public void i_remove_a_contractor_from_a_role() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/renovations/team/delete")
                        .param("teamId", String.valueOf(team.getId()))
                        .param("contractorId", String.valueOf(contractorContext.getContractor().getId()))
                        .with(user(ownerEmail).roles("USER"))   // <- use user(...)
                        .with(csrf())
        ).andExpect(status().isNoContent());
    }

    @Then("That contractor does not receive any more invitations to join a role on the team")
    public void That_contractor_does_not_receive_any_more_invitations_to_join_a_role_on_the_team() {
        Team updatedTeam = teamsRepository.findById(team.getId()).orElseThrow();
        Long removedId = contractorContext.getContractor().getId();

        boolean assigned = updatedTeam.getRoles().stream().anyMatch(
                role -> removedId != null && removedId.equals(role.getContractorId()));

        assertFalse(assigned, "Removed contractor was invited again.");
    }
}
