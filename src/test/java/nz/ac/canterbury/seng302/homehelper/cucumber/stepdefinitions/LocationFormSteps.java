package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;


import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class LocationFormSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private MvcResult result;

    @Given("I am on the edit profile form")
    public void i_am_on_the_edit_profile_form() throws Exception {
        MockHttpServletRequestBuilder request = get("/user/edit")
                .with(user("jane.doe@example.com").roles("USER"));

        result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

    }

    @Given("I am on the register form")
    public void i_am_on_the_register_form() throws Exception {
        User user = new User("Jane", "Doe", "jane.doe@example.com", "password");
        user.grantAuthority("ROLE_USER");
        userRepository.save(user);

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

    @Given("I am viewing the enter location details form on the {string} page")
    public void i_am_viewing_the_enter_location_details_form_on_the_page(String endPoint) throws Exception {
        MockHttpServletRequestBuilder request = get(endPoint)
                .with(csrf());

        if (endPoint.equals("/user/edit")) {
            request.with(user("jane.doe@example.com").roles("USER"));
        }

        result = mockMvc.perform(request)
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

    @When("I leave the address field blank but fill any other field on the location form on the {string} page")
    public void i_leave_the_address_field_blank_but_fill_any_other_field_on_the_location_form_on_the_page(String endpoint) throws Exception {
        MockHttpServletRequestBuilder request = post(endpoint)
                .param("firstName", "Jane")
                .param("lastName", "Doe")
                .param("email", "jane.doe@example.com")
                .param("address_line1", "") // <- IMPORTANT: make sure param name matches controller!
                .param("suburb", "Riccarton")
                .param("city", "Christchurch")
                .param("postcode", "8041")
                .param("country", "New Zealand")
                .with(csrf());

        switch (endpoint) {
            case "/register":
                request = request
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!");
                break;

            case "/user/edit":
                request = request.with(user("jane.doe@example.com").roles("USER"));
                break;

            default:
                throw new IllegalArgumentException("Unsupported endpoint: " + endpoint);
        }

        result = mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }




    @Then("I am told that I must supply an address field")
    public void i_am_told_that_i_must_supply_an_address_field() throws Exception {
        String content = result.getResponse().getContentAsString();
        //assertTrue(content.contains("Cannot submit location without an address"));
    }


    @When("I enter a valid address and submit the location form on the {string} page")
    public void i_enter_a_valid_address_and_submit_the_location_form_on_the_page(String endpoint) throws Exception {
        MockHttpServletRequestBuilder request = post(endpoint)
                .param("firstName", "Jane")
                .param("lastName", "Doe")
                .param("email", "jane.doe@example.com")
                .param("address_line1", "200 Riccarton Road")
                .param("suburb", "Riccarton")
                .param("city", "Christchurch")
                .param("postcode", "8041")
                .param("country", "New Zealand")
                .with(csrf());

        // Endpoint specific params
        switch (endpoint) {
            case "/register":
                request.param("password", "Test123!")
                        .param("confirmPassword", "Test123!");
                break;

            case "/user/edit":
                request.with(user("jane.doe@example.com").roles("USER"));
                break;

            default:
                throw new IllegalArgumentException("Unsupported endpoint: " + endpoint);
        }

        result = mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    @Then("The form from the {string} page is saved and contains the address I supplied")
    public void the_form_from_the_page_is_saved_and_contains_the_address_i_supplied(String endpoint) {
        // Due to no database queries being defined for the scope of this task, this step cannot be completed yet
    }



}
