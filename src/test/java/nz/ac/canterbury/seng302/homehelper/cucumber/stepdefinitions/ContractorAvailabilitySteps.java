package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.ContractorContext;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class ContractorAvailabilitySteps {

    @Autowired
    private MockMvc mockMvc;

    private final ContractorContext contractorContext;

    private MvcResult result;


    public ContractorAvailabilitySteps(ContractorContext contractorContext) {
        this.contractorContext = contractorContext;
    }

    @Given("I am logged in as a contractor")
    public void given_i_am_logged_in_as_a_contractor() throws Exception {
        mockMvc.perform(get("/main").with(csrf())).andExpect(status().isOk()).andReturn();
    }

    @When("I am on the profile page and the job availability option is off")
    public void i_am_on_the_profile_page_and_the_job_availability_option_is_off() throws Exception {
        contractorContext.getContractor().setAvailable(false);
        result = mockMvc.perform(get("/user")
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();

        assertTrue(html.contains("id=\"availabilityCheckbox\""));
        assertFalse(html.contains("id=\"availabilityCheckbox\" checked"));


    }

    @When("I toggle the option to on")
    public void i_toggle_the_option_to_on() {

    }

    @Then("when I reload the page my job availability status is saved as available")
    public void when_i_reload_the_page_my_job_availability_status_is_saved_as_available() {

    }


    @When("I am on the profile page and the job availability option is on")
    public void i_am_on_the_profile_page_and_the_job_availability_option_is_on() {

    }

    @When("I toggle the option to off")
    public void i_toggle_the_option_to_off() {

    }

    @Then("when I reload the page my job availability status is saved as unavailable")
    public void when_i_reload_the_page_my_job_availability_status_is_saved_as_unavailable() {

    }

}
