package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.ContractorContext;
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
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.TeamInvitationService;

import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("cucumber")
public class ReRunAlgorithmSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeamsService teamsService;



    @Autowired
    UserRepository userRepository;

    @Autowired
    RenovationRecordRepository renovationRecordRepository;

    @Autowired
    TeamsRepository teamsRepository;

    @Autowired
    ContractorRepository contractorRepository;

    private Team team;
    private final ContractorContext contractorContext;
    private String ownerEmail;
    private Contractor secondContractor;
    @Autowired
    private TeamInvitationService teamInvitationService;

    public ReRunAlgorithmSteps(ContractorContext contractorContext) {
        this.contractorContext = contractorContext;
    }


    @Before
    public void setUp() {
        String uniqueEmail = "Test" + System.nanoTime() + "@test.test";
        ownerEmail = "Test" + System.nanoTime() + "@test.test";
        User owner = new User("Test", "test", ownerEmail, "test");
        owner.activate();
        userRepository.save(owner);

        User user = new User("Test", "test", uniqueEmail, "test");
        user.activate();
        userRepository.save(user);

        RenovationRecord renovation = new RenovationRecord(owner, "Test renovation", "", new ArrayList<>());
        Location location = new Location("20 Kirkwood Avenue", "NZ", "8041", "Christchurch", "Riccarton", 43.53, 172.63);
        renovation.setLocation(location);
        renovationRecordRepository.save(renovation);

        team = new Team(renovation);
        teamsRepository.save(team);
    }


    @Given("Another eligible contractor exists for that role")
    public void Another_eligible_contractor_exists_for_that_role() {
        String email = "contractor" + System.nanoTime() + "@test.test";
        secondContractor = new Contractor("Bob", "Backup", email, "pw");

        secondContractor.addSkill(Skill.ELECTRICAL);
        Location location = new Location("20 Kirkwood Avenue", "NZ", "8041", "Christchurch", "Riccarton", 43.53, 172.63);
        secondContractor.setLocation(location);
        secondContractor.setAvailable(true);

        contractorRepository.save(secondContractor);
    }

    @Then("The next closest eligible contractor receives an invitation to join that role")
    public void The_next_closest_eligible_contractor_receives_an_invitation_to_join_that_role() {
        Team updatedTeam = teamsRepository.findById(team.getId()).orElseThrow();
        Role role = updatedTeam.getRoles().get(0);

        assertNotEquals(contractorContext.getContractor().getId(), role.getContractorId(), "Rejected contractor was re-invited.");
        assertEquals(secondContractor.getId(), role.getContractorId(), "Next eligible contractor was not invited.");
    }

    @Given("A contractor has received an invitation for a role in a team")
    public void a_contractor_has_received_an_invitation_for_a_role_in_a_team() {
        team.addRole(new Role(contractorContext.getContractor(), Skill.ELECTRICAL, false));
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
        team.addRole(new Role(contractorContext.getContractor(), Skill.ELECTRICAL, false));
        team.getRoles().get(0).setAccepted(true);
        teamsRepository.save(team);
    }

    @When("I remove a contractor from a role")
    public void i_remove_a_contractor_from_a_role() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/renovations/team/delete")
                        .param("teamId", String.valueOf(team.getId()))
                        .param("contractorId", String.valueOf(contractorContext.getContractor().getId()))
                        .with(user(ownerEmail).roles("USER"))
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

    @Given("That i have a team with a role that no contractor is eligible to fill")
    public void that_i_have_a_team_with_a_role_that_is_eligible_to_fill() {
        Role empty = new Role(Skill.PLUMBING);
        team.addRole(empty);
        teamsRepository.save(team);
    }

    @When("A contractor becomes eligible to fill the role")
    public void a_contractor_becomes_eligible_to_fill_the_role() {
        Contractor contractor = contractorContext.getContractor();
        contractor.getLocation().setLongitude(172.63);
        contractor.getLocation().setLatitude(43.53);
        contractor.addSkill(Skill.PLUMBING);
        contractorRepository.save(contractor);
    }

    @Then("An email invitation is sent to that contractor after no more than 10 minutes")
    public void an_email_invitation_is_sent_to_that_contractor_after_no_more_than_10_minutes() {
        teamInvitationService.rerunAlgorithm();
        verify(teamsService).sendContractorEmails(
                argThat(t -> t != null && t.getId() != null && t.getId().equals(team.getId()))
        );
    }

}