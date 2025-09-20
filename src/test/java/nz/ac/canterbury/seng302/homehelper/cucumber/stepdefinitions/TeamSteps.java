package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.ContractorContext;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.*;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class TeamSteps {
    UserContext userContext;
    ContractorContext contractorContext;
    private RenovationRecord renovationRecord;
    private MvcResult mvcResult;
    private ResultActions result;
    private final ContractorRepository contractorRepository;
    private final TeamsRepository teamsRepository;
    private final UserRepository userRepository;
    private Team team;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RenovationRecordRepository renovationRepository;

    public TeamSteps(UserContext userContext, ContractorContext contractorContext, ContractorRepository contractorRepository,
            TeamsRepository teamsRepository, UserRepository userRepository) {
        this.userContext = userContext;
        this.contractorContext = contractorContext;
        this.contractorRepository = contractorRepository;
        this.teamsRepository = teamsRepository;
        this.userRepository = userRepository;
    }

    @Given("I am on the view renovation page for a renovation I own that has a location listed and that doesn't have a team")
    public void i_am_on_the_view_renovation_page_for_a_renovation_i_own_that_has_a_location_listed_and_that_doesnt_not_have_a_team() {
        User user = userContext.getUser();
        renovationRecord = new RenovationRecord(user, "Test", "", List.of());
        Location location = new Location();
        location.setAddress("notNull");
        renovationRecord.setLocation(location);
        renovationRecord = renovationRepository.save(renovationRecord);
    }

    @When("I click the create team button")
    public void i_click_the_create_team_button() throws Exception {
        mvcResult =  mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/create")
                        .param("id", Long.toString(renovationRecord.getId()))
                )
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I can select roles for my renovation")
    public void i_can_select_roles_for_my_renovation() throws UnsupportedEncodingException {
        assertTrue(mvcResult.getResponse().getContentAsString().contains(
                "skills-select"
        ));
    }

    @When("I add zero skills")
    public void i_add_zero_skills() throws Exception {
        mvcResult =  mockMvc.perform(MockMvcRequestBuilders.post("/renovations/team/create?id=" + renovationRecord.getId())
                        .param("id", renovationRecord.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @When("I submit with more then five skills")
    public void i_submit_with_more_then_five_skills() throws Exception {
        mvcResult =  mockMvc.perform(MockMvcRequestBuilders.post("/renovations/team/create?id=" + renovationRecord.getId())
                        .param("id", renovationRecord.getId().toString())
                        .param("skills", "ELECTRICAL", "ELECTRICAL", "ELECTRICAL", "ELECTRICAL", "ELECTRICAL", "ELECTRICAL")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("An error message displays, {string}")
    public void an_error_message_displays(String errorMessage) throws Exception {
        String html = mvcResult.getResponse().getContentAsString();

        assertTrue(html.contains(errorMessage), "Expected error message from validation");
    }

    @Then("I can add the skill {string} twice to the same team")
    public void i_can_add_the_skill_twice_to_the_same_team(String skillName) throws Exception {
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/renovations/team/create")
                        .param("id", renovationRecord.getId().toString())
                        .param("skills", skillName, skillName)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + renovationRecord.getId()))
                .andReturn();
    }

    @When("I add a valid amount of skills")
    public void i_add_a_valid_amount_of_skills() throws Exception {
        result =  mockMvc.perform(MockMvcRequestBuilders.post("/renovations/team/create?id=" + renovationRecord.getId())
                .param("id", renovationRecord.getId().toString())
                .param("skills", "GAS_FITTING", "CNC_MACHINING")
                .with(csrf()));
    }
    @Then("My team request is successfully created")
    public void my_team_request_is_successfully_created() throws Exception {
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + renovationRecord.getId()));
    }

    @Transactional
    @Given("a contractor is assigned and has accepted a role in the team")
    public void a_contractor_is_assigned_and_has_accepted_a_role_in_the_team() {
        User user = userContext.getUser();
        renovationRecord = new RenovationRecord(user, "Test", "", List.of());
        renovationRecord = renovationRepository.save(renovationRecord);
        Team team = new Team(renovationRecord);
        Contractor alice = contractorRepository.save(new Contractor("Alice", "Builder", "alice@test.nz", "pw"));
        alice.setProfilePicture("alice.jpg");
        contractorRepository.save(alice);
        Role accepted = new Role(Skill.CARPENTRY);
        accepted.setContractor(alice);
        accepted.setStatus(RoleStatus.ACCEPTED);
        team.addRole(accepted);
        team = teamsRepository.save(team);
        this.team = team;
    }

    @Transactional
    @Given("a contractor is assigned and has not accepted a role in the team")
    public void a_contractor_is_assigned_and_has_not_accepted_a_role_in_the_team() {
        User user = userContext.getUser();
        renovationRecord = new RenovationRecord(user, "Test", "", List.of());
        renovationRecord = renovationRepository.save(renovationRecord);
        Team team = new Team(renovationRecord);
        Contractor alice = contractorRepository.save(new Contractor("Alice", "Builder", "alice2@test.nz", "pw"));
        alice.setProfilePicture("alice.jpg");
        contractorRepository.save(alice);
        Role role = new Role(Skill.CARPENTRY);
        role.setContractor(alice);
        role.setStatus(RoleStatus.WAITING);
        team.addRole(role);
        team = teamsRepository.save(team);
        this.team = team;
    }

    @Transactional
    @Given("a team has no contractors assigned")
    public void a_team_has_no_contractors_assigned() {
        User user = userContext.getUser();
        renovationRecord = new RenovationRecord(user, "Test", "", List.of());
        renovationRecord = renovationRepository.save(renovationRecord);
        Team team = new Team(renovationRecord);
        Role empty = new Role(Skill.PLUMBING);
        team.addRole(empty);
        team = teamsRepository.save(team);

        Contractor bob = contractorRepository.save(new Contractor("Bob", "Spark", "bob@test.nz", "pw"));
        bob.setProfilePicture("bob.jpg");
        contractorRepository.save(bob);
        Role pending = new Role(Skill.ELECTRICAL);
        pending.setContractor(bob);
        pending.setStatus(RoleStatus.WAITING);
        team.addRole(pending);
        this.team = team;

    }

    @When("I click the View Team button")
    public void i_click_the_view_team_button() throws Exception {
        mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/renovations/team/view")
                        .param("id", team.getId().toString())
                        .with(csrf())
        ).andExpect(status().isOk()).andReturn();
    }

    @Then("I see the contractor's name and profile picture")
    public void i_see_the_contractor_s_name_and_profile_picture() throws Exception {
        String html = mvcResult.getResponse().getContentAsString();
        assertTrue(html.contains("Alice Builder"));
        assertTrue(html.contains("alice.jpg"));
        assertTrue(html.contains("Carpentry"));
    }

    @Then("I see a placeholder")
    public void i_see_a_placeholder() throws Exception {
        String html = mvcResult.getResponse().getContentAsString();
        assertTrue(html.contains("Invite Sent!"));
        assertTrue(html.contains("bob.jpg"));
        assertTrue(html.contains("Electrical"));
        assertTrue(html.contains("Plumbing"));
    }

    @Transactional
    @Given("I am assigned to the team and have accepted a role in the team")
    public void i_am_assigned_to_the_team_and_have_accepted_a_role_in_the_team() {
        User owner = new User("Owner", "User", "owner"+System.currentTimeMillis()+"@test.nz", "pw");
        owner.activate();
        owner = userRepository.save(owner);

        Contractor contractor = contractorContext.getContractor();
        contractor.setFirstName("Alice");
        contractor.setLastName("Builder");
        contractor.setProfilePicture("alice.jpg");
        contractor = contractorRepository.save(contractor);

        renovationRecord = new RenovationRecord(owner, "Test", "", List.of());
        renovationRecord = renovationRepository.save(renovationRecord);

        Team team = new Team(renovationRecord);
        Role acceptedRole = new Role(Skill.CARPENTRY);
        acceptedRole.setContractor(contractor);
        acceptedRole.setStatus(RoleStatus.ACCEPTED);
        team.addRole(acceptedRole);
        team = teamsRepository.save(team);

        this.team = team;
    }


    @Transactional
    @Given("I am assigned to the team and have not accepted a role in the team")
    public void i_am_assigned_to_the_team_and_have_not_accepted_a_role_in_the_team() {
        Contractor contractor = contractorContext.getContractor();
        contractor.setFirstName("Alice");
        contractor.setLastName("Builder");
        contractor.setProfilePicture("alice.jpg");
        contractor = contractorRepository.save(contractor);

        renovationRecord = new RenovationRecord(contractor, "Test", "", List.of());
        renovationRecord = renovationRepository.save(renovationRecord);

        Team team = new Team(renovationRecord);
        Role acceptedRole = new Role(Skill.CARPENTRY);
        acceptedRole.setContractor(contractor);
        acceptedRole.setStatus(RoleStatus.ACCEPTED);
        team.addRole(acceptedRole);
        team = teamsRepository.save(team);

        this.team = team;
    }

    @Transactional
    @Given("I do not own the team")
    public void i_do_not_own_the_team() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String ownerEmail = "owner" + System.currentTimeMillis() + "@user.nz";
        User owner = new User("Owner", "User", ownerEmail, encoder.encode("Test123!"));
        owner.activate();
        userRepository.save(owner);

        renovationRecord = new RenovationRecord(owner, "Other Renovation", "", List.of());
        renovationRecord = renovationRepository.save(renovationRecord);
        team = new Team(renovationRecord);
        teamsRepository.save(team);
    }

    @When("I visit the team page")
    public void i_visit_the_team_page() throws Exception {
        mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/renovations/team/view")
                        .param("id", team.getId().toString())
                        .with(csrf())
        ).andReturn();
    }

    @Then("I get 404 error")
    public void i_get_404_error() throws Exception {
        int status = mvcResult.getResponse().getStatus();
        assertEquals(404, status);
    }

    @Transactional
    @Given("There is a public renovation I do not own")
    public void there_is_a_public_renovation_i_do_not_own() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String ownerEmail = "owner" + System.currentTimeMillis() + "@user.nz";
        User owner = new User("Owner", "User", ownerEmail, encoder.encode("Test123!"));
        owner.activate();
        userRepository.save(owner);

        renovationRecord = new RenovationRecord(owner, "Public Renovation", "", List.of());
        renovationRecord.setPublicity(true);
        renovationRecord = renovationRepository.save(renovationRecord);
    }

    @When("I visit the renovation record")
    public void i_visit_the_renovation_record() throws Exception {
        mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/renovations/view")
                        .param("id", renovationRecord.getId().toString())
                        .with(csrf())
        ).andExpect(status().isOk()).andReturn();
    }

    @Then("I can not see the view team button")
    public void i_can_not_see_the_view_team_button() throws Exception {
        String html = mvcResult.getResponse().getContentAsString();
        assertFalse(html.contains("View Team"));
    }

    @Transactional
    @Given("I am not assigned to a role in the team")
    public void i_am_not_assigned_to_a_role_in_the_team() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String uniqueEmail = "test" + System.currentTimeMillis() + "@user.nz";
        User user = new User("Test", "User", uniqueEmail, encoder.encode("Test123!"));
        user.activate();
        userRepository.save(user);

        renovationRecord = new RenovationRecord(user, "Team Without Me", "", List.of());
        renovationRepository.save(renovationRecord);

        team = new Team(renovationRecord);
        Role role = new Role(Skill.CARPENTRY);

        Contractor other = new Contractor("Other", "Contractor", "other@test.nz", "pw");
        contractorRepository.save(other);
        role.setContractor(other);
        role.setStatus(RoleStatus.ACCEPTED);

        team.addRole(role);
        teamsRepository.save(team);
    }

    @Transactional
    @Given("There is a public renovation I am not assigned to")
    public void public_renovation_not_assigned() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String ownerEmail = "owner" + System.currentTimeMillis() + "@user.nz";
        User owner = new User("Owner", "User", ownerEmail, encoder.encode("Test123!"));
        owner.activate();
        userRepository.save(owner);

        renovationRecord = new RenovationRecord(owner, "Public Renovation", "", List.of());
        renovationRecord.setPublicity(true);
        renovationRepository.save(renovationRecord);
    }
}
