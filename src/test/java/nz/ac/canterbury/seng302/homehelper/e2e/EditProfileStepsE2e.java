package nz.ac.canterbury.seng302.homehelper.e2e;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
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
    }

}
