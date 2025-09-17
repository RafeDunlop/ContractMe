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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    @Autowired
    ContractorRepository contractorRepository;

    private Team team;
    private final ContractorContext contractorContext;
    private Contractor secondContractor;

    public ReRunAlgorithmSteps(ContractorContext contractorContext) {
        this.contractorContext = contractorContext;
    }

    @Before
    public void setUp() {
        String uniqueEmail = "Test" + System.nanoTime() + "@test.test";

        User user = new User("Test", "test", uniqueEmail, "test");
        user.activate();
        userRepository.save(user);

        RenovationRecord renovation = new RenovationRecord(user, "Test renovation", "", new ArrayList<>());
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

        contractorRepository.save(secondContractor);
    }

    @Then("the next closest eligible contractor receives an invitation to join that role")
    public void the_next_closest_eligible_contractor_receives_an_invitation_to_join_that_role() {
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

}