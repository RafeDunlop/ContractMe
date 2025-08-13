package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.ContractorContext;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.mapper.AddressMapper;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@AutoConfigureMockMvc
@SpringBootTest
public class ContractorEditDetailsSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult mvcResult;

    private ContractorContext contractorContext;

    private UserContext userContext;

    private AddressMapper addressMapper;

    public ContractorEditDetailsSteps(ContractorContext contractorContext, UserContext userContext) {
        this.contractorContext = contractorContext;
        this.userContext = userContext;
        addressMapper = new AddressMapper();
    }

    @Given("I am editing the contractor details")
    public void i_am_editing_the_contractor_details() {
    }

    @When("I click the edit profile button")
    public void i_click_the_edit_profile_button() throws Exception {
        mvcResult = mockMvc.perform(get("/user/edit"))
                .andReturn();
    }

    @When("I enter the phone number {string} and submit the form")
    public void i_enter_the_phone_number_and_submit_the_form(String phoneNumber) {

    }

    @Then("I see fields for my skills, phone number, hourly rate, and location")
    public void i_see_fields_for_my_skills_phone_number_hourly_rate_and_location() {
        UserRegisterDTO contractorDetails = (UserRegisterDTO) Objects.requireNonNull(mvcResult.getModelAndView()).getModel().get("contractorDTO");
        AddressDTO locationDetails  = (AddressDTO) Objects.requireNonNull(mvcResult.getModelAndView()).getModel().get("addressDTO");
        Contractor currentContractor = contractorContext.getContractor();
        AddressDTO currentLocation = addressMapper.mapLocationToAddressDTO(currentContractor.getLocation());

        Assertions.assertEquals(currentContractor.getSkills(), new HashSet<>(contractorDetails.getSkills()));
        Assertions.assertEquals(currentContractor.getPhoneNumber(), contractorDetails.getPhoneNumber());
        Assertions.assertEquals(currentContractor.getHourlyRate(), contractorDetails.getHourlyRate());

        Assertions.assertEquals(currentLocation, locationDetails);
    }

    @Then("I don't see the fields for my skills, phone number, and hourly rate")
    public void i_dont_see_the_fields_for_my_skills_phone_number_and_hourly_rate() {

    }

    @Then("An error message tells me {string}")
    public void an_error_message_tells_me(String message) {

    }
}
