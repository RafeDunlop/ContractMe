package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * This is a sample step definition class to check that cucumber works.
 * I am only including it because we have no cucumber tests in the project yet.
 * THIS SHOULD BE REMOVED AS SOON AS ANY REAL TESTS ARE ADDED.
 */
public class SampleStepDef {

    private boolean cucumber;

    @Given("I have a cucumber setup")
    public void i_have_a_cucumber_setup() {
        // Write code here that turns the phrase above into concrete actions
        cucumber = true;
    }
    @When("I run the tests")
    public void i_run_the_tests() {
        // Write code here that turns the phrase above into concrete actions
        // This is where you would run your tests
        // For this example, we will just print a message to the console
        System.out.println("Running tests...");
    }
    @Then("I should see the results in the console")
    public void i_should_see_the_results_in_the_console() {
        // Write code here that turns the phrase above into concrete actions
        assertTrue(cucumber, "Cucumber setup should be true");
    }
}
