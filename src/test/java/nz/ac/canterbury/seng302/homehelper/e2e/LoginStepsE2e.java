package nz.ac.canterbury.seng302.homehelper.e2e;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.e2e.context.E2eUserContext;
import org.junit.jupiter.api.Assertions;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public class LoginStepsE2e {

    PasswordEncoder passwordEncoder;
    E2eUserContext userContext;

    public LoginStepsE2e(E2eUserContext userContext) {
        this.userContext = userContext;
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


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

    @And("I enter an email address and its corresponding password for an account that exists in the system")
    public void i_enter_an_email_address_and_its_corresponding_password_for_an_account_that_exists_in_the_system() {
        String userEmail = userContext.getUser().getEmail();
        String password = "Test123!";

        RunPlaywrightTests.page.locator("#username").fill(userEmail);
        RunPlaywrightTests.page.locator("#password").fill(password);
    }


    @And("I enter the wrong password for the corresponding email address")
    public void i_enter_the_wrong_password_for_the_corresponding_email_address() {
        String userEmail = userContext.getUser().getEmail();
        String incorrectPassword = "Test123!2354y";

        RunPlaywrightTests.page.locator("#username").fill(userEmail);
        RunPlaywrightTests.page.locator("#password").fill(incorrectPassword);

    }

    @And("I enter an empty password for the corresponding email address")
    public void i_enter_an_empty_password_for_the_corresponding_email_address() {
        String userEmail = userContext.getUser().getEmail();
        String incorrectPassword = "";

        RunPlaywrightTests.page.locator("#username").fill(userEmail);
        RunPlaywrightTests.page.locator("#password").fill(incorrectPassword);

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

    @Then("It indicates a button labelled {string}")
    public void it_indicates_a_button_labelled(String expectedButtonName) {
        String buttonName = RunPlaywrightTests.page.locator("#login-button").innerText();
        Assertions.assertEquals(expectedButtonName, buttonName);
    }

    @Then("An error message tells me that {string}")
    public void an_error_message_tells_me_email_address_must_be_in_the_form(String expectedErrorMessage) {
        String errorMessage = RunPlaywrightTests.page.locator("#email-backend-error").locator("ul").innerText();
        Assertions.assertEquals(expectedErrorMessage, errorMessage);
    }

    @Then("An error message tells me {string}")
    public void an_error_message_tells_me_the_email_address_is_unknown_or_the_password_is_invalid(String expectedErrorMessage) {
        String errorMessage = RunPlaywrightTests.page.locator("#general-error").innerText();
        Assertions.assertEquals(expectedErrorMessage, errorMessage);
    }

    @Then("I am taken to the registration page")
    public void i_am_taken_to_the_registration_page() {
        String registrationUrl = RunPlaywrightTests.baseUrl + "/register";
        String currentUrl = RunPlaywrightTests.page.url();
        Assertions.assertEquals(registrationUrl, currentUrl);
    }

    @Then("I am taken back to the system's home page")
    public void i_am_taken_to_the_systems_home_page() {
        String homeUrl = RunPlaywrightTests.baseUrl + "/";
        String currentUrl = RunPlaywrightTests.page.url();
        Assertions.assertEquals(homeUrl, currentUrl);
    }

    @Then("I am taken to the main page of the application")
    public void i_am_taken_to_the_main_page_of_the_application() {
        String mainUrl = RunPlaywrightTests.baseUrl + "/main";
        String currentUrl = RunPlaywrightTests.page.url();
        Assertions.assertEquals(mainUrl, currentUrl);
    }
}
