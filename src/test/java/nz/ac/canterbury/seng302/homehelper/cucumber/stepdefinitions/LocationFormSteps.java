package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class LocationFormSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Given("I am on the register form")
    public void i_am_on_the_register_form() throws Exception {
        result = mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @When("I click the location toggle switch")
    public void i_click_the_location_toggle_switch() throws Exception {
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("id=\"location-toggleswitch\""));
    }

    @Then("I can see the add location input fields")
    public void i_can_see_the_add_location_input_fields() throws Exception {
        String content = result.getResponse().getContentAsString();

        assertTrue(content.contains("id=\"location-form\""));
        assertTrue(content.contains("id=\"address\""));
        assertTrue(content.contains("id=\"suburb\""));
        assertTrue(content.contains("id=\"city\""));
        assertTrue(content.contains("id=\"postcode\""));
        assertTrue(content.contains("id=\"country\""));
    }

    @Given("I am viewing the enter location details form on the register page")
    public void i_am_viewing_the_enter_location_details_on_the_register_page() throws Exception {
        result = mockMvc.perform(get("/register")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        assertTrue(content.contains("id=\"location-form\""));
        assertTrue(content.contains("id=\"address\""));
        assertTrue(content.contains("id=\"suburb\""));
        assertTrue(content.contains("id=\"city\""));
        assertTrue(content.contains("id=\"postcode\""));
        assertTrue(content.contains("id=\"country\""));
    }

    @When("I leave the address field blank on the location form on the register page")
    public void i_leave_the_address_field_blank_on_the_register_page() throws Exception {
        result = mockMvc.perform(post("/register")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john.doe@example.com")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .param("address", "")
                        .param("country", "New Zealand")
                        .param("postcode", "8041")
                        .param("city", "Wellington")
                        .param("suburb", "Central")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    @Then("I am told that I must supply an address field")
    public void i_am_told_that_i_must_supply_an_address_field() throws Exception {
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("must supply an address field"));
    }
}
