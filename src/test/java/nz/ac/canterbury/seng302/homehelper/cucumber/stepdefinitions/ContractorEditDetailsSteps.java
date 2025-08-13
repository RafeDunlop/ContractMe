package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.ContractorContext;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.mapper.AddressMapper;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@AutoConfigureMockMvc
@SpringBootTest
public class ContractorEditDetailsSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult mvcResult;

    private final ContractorContext contractorContext;

    private final AddressMapper addressMapper;

    private Float hourlyRate;
    private Integer countryCode;
    private String phoneNumber;
    private Set<Skill> skills;

    Map<String, String> EXPECTED_ERRORS = Map.of(
            "Your phone number is invalid", "phoneNumberError",
            "Invalid hourly rate", "hourlyRateError",
            "You must select one or more skills", "skillsError"
    );

    public ContractorEditDetailsSteps(ContractorContext contractorContext) {
        this.contractorContext = contractorContext;
        addressMapper = new AddressMapper();
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

    @Given("I make changes to the skills in the input field, and I have at least one skill in the input field")
    public void i_make_changes_to_the_skills_in_the_input_field_and_i_have_at_least_one_skill_in_the_input_field() {

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

        mvcResult = mockMvc.perform(post("/user/edit")
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
                        .param("skills", skills.stream().map(Skill::toString).toArray(String[]::new)))
                .andReturn();
    }

    @Then("I see fields for my skills, phone number, hourly rate, and location")
    public void i_see_fields_for_my_skills_phone_number_hourly_rate_and_location() throws UnsupportedEncodingException {
        UserRegisterDTO contractorDetails = (UserRegisterDTO) Objects.requireNonNull(mvcResult.getModelAndView()).getModel().get("contractorDTO");
        AddressDTO locationDetails  = (AddressDTO) Objects.requireNonNull(mvcResult.getModelAndView()).getModel().get("addressDTO");
        Contractor currentContractor = contractorContext.getContractor();
        AddressDTO currentLocation = addressMapper.mapLocationToAddressDTO(currentContractor.getLocation());

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
        for (String error : EXPECTED_ERRORS.keySet()) {
            if (Objects.equals(error, expectedError)) {
                Assertions.assertTrue(Objects.requireNonNull(mvcResult.getModelAndView()).getModel().containsKey(EXPECTED_ERRORS.get(error)));
                Assertions.assertEquals(expectedError, mvcResult.getModelAndView().getModel().get(EXPECTED_ERRORS.get(error)));
            }
        }
    }
}
