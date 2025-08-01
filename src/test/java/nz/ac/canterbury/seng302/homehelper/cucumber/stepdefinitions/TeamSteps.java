package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class TeamSteps {

    UserContext userContext;

    private RenovationRecord renovationRecord;

    private MvcResult mvcResult;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RenovationRecordRepository renovationRepository;

    public TeamSteps(UserContext userContext) {
        this.userContext = userContext;
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
}
