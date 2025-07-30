package nz.ac.canterbury.seng302.homehelper.e2e;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

public class LoginStepsE2e {

    @Given("I connect to the system's main URL")
    public void i_connect_to_the_systems_main_url() {
        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl);
    }

    @Given("I am on the login form")
    public void i_am_on_the_login_form() {
        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/login");
    }

    @Given("I enter an email address {string}")
    public void i_enter_a_malformed_email_address(String email) {
        RunPlaywrightTests.page.locator("#username").fill(email);
    }

    @Given("I enter an email address that is unknown to the system")
    public void i_enter_an_email_address_that_is_unknown_to_the_system() {
        RunPlaywrightTests.page.locator("#username").fill("this.email@doesnt.exist");
    }

    @When("I see the homepage")
    public void i_see_the_homepage() {
        String expectedTitle = "Welcome to Home Helper";
        String homeTitle = RunPlaywrightTests.page.locator("#home-title").locator("h1").innerText();
        Assertions.assertEquals(expectedTitle, homeTitle);
    }

    @When("I click the \"Sign in\" button")
    public void i_click_the_sign_in_button() {
        RunPlaywrightTests.page.locator("#sign-in-button").click();
    }

    @When("I click a highlighted link with the text \"Not registered? Create an account\"")
    public void i_click_a_highlighted_link_with_the_text_not_registered_create_an_account() {
        RunPlaywrightTests.page.locator("#registration-page-link").click();
    }

    @When("I click the \"Cancel\" button")
    public void i_click_the_cancel_button() {
        RunPlaywrightTests.page.locator("#cancel-button").click();
    }

    @Then("It indicates a button labelled \"Sign in\"")
    public void it_indicates_a_button_labelled() {
        String expectButtonName = "Sign in";
        String buttonName = RunPlaywrightTests.page.locator("#login-button").innerText();
        Assertions.assertEquals(expectButtonName, buttonName);
    }

    @Then("An error message tells me \"Email address must be in the form 'jane@doe.nz'.\"")
    public void an_error_message_tells_me_email_address_must_be_in_the_form() {
        String expectedErrorMessage = "Email address must be in the form 'jane@doe.nz'.";
        String errorMessage = RunPlaywrightTests.page.locator("#email-backend-error").locator("ul").innerText();
        Assertions.assertEquals(expectedErrorMessage, errorMessage);
    }

    @Then("An error message tells me \"The email address is unknown, or the password is invalid.\"")
    public void an_error_message_tells_me_the_email_address_is_unknown_or_the_password_is_invalid() {
        String expectErrorMessage = "The email address is unknown, or the password is invalid.";
        String errorMessage = RunPlaywrightTests.page.locator("#general-error").innerText();
        Assertions.assertEquals(expectErrorMessage, errorMessage);
    }

    @Then("I am taken to the registration page")
    public void i_am_taken_to_the_registration_page() {
        String registrationUrl = RunPlaywrightTests.baseUrl + "/register";
        String currentUrl = RunPlaywrightTests.page.url();
        Assertions.assertEquals(registrationUrl, currentUrl);
    }

    @Then("I am taken back to the system’s home page")
    public void i_am_taken_to_the_system_s_home_page() {
        String homeUrl = RunPlaywrightTests.baseUrl + "/";
        String currentUrl = RunPlaywrightTests.page.url();
        Assertions.assertEquals(homeUrl, currentUrl);
    }
}
