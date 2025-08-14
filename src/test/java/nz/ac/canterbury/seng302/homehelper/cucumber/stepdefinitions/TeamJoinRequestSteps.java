package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.ContractorContext;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureMockMvc
@WithMockUser
@SpringBootTest
@Transactional
public class TeamJoinRequestSteps {

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamsRepository teamsRepository;

    @Autowired private MockMvc mockMvc;

    private RenovationRecord renovationRecord;
    private User owner;
    private Team team;

    private Contractor contractor;
    private final ContractorContext contractorContext;
    private ResultActions resultActions;

    public TeamJoinRequestSteps(ContractorContext contractorContext) {
        this.contractorContext = contractorContext;
    }

    @Given("A private renovation exists with a team")
    public void a_private_renovation_exists_with_a_team() {
        owner = userRepository.save(new User("Greg", "smith", "greg@smith.com", "Password123!"));

        renovationRecord = new RenovationRecord(owner, "Test Renovation", "Test Desc", Collections.emptyList());
        renovationRecord.setPublicity(false);
        renovationRecord = renovationRecordRepository.save(renovationRecord);

        team = new Team(renovationRecord);
    }

    @Given("I am logged in and a contractor")
    public void i_am_logged_in_and_a_contractor() {
        contractor = contractorContext.getContractor();
        if (this.contractor == null) { throw new IllegalStateException("Contractor was not found"); }
    }

    @Given("my request to join the renovation team is {string}")
    public void my_request_to_join_the_renovation_team_is(String requestStatus) throws Exception {
        boolean accepted = switch (requestStatus.toLowerCase()) {
            case "accepted" -> true;
            case "pending"  -> false;
            default -> throw new Exception("Incorrect status: " + requestStatus);
        };

        team.addRole(new Role(contractor, Skill.ELECTRICAL, accepted));
        teamsRepository.save(team);
    }

    @When("I view the renovation")
    public void i_view_the_renovation() throws Exception {
        resultActions = mockMvc.perform(get("/renovations/view")
                                .param("id", Long.toString(renovationRecord.getId()))
                                .with(user(contractor.getEmail()).roles("USER","CONTRACTOR"))
                                .with(csrf()));

    }

    @Then("I can view the renovation record")
    public void i_can_view_the_renovation_record() throws Exception {
        resultActions.andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"));
    }
}
