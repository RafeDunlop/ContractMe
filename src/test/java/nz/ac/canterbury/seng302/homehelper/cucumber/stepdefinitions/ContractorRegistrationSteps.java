package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class ContractorRegistrationSteps {

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;
    private ResultActions resultActions;
    private String userEmailAddress;
    private String userName;
    private String userPhoneNumber;
    private Set<Skill> userSkills;



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
        assertTrue(content.contains("id=\"skills-select\""));
        assertTrue(content.contains("id=\"phoneNumber\""));
    }

    @When("I enter an invalid phone number {string}")
    public void i_enter_an_invalid_phone_number(String phoneNumber) throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .param("firstName", "Jane")
                .param("lastName", "Doe")
                .param("email", "jane.doe@example.com")
                .param("address_line1", "") // <- IMPORTANT: make sure param name matches controller!
                .param("suburb", "Riccarton")
                .param("city", "Christchurch")
                .param("postcode", "8041")
                .param("country", "New Zealand")
                .param("countryCode", "64")
                .param("phoneNumber", phoneNumber)
                .param("skills", "HVAC")
                .param("hourlyRate", "1")
                .param("password", "P4$$word")
                .param("confirmPassword", "P4$$word")
                .param("isContractor", "true")
                .with(csrf());

        resultActions = mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
    }

    @Then("a {string} error message tells me {string}")
    public void a_error_message_tells_me(String errorName, String errorMessage) throws Exception {
        resultActions.andExpect(flash().attribute(errorName, List.of(errorMessage)));
    }

    @When("I don't enter a phone number")
    public void i_don_t_enter_a_phone_number() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .param("firstName", "Jane")
                .param("lastName", "Doe")
                .param("email", "jane.doe@example.com")
                .param("address_line1", "") // <- IMPORTANT: make sure param name matches controller!
                .param("suburb", "Riccarton")
                .param("city", "Christchurch")
                .param("postcode", "8041")
                .param("country", "New Zealand")
                .param("countryCode", "64")
                .param("phoneNumber", "")
                .param("skills", "HVAC")
                .param("hourlyRate", "1")
                .param("password", "P4$$word")
                .param("confirmPassword", "P4$$word")
                .param("isContractor", "true")
                .with(csrf());

        resultActions = mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
    }

    @When("I don't enter location details")
    public void i_don_t_enter_location_details() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .param("firstName", "Jane")
                .param("lastName", "Doe")
                .param("email", "jane.doe@example.com")
                .param("address_line1", "") // <- IMPORTANT: make sure param name matches controller!
                .param("suburb", "")
                .param("city", "")
                .param("postcode", "")
                .param("country", "")
                .param("countryCode", "64")
                .param("phoneNumber", "12345678")
                .param("skills", "HVAC")
                .param("hourlyRate", "1")
                .param("password", "P4$$word")
                .param("confirmPassword", "P4$$word")
                .param("isContractor", "true")
                .with(csrf());

        resultActions = mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
    }

    @Given("I enter valid user details {string}, email address {string}, skills {string}, a phone number {string}, and a location")
    public void i_enter_valid_user_details_skills_a_phone_number_and_a_location(String name, String emailAddress, String skills, String phoneNumber) throws Exception {
        userEmailAddress = emailAddress;
        userName = name;
        userSkills = Set.of(Skill.valueOf(skills));
        userPhoneNumber = phoneNumber;
        MockHttpServletRequestBuilder request = post("/register")
                .param("firstName", name)
                .param("lastName", "Doe")
                .param("email", emailAddress)
                .param("address_line1", "1 Nice Street") // <- IMPORTANT: make sure param name matches controller!
                .param("suburb", "Riccarton")
                .param("city", "Christchurch")
                .param("postcode", "8041")
                .param("country", "New Zealand")
                .param("countryCode", "64")
                .param("phoneNumber", phoneNumber)
                .param("skills", skills)
                .param("hourlyRate", "1")
                .param("password", "P4$$word")
                .param("confirmPassword", "P4$$word")
                .param("isContractor", "true")
                .with(csrf());

        resultActions = mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
    }

    @Transactional
    @Then("the form is saved with the contractor details I supplied")
    public void the_form_is_saved_with_the_contractor_details_i_supplied() {
        Optional<Contractor> contractor = contractorRepository.findByEmailIgnoreCase(userEmailAddress);
        assertTrue(contractor.isPresent());
        Contractor actualContractor = contractor.get();
        assertEquals(userName, actualContractor.getFirstName());
        assertEquals(userEmailAddress, actualContractor.getEmail());
        assertEquals(userSkills, actualContractor.getSkills());
        assertEquals(userPhoneNumber, actualContractor.getPhoneNumber());
    }

    @When("I enter an invalid hourly rate {float}")
    public void i_enter_an_invalid_hourly_rate(Float hourlyRate) throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .param("firstName", "Jane")
                .param("lastName", "Doe")
                .param("email", "jane.doe@example.com")
                .param("address_line1", "") // <- IMPORTANT: make sure param name matches controller!
                .param("suburb", "Riccarton")
                .param("city", "Christchurch")
                .param("postcode", "8041")
                .param("country", "New Zealand")
                .param("countryCode", "64")
                .param("phoneNumber", "123455786")
                .param("skills", "HVAC")
                .param("hourlyRate", String.valueOf(hourlyRate))
                .param("password", "P4$$word")
                .param("confirmPassword", "P4$$word")
                .param("isContractor", "true")
                .with(csrf());

        resultActions = mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
    }

    @When("I don't enter any skills")
    public void i_dont_enter_any_skills() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .param("firstName", "Jane")
                .param("lastName", "Doe")
                .param("email", "jane.doe@example.com")
                .param("address_line1", "") // <- IMPORTANT: make sure param name matches controller!
                .param("suburb", "Riccarton")
                .param("city", "Christchurch")
                .param("postcode", "8041")
                .param("country", "New Zealand")
                .param("countryCode", "64")
                .param("phoneNumber", "123455786")
                .param("skills", (String) null)
                .param("hourlyRate", "1")
                .param("password", "P4$$word")
                .param("confirmPassword", "P4$$word")
                .param("isContractor", "true")
                .with(csrf());

        resultActions = mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
    }
}
