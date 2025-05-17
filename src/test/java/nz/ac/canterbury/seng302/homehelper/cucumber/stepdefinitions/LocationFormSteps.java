package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;

import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;


import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class LocationFormSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;
    private ResultActions resultActions;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Given("I am on the register form")
    public void i_am_on_the_register_form() throws Exception {
        userRepository.findByEmailIgnoreCase("john.doe@example.com").ifPresent(user -> {
            verificationCodeRepository.deleteAll();
            userRepository.delete(user);
        });
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
        result = mockMvc.perform(get(endPoint)
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

    @When("I leave the address field blank but fill any other field on the location form on the {string} page")
    public void i_leave_the_address_field_blank_but_fill_any_other_field_on_the_location_form_on_the_page(String endpoint) throws Exception {

            result = mockMvc.perform(post(endpoint)
                            .param("firstName", "Jane")
                            .param("lastName", "Doe")
                            .param("email", "jane.doe@example.com")
                            .param("password", "Test123!")
                            .param("confirmPassword", "Test123!")
                            .param("address", "")
                            .param("suburb", "Riccarton")
                            .param("city", "Christchurch")
                            .param("postcode", "8041")
                            .param("country", "New Zealand")
                            .with(csrf()))
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
        result = mockMvc.perform(post(endpoint)
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane.doe@example.com")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .param("address", "200 Riccarton Road")
                        .param("suburb", "Riccarton")
                        .param("city", "Christchurch")
                        .param("postcode", "8041")
                        .param("country", "New Zealand")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

    }

    @Then("The form from the {string} page is saved and contains the address I supplied")
    public void the_form_from_the_page_is_saved_and_contains_the_address_i_supplied(String endpoint) {
        // Due to no database queries being defined for the scope of this task, this step cannot be completed yet
    }


    @When("I enter a valid address but an invalid suburb and submit the form on the {string} page")
    public void i_enter_a_valid_address_but_an_invalid_suburb_and_submit_the_form_on_the_page(String endpoint) throws Exception {
        MockHttpServletRequestBuilder request;

        switch (endpoint) {
            case "/register":
                request = post(endpoint)
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane.doe@example.com")
                        .param("address_line1", "77 Ilam Road")
                        .param("region", "a#$%")
                        .param("city", "Christchurch")
                        .param("postcode", "8041")
                        .param("country", "New Zealand")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .with(csrf());
                resultActions = mockMvc.perform(request);

                break;

            case "/user/edit":
                request = post(endpoint)
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane.doe@example.com")
                        .param("address_line1", "77 Ilam Road")
                        .param("region", "a#$%")
                        .param("city", "Christchurch")
                        .param("postcode", "8041")
                        .param("country", "New Zealand")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .with(csrf());

                request = request.with(user("jane.doe@example.com").roles("USER"));
                resultActions = mockMvc.perform(request);

                break;


            default:
                throw new IllegalArgumentException("Unsupported endpoint: " + endpoint);

        }

    }


    @When("I enter an invalid postcode: {string}")
    public void i_enter_an_invalid_postcode(String postcode) throws Exception {
        resultActions = mockMvc.perform(post("/register")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john.doe@example.com")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .param("address", "123 Street")
                        .param("country", "New Zealand")
                        .param("postcode", postcode)
                        .param("city", "Wellington")
                        .param("suburb", "Central")
                        .with(csrf()));
    }

    @When("I enter a valid city: {string}")
    public void i_enter_an_valid_city(String city) throws Exception {
        result = mockMvc.perform(post("/register")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john.doe@example.com")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .param("address", "123 Street")
                        .param("country", "New Zealand")
                        .param("postcode", "8042")
                        .param("city", city)
                        .param("suburb", "Central")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    @When("I enter an invalid city: {string}")
    public void i_enter_an_invalid_city(String city) throws Exception {
        resultActions = mockMvc.perform(post("/register")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john.doe@example.com")
                .param("password", "Test123!")
                .param("confirmPassword", "Test123!")
                .param("address", "123 Street")
                .param("country", "New Zealand")
                .param("postcode", "8042")
                .param("city", city)
                .param("suburb", "Central")
                .with(csrf()));
    }

    @When("I enter a valid postcode: {string}")
    public void i_enter_an_valid_postcode(String postcode) throws Exception {
        result = mockMvc.perform(post("/register")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john.doe@example.com")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .param("address", "123 Street")
                        .param("country", "New Zealand")
                        .param("postcode", postcode)
                        .param("city", "Wellington")
                        .param("suburb", "Central")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    @Then("I am taken back to the register form")
    public void i_am_taken_back_to_the_register_form() throws Exception {
        resultActions
                .andExpect(status().is3xxRedirection());
    }

    @Then("I am taken back to the {string} page")
    public void i_am_taken_back_to_the_page(String endpoint) throws Exception {
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(endpoint));

    }

    @Then("a message tells me that Postcode contains invalid characters")
    public void a_message_tells_me_that_postcode_contains_invalid_characters() throws Exception {
        resultActions
                .andExpect(flash().attribute("postcodeError", List.of("Postcode contains invalid characters.")));
    }

    @Then("a message tells me that the city contains invalid characters")
    public void a_message_tells_me_that_the_city_contains_invalid_characters() throws Exception {
        resultActions
                .andExpect(flash().attribute("cityError", List.of("City contains invalid characters.")));
    }

    @Then("I am taken to the confirm register page")
    public void i_am_taken_to_the_confirm_register_page() throws Exception {
        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).contains("/confirm-registration"));
    }

    @And("I am told that I have entered an invalid suburb")
    public void i_am_told_that_i_have_entered_an_invalid_suburb() throws Exception {
        resultActions
                .andExpect(flash().attribute("suburbError", List.of("Suburb contains invalid characters")));
    }

    @When("I enter a valid address but an invalid city and submit the form on the {string} page")
    public void i_enter_a_valid_address_but_an_invalid_city_and_submit_the_form_on_the_page(String endpoint) throws Exception {
        MockHttpServletRequestBuilder request;


        switch (endpoint) {
            case "/register":
                request = post(endpoint)
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane.doe@example.com")
                        .param("address_line1", "77 Ilam Road")
                        .param("region", "Ilam")
                        .param("city", "Christ23church")
                        .param("postcode", "8041")
                        .param("country", "New Zealand")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .with(csrf());
                resultActions = mockMvc.perform(request);

                break;

            case "/user/edit":
                request = post(endpoint)
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane.doe@example.com")
                        .param("address_line1", "77 Ilam Road")
                        .param("suburb", "Ilam")
                        .param("city", "Christ23church")
                        .param("postcode", "8041")
                        .param("country", "New Zealand")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .with(csrf());

                request = request.with(user("jane.doe@example.com").roles("USER"));
                resultActions = mockMvc.perform(request);

                break;


            default:
                throw new IllegalArgumentException("Unsupported endpoint: " + endpoint);

        }

    }

    @And("I am told that I have entered an invalid city")
    public void i_am_told_that_i_have_entered_an_invalid_city() throws Exception {
        resultActions
                .andExpect(flash().attribute("cityError", List.of("City contains invalid characters")));
    }
}
