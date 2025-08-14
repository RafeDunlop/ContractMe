package nz.ac.canterbury.seng302.homehelper.e2e;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class EditProfileStepsE2e {
    private UserContext userContext;

    @Autowired
    private UserRepository userRepository;

    public EditProfileStepsE2e(UserContext userContext) {
        this.userContext = userContext;
    }

    @Given("I am on my profile page")
    public void i_am_on_my_profile_page() {
        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/user");
    }

    @Then("I see the edit profile form with all my details prepopulated except my password")
    public void i_see_the_edit_profile_form_with_all_my_details_prepopulated_except_my_password() {
        String editProfileUrl = RunPlaywrightTests.baseUrl + "/user/edit";
        String currentUrl = RunPlaywrightTests.page.url();
        Assertions.assertEquals(editProfileUrl, currentUrl);

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

    @Given("I am on the edit profile form")
    public void i_am_on_the_edit_profile_form() {
        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/user/edit");
    }

    @When("I enter valid values for my first name, last name, and email address")
    public void i_enter_valid_values_for_my_first_name_last_name_and_email_address() {
        RunPlaywrightTests.page.locator("#first-name").fill("John");
        RunPlaywrightTests.page.locator("#last-name").fill("Smith");
        RunPlaywrightTests.page.locator("#email").fill("test@example.com");
    }

    @When("I click on the {string} button")
    public void i_click_on_the_button(String buttonName) {
        RunPlaywrightTests.page.locator("#" + buttonName.toLowerCase() + "-button").click();
    }

    @Then("my new details are saved")
    public void my_new_details_are_saved() {
        Optional<User> optionalUpdatedUser = userRepository.findById(userContext.getUser().getId());
        User updatedUser = optionalUpdatedUser.get();
        Assertions.assertEquals("John", updatedUser.getFirstName());
        Assertions.assertEquals("Smith", updatedUser.getLastName());
        Assertions.assertEquals("test@example.com", updatedUser.getEmail());
    }

    @Then("I am taken back to my profile page")
    public void i_am_taken_back_to_my_profile_page() {
        String profileUrl = RunPlaywrightTests.baseUrl + "/user";
        String currentUrl = RunPlaywrightTests.page.url();
        Assertions.assertEquals(profileUrl, currentUrl);
    }

    @When("I enter an empty first name")
    public void i_enter_an_empty_first_name() {
        RunPlaywrightTests.page.locator("#first-name").fill("");
    }

    @Then("an error message tells me First name cannot be empty")
    public void an_error_message_tells_me_First_name_cannot_by_empty() {
        String expectedErrorMessage = "First name cannot be empty.";
        String errorMessage = RunPlaywrightTests.page.locator("#first-name-backend-error").locator("ul").innerText();
        Assertions.assertEquals(expectedErrorMessage, errorMessage);
    }

    @When("no changes are saved")
    public void no_changes_are_saved() {
        Optional<User> optionalUpdatedUser = userRepository.findById(userContext.getUser().getId());
        User updatedUser = optionalUpdatedUser.get();
        Assertions.assertEquals(userContext.getUser(), updatedUser);

    }

    @When("I enter an invalid first name")
    public void i_enter_an_invalid_first_name() {
        RunPlaywrightTests.page.locator("#first-name").fill("#$%^&");
    }

    @Then("an error message tells me First name must only include letters, spaces, hyphens, or apostrophes")
    public void an_error_message_tells_me_First_name_must_only_include_letters_and_spaces_or_hyphens_or_apostrophes() {
        String expectedErrorMessage = "First name must only include letters, spaces, hyphens, or apostrophes.";
        String errorMessage = RunPlaywrightTests.page.locator("#first-name-backend-error").locator("ul").innerText();
        Assertions.assertEquals(expectedErrorMessage, errorMessage);
    }

    @When("I enter an invalid last name")
    public void i_enter_an_invalid_last_name() {
        RunPlaywrightTests.page.locator("#last-name").fill("#$%^&");
    }

    @Then("an error message tells me Last name must only include letters, spaces, hyphens, or apostrophes")
    public void an_error_message_tells_me_Last_name_must_only_include_letters_and_spaces_or_hyphens_or_apostrophes() {
        String expectedErrorMessage = "Last name must only include letters, spaces, hyphens, or apostrophes.";
        String errorMessage = RunPlaywrightTests.page.locator("#last-name-backend-error").locator("ul").innerText();
        Assertions.assertEquals(expectedErrorMessage, errorMessage);
    }

    @Given("I enter an first name that is more than 64 characters")
    public void i_enter_an_first_name_that_is_more_than_64_characters() {
        RunPlaywrightTests.page.locator("#first-name").fill("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    }

    @Then("an error message tells me First name must be 64 characters long or less")
    public void an_error_message_tells_me_First_name_must_only_include_64_characters_long_or_less() {
        String expectedErrorMessage = "First name must be 64 characters long or less.";
        String errorMessage = RunPlaywrightTests.page.locator("#first-name-backend-error").locator("ul").innerText();
        Assertions.assertEquals(expectedErrorMessage, errorMessage);
    }

    @Given("I enter an last name that is more than 64 characters")
    public void i_enter_an_last_name_that_is_more_than_64_characters() {
        RunPlaywrightTests.page.locator("#last-name").fill("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    }

    @Then("an error message tells me Last name must be 64 characters long or less")
    public void an_error_message_tells_me_Last_name_must_only_include_64_characters_long_or_less() {
        String expectedErrorMessage = "Last name must be 64 characters long or less.";
        String errorMessage = RunPlaywrightTests.page.locator("#last-name-backend-error").locator("ul").innerText();
        Assertions.assertEquals(expectedErrorMessage, errorMessage);
    }

    @Given("I enter an invalid email: {string}")
    public void i_enter_an_invalid_email(String email) {
        RunPlaywrightTests.page.locator("#email").fill(email);
    }

    @Then("an error message tells me Email address must be in the form ‘jane@doe.nz’")
    public void an_error_message_tells_me_Email_address_must_only_include_email_address() {
        String expectedErrorMessage = "Email address must be in the form 'jane@doe.nz'.";
        String errorMessage = RunPlaywrightTests.page.locator("#email-backend-error").locator("ul").innerText();
        Assertions.assertEquals(expectedErrorMessage, errorMessage);
    }


    @Given("I enter an email address associated to an account that already exists")
    public void I_enter_an_email_address_associated_to_an_account_that_exists() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String email = "test" + System.currentTimeMillis() + "b@user.nz";
        User user = new User("Second", "User", email, encoder.encode("Test123!"));
        user.activate();
        userRepository.save(user);
        RunPlaywrightTests.page.locator("#email").fill(email);
    }

    @Then("an error message tells me This email address is already in use")
    public void an_error_message_tells_me_this_email_address_is_already_in_use() {
        String expectedErrorMessage = "This email address is already in use.";
        String errorMessage = RunPlaywrightTests.page.locator("#email-backend-error").locator("ul").innerText();
        Assertions.assertEquals(expectedErrorMessage, errorMessage);
    }

}
