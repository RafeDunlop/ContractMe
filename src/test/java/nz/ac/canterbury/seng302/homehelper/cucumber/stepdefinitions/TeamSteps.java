package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class TeamSteps {

    UserContext userContext;

    private RenovationRecord renovationRecord;

    private MvcResult mvcResult;
    private ResultActions result;

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

    @When("I add zero roles")
    public void i_add_zero_roles() throws Exception {
        mvcResult =  mockMvc.perform(MockMvcRequestBuilders.post("/renovations/team/create?id=" + renovationRecord.getId())
                        .param("id", renovationRecord.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }


    @Then("An error message displays, telling me I must have at least one role")
    public void an_error_message_displays_telling_me_i_must_have_at_least_one_role() throws Exception {
        String html = mvcResult.getResponse().getContentAsString();

        assertTrue(html.contains("Your team request must have at least one role."),
                "Expected error message from validation");
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

}
