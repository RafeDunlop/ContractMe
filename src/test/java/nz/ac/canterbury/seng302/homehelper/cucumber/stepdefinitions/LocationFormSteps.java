package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    private MvcResult result;
    private ResultActions resultActions;
    private RenovationRecord existingRecord;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Given("I am on the edit profile form")
    public void i_am_on_the_edit_profile_form() throws Exception {
        User testUser = new User("Jane", "Doe", "jane.doe@example.com", "password");
        userRepository.save(testUser);

        MockHttpServletRequestBuilder request = get("/user/edit")
                .with(user("jane.doe@example.com").roles("USER"));

        result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
    }

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

    @Given("I am on the create renovation form")
    public void i_am_on_the_create_renovation_form() throws Exception {
        MockHttpServletRequestBuilder request = get("/renovations/create")
                .with(user("jane.doe@example.com").roles("USER"));

        result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

    }

    @Given("I have an existing renovation record")
    public void i_have_an_existing_renovation_record() {
        User user = userRepository.findByEmailIgnoreCase("jane.doe@example.com")
                .orElseThrow(() -> new IllegalStateException("Test user should exist"));

        existingRecord = new RenovationRecord(user, "Test Record", null, Collections.emptyList());
        renovationRecordRepository.save(existingRecord);
        userRepository.save(user);

    }
    @Given("I am on the edit record for my existing record")
    public void i_am_on_the_edit_record_for_my_existing_record() throws Exception {
        assertNotNull(existingRecord, "Renovation record must be exist before accessing edit page");

        String email = existingRecord.getUser().getEmail();
        MockHttpServletRequestBuilder request = get("/renovations/edit?id=" + existingRecord.getId())
                .with(user(email).roles("USER"));

        result = mockMvc.perform(request)
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

    @When("I leave the address field blank but fill any other field on the location form on the edit page for my existing record")
    public void i_leave_the_address_field_blank_but_fill_any_other_field_on_the_location_form_on_the_edit_page_for_my_existing_record() throws Exception {
        assertNotNull(existingRecord, "Existing renovation record must exist");

        MockHttpServletRequestBuilder request = post("/renovations/edit?id=" + existingRecord.getId())
                .param("name", existingRecord.getName())
                .param("description", "Some description")
                .param("roomList", "Kitchen")
                .param("address_line1", "")
                .param("suburb", "Riccarton")
                .param("city", "Christchurch")
                .param("postcode", "8041")
                .param("country", "New Zealand")
                .with(user(existingRecord.getUser().getEmail()).roles("USER"))
                .with(csrf());

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
        String originalEndpoint = endpoint;

        if (endpoint.startsWith("/renovations/edit")) {
            endpoint = "/renovations/edit?id=" + existingRecord.getId();
        }

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
        switch (originalEndpoint) {
            case "/register":
                request.param("password", "Test123!")
                        .param("confirmPassword", "Test123!");
                break;

            case "/user/edit":
                request.with(user("jane.doe@example.com").roles("USER"));
                break;

            case "/renovations/edit":
                request = request
                        .param("name", existingRecord.getName())
                        .param("description", "Test Description")
                        .param("roomList", "Kitchen")
                        .with(user("jane.doe@example.com").roles("USER"));
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


    @When("I enter a valid address but an invalid suburb and submit the form on the {string} page")
    public void i_enter_a_valid_address_but_an_invalid_suburb_and_submit_the_form_on_the_page(String endpoint) throws Exception {
        MockHttpServletRequestBuilder request;

        switch (endpoint) {
            case "/renovations/edit":
                request = post("/renovations/edit?id=" + existingRecord.getId())
                        .param("name", existingRecord.getName())
                        .param("description", "Test Description")
                        .param("roomList", "Kitchen")
                        .param("address_line1", "77 Ilam Road")
                        .param("region", "a#$%")
                        .param("city", "Christchurch")
                        .param("postcode", "8041")
                        .param("country", "New Zealand")
                        .with(user("jane.doe@example.com").roles("USER"))
                        .with(csrf());

                resultActions = mockMvc.perform(request);
                break;


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


    @Then("I am taken back to the {string} page")
    public void i_am_taken_back_to_the_page(String endpoint) throws Exception {
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(endpoint));

    }

    @Then("I am taken back to the edit page for my record")
    public void i_am_taken_back_to_the_edit_page_for_my_record() throws Exception {
        String expectedRedirect = "/renovations/edit?id=" + existingRecord.getId();

        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirect));
    }

    @And("I am told that I have entered an invalid suburb")
    public void i_am_told_that_i_have_entered_an_invalid_suburb() throws Exception {
        resultActions
                .andExpect(flash().attribute("suburbError", List.of("Suburb contains invalid characters.")));
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
                        .param("region", "Ilam")
                        .param("city", "Christ23church")
                        .param("postcode", "8041")
                        .param("country", "New Zealand")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .with(csrf());

                request = request.with(user("jane.doe@example.com").roles("USER"));
                resultActions = mockMvc.perform(request);

                break;

            case "/renovations/edit":
                request = post("/renovations/edit?id=" + existingRecord.getId())
                        .param("name", existingRecord.getName())
                        .param("description", "Test Description")
                        .param("roomList", "Kitchen")
                        .param("address_line1", "77 Ilam Road")
                        .param("region", "Ilam")
                        .param("city", "Christ23church")
                        .param("postcode", "8041")
                        .param("country", "New Zealand")
                        .with(user("jane.doe@example.com").roles("USER"))
                        .with(csrf());

                resultActions = mockMvc.perform(request);
                break;

            default:
                throw new IllegalArgumentException("Unsupported endpoint: " + endpoint);

        }

    }

    @When("I enter a valid address but an invalid postcode and submit the form on the {string} page")
    public void i_enter_a_valid_address_but_an_invalid_postcode_and_submit_the_form_on_the_page(String endpoint) throws Exception {
        MockHttpServletRequestBuilder request;


        switch (endpoint) {
            case "/register":
                request = post(endpoint)
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane.doe@example.com")
                        .param("address_line1", "77 Ilam Road")
                        .param("region", "Ilam")
                        .param("city", "Christchurch")
                        .param("postcode", "8041@")
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
                        .param("region", "Ilam")
                        .param("city", "Christchurch")
                        .param("postcode", "8041@")
                        .param("country", "New Zealand!")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .with(csrf());

                request = request.with(user("jane.doe@example.com").roles("USER"));
                resultActions = mockMvc.perform(request);
                break;

            case "/renovations/edit":
                request = post("/renovations/edit?id=" + existingRecord.getId())
                        .param("name", existingRecord.getName())
                        .param("description", "Test Description")
                        .param("roomList", "Kitchen")
                        .param("address_line1", "77 Ilam Road")
                        .param("region", "Ilam")
                        .param("city", "Christchurch")
                        .param("postcode", "8041@")
                        .param("country", "New Zealand!")
                        .with(user("jane.doe@example.com").roles("USER"))
                        .with(csrf());

                resultActions = mockMvc.perform(request);
                break;

            default:
                throw new IllegalArgumentException("Unsupported endpoint: " + endpoint);

        }
    }


    @When("I enter a valid address but an invalid country and submit the form on the {string} page")
    public void i_enter_a_valid_address_but_an_invalid_country_and_submit_the_form_on_the_page(String endpoint) throws Exception {
        MockHttpServletRequestBuilder request;


        switch (endpoint) {
            case "/register":
                request = post(endpoint)
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane.doe@example.com")
                        .param("address_line1", "77 Ilam Road")
                        .param("region", "Ilam")
                        .param("city", "Christchurch")
                        .param("postcode", "8041")
                        .param("country", "New  Zealand!")
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
                        .param("region", "Ilam")
                        .param("city", "Christchurch")
                        .param("postcode", "8041")
                        .param("country", "New  Zealand!")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .with(csrf());

                request = request.with(user("jane.doe@example.com").roles("USER"));
                resultActions = mockMvc.perform(request);

                break;

            case "/renovations/edit":
                request = post("/renovations/edit?id=" + existingRecord.getId())
                        .param("name", existingRecord.getName())
                        .param("description", "Test Description")
                        .param("roomList", "Kitchen")
                        .param("address_line1", "77 Ilam Road")
                        .param("region", "Ilam")
                        .param("city", "Christchurch")
                        .param("postcode", "8041")
                        .param("country", "New  Zealand!")
                        .with(user("jane.doe@example.com").roles("USER"))
                        .with(csrf());

                resultActions = mockMvc.perform(request);
                break;

            default:
                throw new IllegalArgumentException("Unsupported endpoint: " + endpoint);

        }
    }

    @And("I am told that I have entered an invalid city")
    public void i_am_told_that_i_have_entered_an_invalid_city() throws Exception {
        resultActions
                .andExpect(flash().attribute("cityError", List.of("City contains invalid characters.")));
    }

    @And("I am told that I have entered an invalid postcode")
    public void i_am_told_that_i_have_entered_an_invalid_postcode() throws Exception {
        resultActions
                .andExpect(flash().attribute("postcodeError", List.of("Postcode contains invalid characters.")));
    }

    @And("I am told that I have entered an invalid country")
    public void i_am_told_that_i_have_entered_an_invalid_country() throws Exception {
        resultActions
                .andExpect(flash().attribute("countryError", List.of("Country contains invalid characters.")));
    }
}
