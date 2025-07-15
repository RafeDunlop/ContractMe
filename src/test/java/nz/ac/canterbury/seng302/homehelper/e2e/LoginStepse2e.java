package nz.ac.canterbury.seng302.homehelper.e2e;


import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class LoginStepse2e {


    @Given("I connect to the system's main URL")
    public void i_connect_to_the_systems_main_url() {
        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/login");
    }

    @When("I see the homepage")
    public void i_see_the_homepage() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
    @Then("it indicates a button labelled {string}")
    public void it_indicates_a_button_labelled(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

}
