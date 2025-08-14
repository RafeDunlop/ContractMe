package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.ContractorContext;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@AutoConfigureMockMvc
@SpringBootTest
public class ContractorEditDetailsSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult mvcResult;

    private final ContractorContext contractorContext;

    private Float hourlyRate;
    private Integer countryCode;
    private String phoneNumber;
    private Set<Skill> skills;

    Map<String, String> expectedErrors = Map.of(
            "Your phone number is invalid", "phoneNumberError",
            "You must enter a phone number", "phoneNumberError",
            "Invalid hourly rate", "hourlyRateError",
            "You must select one or more skills", "skillsError"
    );

    public ContractorEditDetailsSteps(ContractorContext contractorContext) {
        this.contractorContext = contractorContext;
    }

    private void resetInputs() {
        hourlyRate = null;
        countryCode = null;
        phoneNumber = null;
        skills = null;
    }

    @Given("I am editing the contractor details")
    public void i_am_editing_the_contractor_details() {
        resetInputs();
    }

    @Given("I enter the phone number {string}")
    public void i_enter_the_phone_number_and_submit_the_form(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Given("I add {string} to the skills input")
    public void i_add_to_the_skills_input(String skillsString) {
        skills = Arrays.stream(skillsString.split(","))
                .map(Skill::valueOf)
                .collect(Collectors.toSet());
    }

    @Given("I don't have any skills in the input field")
    public void i_dont_have_any_skills_in_the_input_field() {
        skills = Set.of();
    }

    @Given("I enter an hourly rate {float}")
    public void i_enter_an_hourly_rate(float hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    @When("I click the edit profile button")
    public void i_click_the_edit_profile_button() throws Exception {
        mvcResult = mockMvc.perform(get("/user/edit"))
                .andReturn();
    }

    @When("I submit the edit profile form")
    public void i_submit_the_edit_profile_form() throws Exception {
        Contractor contractor = contractorContext.getContractor();
        hourlyRate = Objects.requireNonNullElse(hourlyRate, contractor.getHourlyRate());
        countryCode = Objects.requireNonNullElse(countryCode, contractor.getCountryCode());
        phoneNumber = Objects.requireNonNullElse(phoneNumber, contractor.getPhoneNumber());
        skills = Objects.requireNonNullElse(skills, contractor.getSkills());

        MockHttpServletRequestBuilder requestBuilder = post("/user/edit")
                        .param("firstName", contractor.getFirstName())
                        .param("lastName", contractor.getLastName())
                        .param("email", contractor.getEmail())
                        .param("address_line1", contractor.getLocation().getAddress())
                        .param("region", contractor.getLocation().getSuburb())
                        .param("city", contractor.getLocation().getCity())
                        .param("postcode", contractor.getLocation().getPostcode())
                        .param("country", contractor.getLocation().getCountry())
                        .param("lat", Double.toString(contractor.getLocation().getLatitude()))
                        .param("lon", Double.toString(contractor.getLocation().getLongitude()))
                        .param("hourlyRate", hourlyRate.toString())
                        .param("countryCode", countryCode.toString())
                        .param("phoneNumber", phoneNumber)
                        .with(csrf());

        if (!skills.isEmpty()) {
            requestBuilder.param("skills", skills.stream().map(Skill::toString).collect(Collectors.joining(",")));
        }

        mvcResult = mockMvc.perform(requestBuilder)
                .andReturn();
    }

    @Then("I see fields for my skills, phone number, hourly rate, and location")
    public void i_see_fields_for_my_skills_phone_number_hourly_rate_and_location() throws UnsupportedEncodingException {
        UserRegisterDTO contractorDetails = (UserRegisterDTO) Objects.requireNonNull(mvcResult.getModelAndView()).getModel().get("contractorDTO");
        AddressDTO locationDetails  = (AddressDTO) Objects.requireNonNull(mvcResult.getModelAndView()).getModel().get("addressDTO");
        Contractor currentContractor = contractorContext.getContractor();
        AddressDTO currentLocation = new AddressDTO();
        currentLocation.setFromLocation(currentContractor.getLocation());

        Assertions.assertEquals(currentContractor.getSkills(), new HashSet<>(contractorDetails.getSkills()));
        Assertions.assertEquals(currentContractor.getPhoneNumber(), contractorDetails.getPhoneNumber());
        Assertions.assertEquals(currentContractor.getHourlyRate(), contractorDetails.getHourlyRate());
        Assertions.assertEquals(currentLocation, locationDetails);

        List<String> formInputIds = List.of("id=\"hourlyRate\"", "id=\"phoneNumber\"", "id=\"skills-select\"", "id=\"location-form\"");
        String formContent = mvcResult.getResponse().getContentAsString();
        for (String formInputId : formInputIds) {
            Assertions.assertTrue(formContent.contains(formInputId));
        }
    }

    @Then("I don't see the fields for my skills, phone number, and hourly rate")
    public void i_dont_see_the_fields_for_my_skills_phone_number_and_hourly_rate() throws UnsupportedEncodingException {
        UserRegisterDTO contractorDetails = (UserRegisterDTO) Objects.requireNonNull(mvcResult.getModelAndView()).getModel().get("contractorDTO");
        Assertions.assertNull(contractorDetails);

        List<String> formInputIds = List.of("id=\"hourlyRate\"", "id=\"phoneNumber\"", "id=\"skills-select\"");
        String formContent = mvcResult.getResponse().getContentAsString();
        for (String formInputId : formInputIds) {
            Assertions.assertFalse(formContent.contains(formInputId));
        }
    }

    @Then("An error message tells me {string}")
    public void an_error_message_tells_me(String expectedError) {
        for (String error : expectedErrors.keySet()) {
            if (Objects.equals(error, expectedError)) {
                Assertions.assertTrue(Objects.requireNonNull(mvcResult.getFlashMap()).containsKey(expectedErrors.get(error)));
                Assertions.assertEquals(List.of(expectedError), mvcResult.getFlashMap().get(expectedErrors.get(error)));
            }
        }
    }

    @Then("My skills are updated to the new values")
    public void my_skills_are_updated_to_the_new_values() {
        System.out.println("hello");
    }
}
