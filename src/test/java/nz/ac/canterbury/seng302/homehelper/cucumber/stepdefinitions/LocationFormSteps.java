package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.FlashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Objects;

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

    @When("I enter an valid postcode: {string}")
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

    @Then("a message tells me that Postcode contains invalid characters")
    public void a_message_tells_me_that_postcode_contains_invalid_characters() throws Exception {
        resultActions
                .andExpect(flash().attribute("postcodeError", List.of("Postcode contains invalid characters.")));
    }

    @Then("I am taken to the confirm register page")
    public void i_am_taken_to_the_confirm_register_page() throws Exception {
        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).contains("/confirm-registration"));
    }
}
