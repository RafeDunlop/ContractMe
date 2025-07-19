package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class ContractorRegistrationSteps {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;



    @Given("I am on the registration form")
    public void i_am_on_the_registration_form() throws Exception {
        userRepository.findByEmailIgnoreCase("john.doe@example.com").ifPresent(user -> {
            verificationCodeRepository.deleteAll();
            userRepository.delete(user);
        });
        result = mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andReturn();

    }

    @When("I select the contractor button")
    public void i_select_the_contractor_button() throws UnsupportedEncodingException {
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("id=\"contractor-toggleswitch\""));
    }

    @Then("it includes fields where I can add my skills, phone number, and hourly rate")
    public void it_includes_fields_where_i_can_add_my_skills_phone_number_and_hourly_rate() throws UnsupportedEncodingException {
        String content = result.getResponse().getContentAsString();

        assertTrue(content.contains("id=\"contractor-form\""));
        assertTrue(content.contains("id=\"hourlyRate\""));
        assertTrue(content.contains("id=\"skills\""));
        assertTrue(content.contains("id=\"phoneNumber\""));
    }

    @When("I enter an invalid {string}")
    public void i_enter_an_invalid(String phoneNumber) throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .param("firstName", "Jane")
                .param("lastName", "Doe")
                .param("email", "jane.doe@example.com")
                .param("address_line1", "") // <- IMPORTANT: make sure param name matches controller!
                .param("suburb", "Riccarton")
                .param("city", "Christchurch")
                .param("postcode", "8041")
                .param("country", "New Zealand")
                .param("phoneNumber", phoneNumber)
                .param("skills", "HVAC")
                .param("hourlyRate", "1")
                .param("password", "P4$$word")
                .param("confirmPassword", "P4$$word")
                .with(csrf());

        result = mockMvc.perform(request)
                .andExpect(status().is3xxRedirection()).andReturn();
    }

    @When("an error message tells me {string}")
    public void an_error_message_tells_me(String errorMessage) throws UnsupportedEncodingException {
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains(errorMessage));
    }

}
