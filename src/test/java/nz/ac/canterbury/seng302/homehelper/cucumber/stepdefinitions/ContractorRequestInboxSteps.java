package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.*;
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
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.util.List;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ContractorRequestInboxSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private TeamsRepository teamsRepository;

    private MvcResult mvcResult;

    private List<Team> expectedTeams;

    private final ContractorContext contractorContext;
    @Autowired
    private TeamsService teamsService;

    public ContractorRequestInboxSteps(ContractorContext contractorContext) {
        this.contractorContext = contractorContext;
    }

    @Given("I have received a team request from another user")
    public void i_have_received_a_team_request_from_another_user() {
        User renovationOwner = new User("John", "Smith", "john.smith@email.com", "password");
        renovationOwner.grantAuthority("ROLE_USER");
        renovationOwner.activate();
        renovationOwner = userRepository.save(renovationOwner);

        RenovationRecord teamRenovationRecord = new RenovationRecord(renovationOwner, "Team Renovation", "Description", List.of());
        teamRenovationRecord = renovationRecordRepository.save(teamRenovationRecord);

        Role role = new Role(contractorContext.getContractor(), Skill.HVAC, false);
        Team team = new Team(teamRenovationRecord);
        team.addRole(role);
        team = teamsRepository.save(team);
        expectedTeams = List.of(team);
    }

    @When("I click the view requests button in the navigation page")
    public void i_click_the_view_requests_button_in_the_navigation_page() throws Exception {
        mvcResult = mockMvc.perform(get("/view-requests"))
                .andReturn();
    }

    @Then("I am taken to the contractor team request inbox where I can see requests from clients")
    public void i_am_taken_to_the_contractor_team_request_inbox_where_i_can_see_requests_from_clients() {
        Object returnedObject = Objects.requireNonNull(mvcResult.getModelAndView()).getModel().get("teams");
        Assertions.assertInstanceOf(List.class, returnedObject);

        List<?> returnedTeams = (List<?>) returnedObject;

        Team expectedTeam = expectedTeams.get(0);
        Team returnedTeam = (Team) returnedTeams.get(0);
        Assertions.assertEquals(expectedTeams.size(), returnedTeams.size());
        Assertions.assertEquals(expectedTeam.getId(), returnedTeam.getId());
        Assertions.assertEquals(expectedTeam.getRenovationRecord().getId(), returnedTeam.getRenovationRecord().getId());

        Role expectedRole = expectedTeam.getRoles().get(0);
        Role returnedRole = returnedTeam.getRoles().get(0);
        Assertions.assertEquals(expectedRole.getContractor(), returnedRole.getContractor());
        Assertions.assertEquals(expectedRole.getSkill(), returnedRole.getSkill());
    }

    @Then("I am redirected to the main page")
    public void i_am_redirected_to_the_main_page() {
        Assertions.assertEquals("/main", mvcResult.getResponse().getRedirectedUrl());
    }

    @Given("I have not received any requests")
    public void i_have_not_received_any_requests() {
        Contractor contractor = contractorContext.getContractor();
        Assertions.assertTrue(teamsService.getContractorTeamRequests(contractor).isEmpty());
    }

    @Then("I see a message telling me that I have not received any requests yet")
    public void i_see_a_message_telling_me_that_i_have_not_received_any_requests_yet() throws Exception {
        String content = mvcResult.getResponse().getContentAsString();
        Assertions.assertTrue(content.contains("Your inbox is empty."));
    }
}
