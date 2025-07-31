package nz.ac.canterbury.seng302.homehelper.e2e;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.junit.jupiter.api.Assertions;

public class EditProfileStepsE2e {
    private final UserContext userContext;

    public EditProfileStepsE2e(UserContext userContext) {
        this.userContext = userContext;
    }

    @Given("I am on my profile page")
    public void i_am_on_my_profile_page() {
        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/user");
    }

    @When("I click the Edit button")
    public void i_click_on_edit_button() {
        RunPlaywrightTests.page.locator("#edit-button").click();
    }

    @Then("I see the edit profile form with all my details prepopulated except my password")
    public void i_see_the_edit_profile_form_with_all_my_details_prepopulated_except_my_password() {
        String registrationUrl = RunPlaywrightTests.baseUrl + "/user/edit";
        String currentUrl = RunPlaywrightTests.page.url();
        Assertions.assertEquals(registrationUrl, currentUrl);

        User user = userContext.getUser();

        String userFirstName = user.getFirstName();
        String firstNameFieldValue = RunPlaywrightTests.page.locator("#first-name").inputValue();
        Assertions.assertEquals(userFirstName, firstNameFieldValue);

        String userLastName = user.getLastName();
        String lastNameFieldValue = RunPlaywrightTests.page.locator("#last-name").inputValue();
        Assertions.assertEquals(userLastName, lastNameFieldValue);

        String userEmail = user.getEmail();
        String emailFieldValue = RunPlaywrightTests.page.locator("#email").inputValue();
        Assertions.assertEquals(userEmail, emailFieldValue);

        Location userLocation = user.getLocation();
        String addressFieldValue = RunPlaywrightTests.page.locator("#address").inputValue();
        Assertions.assertEquals(userLocation.getAddress(), addressFieldValue);
        String suburbFieldValue = RunPlaywrightTests.page.locator("#suburb").inputValue();
        Assertions.assertEquals(userLocation.getSuburb(), suburbFieldValue);
        String cityFieldValue = RunPlaywrightTests.page.locator("#city").inputValue();
        Assertions.assertEquals(userLocation.getCity(), cityFieldValue);
        String postcodeFieldValue = RunPlaywrightTests.page.locator("#postcode").inputValue();
        Assertions.assertEquals(userLocation.getPostcode(), postcodeFieldValue);
        String countryFieldValue = RunPlaywrightTests.page.locator("#country").inputValue();
        Assertions.assertEquals(userLocation.getCountry(), countryFieldValue);

    }

}
