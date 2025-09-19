package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.ContractorContext;
import nz.ac.canterbury.seng302.homehelper.dto.TeamRequestDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
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
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Autowired
    @SpyBean
    private TeamsService teamsService;

    @Autowired
    private MockMvc mockMvc;

    private RenovationRecord renovationRecord;
    private Team team;
    private Contractor contractor;
    private final ContractorContext contractorContext;
    private ResultActions resultActions;
    private AutoCloseable autoCloseable;

    @Mock
    private EmailService emailService;

    public TeamJoinRequestSteps(ContractorContext contractorContext) {
        this.contractorContext = contractorContext;
    }

    @Before
    public void openMocks() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }


    @After
    public void releaseMocks() throws Exception {
        autoCloseable.close();
    }

    @Given("There is an available contractor eligible for that role")
    public void there_is_an_available_contractor_eligible_for_that_role() {
        contractor = contractorContext.getContractor();
        if (this.contractor == null) { throw new IllegalStateException("Contractor was not found"); }
        ReflectionTestUtils.setField(teamsService, "emailService", emailService);
        assertTrue(contractor.getSkills().contains(Skill.CARPENTRY));
    }

    @When("A team request has been created for a renovation which has an available role")
    public void a_team_request_has_been_created_for_a_renovation_which_has_an_available_role() throws Exception {
        User owner = userRepository.save(new User("Eve", "Smith", "eve" + System.currentTimeMillis() + "@smith.com", "Password123!"));
        renovationRecord = new RenovationRecord(owner, "Test Renovation", "Test Desc", Collections.emptyList());
        Location location = new Location("77 Ilam Road", "", "", "", "", -43.522345, 172.580907);
        renovationRecord.setLocation(location);
        renovationRecord.setPublicity(false);
        renovationRecord = renovationRecordRepository.save(renovationRecord);

        mockMvc.perform(post("/renovations/team/create")
                .param("skills", Skill.CARPENTRY.toString())
                .param("id", renovationRecord.getId().toString()));
    }

    @Then("The system will automatically send an email to the contractor who is closest to the renovation location")
    public void the_system_will_automatically_send_an_email_to_the_contractor_who_is_closest_to_the_renovation_location() {
        TeamRequestDTO teamRequestDTO = new TeamRequestDTO();
        teamRequestDTO.setSkills(List.of(Skill.CARPENTRY.toString()));
        teamsService.createNewTeam(renovationRecord, teamRequestDTO);
        Mockito.verify(emailService, times(1)).sendRequestToContractor(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Locale.class),Mockito.anyLong());
    }

    @Given("A private renovation exists with a team")
    public void a_private_renovation_exists_with_a_team() {
        User owner = userRepository.save(new User("Greg", "smith", "greg" + System.currentTimeMillis() + "@smith.com", "Password123!"));

        renovationRecord = new RenovationRecord(owner, "Test Renovation", "Test Desc", Collections.emptyList());
        renovationRecord.setPublicity(false);

        Location location = new Location("Test", "NZ", "Christchurch", "suburb", "Riccarton", 43.53, 172.63);
        renovationRecord.setLocation(location);

        renovationRecord = renovationRecordRepository.save(renovationRecord);

        team = new Team(renovationRecord);
        team = teamsRepository.save(team);
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

    @When("^I click the \"(accept|decline)\" button$")
    public void i_click_the_button(String button) throws Exception {
        resultActions = mockMvc.perform(post("/renovations/team/invitations/" + team.getId() + "/" + button)
                    .with(user(contractor.getEmail()).roles("USER", "CONTRACTOR"))
                    .with(csrf()));

    }

    @Then("I am taken to the renovation page")
    public void i_am_taken_to_the_renovation_page() throws Exception {
        String expectedUrl = "/renovations/view?id=" + renovationRecord.getId();
        resultActions.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedUrl));
    }


    @Then("I am taken to the view requests page")
    public void i_am_taken_to_the_view_requests_page() throws Exception {
        String expectedUrl = "/view-requests";
        resultActions.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedUrl));
    }

    @Then("I am in the team")
    public void i_am_in_the_team() {
        team = teamsRepository.findById(team.getId()).orElseThrow();

        boolean inTeam = team.getRoles().stream()
                .anyMatch(r -> r.getContractorId() != null
                        && r.getContractorId().equals(contractor.getId()));
        assertTrue(inTeam);
    }

    @Then("I am not in the team")
    public void i_am_not_in_the_team() {
        team = teamsRepository.findById(team.getId()).orElseThrow();

        boolean inTeam = team.getRoles().stream()
                .anyMatch(r -> r.getContractorId() != null &&
                        r.getContractorId().equals(contractor.getId()));
        assertFalse(inTeam);
    }

    @Given("I am shown an error page displaying {string}")
    public void i_am_shown_an_error_page_displaying(String error) throws Exception {
        resultActions.andExpect(status().isNotFound())
                .andExpect(status().reason(error));
    }
}
