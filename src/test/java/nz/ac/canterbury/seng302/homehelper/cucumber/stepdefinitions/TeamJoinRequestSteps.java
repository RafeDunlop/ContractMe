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
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private EmailService emailService;

    public TeamJoinRequestSteps(ContractorContext contractorContext) {
        this.contractorContext = contractorContext;
    }

    @Given("A team request has been created for a renovation which has an available role")
    public void a_team_request_has_been_created_for_a_renovation_which_has_an_available_role() {

        Role role = new Role(Skill.valueOf(Skill.PLUMBING.toString()));
        team = new Team(renovationRecord);
        team.addRole(role);
        teamsRepository.save(team);
        assertNull(team.getRoles().get(0).getContractor());

    }

    @Given("A private renovation exists with a team")
    public void a_private_renovation_exists_with_a_team() {
        owner = userRepository.save(new User("Greg", "smith", "greg" + System.currentTimeMillis() + "@smith.com", "Password123!"));

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

    @When("There is an available contractor eligible for that role")
    public void there_is_an_available_contractor_eligible_for_that_role() {
        contractor = contractorContext.getContractor();
        if (this.contractor == null) { throw new IllegalStateException("Contractor was not found"); }
        contractor.setSkills(Set.of(Skill.PLUMBING));
        contractor.setAvailable(true);

    }

    @When("I view the renovation")
    public void i_view_the_renovation() throws Exception {
        resultActions = mockMvc.perform(get("/renovations/view")
                                .param("id", Long.toString(renovationRecord.getId()))
                                .with(user(contractor.getEmail()).roles("USER","CONTRACTOR"))
                                .with(csrf()));

    }

    @Then("The system will automatically send an email to the contractor who is closest to the renovation location")
    public void the_system_will_automatically_send_an_email_to_the_contractor_who_is_closest_to_the_renovation_location() {

    }

    @Then("I can view the renovation record")
    public void i_can_view_the_renovation_record() throws Exception {
        resultActions.andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"));
    }
}
