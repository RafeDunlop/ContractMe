package nz.ac.canterbury.seng302.homehelper.e2e;

import com.microsoft.playwright.Locator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class DeleteTeamStepsE2e {

    private final UserContext userContext;
    private RenovationRecord renovationRecord;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private TeamsRepository teamsRepository;

    public DeleteTeamStepsE2e(UserContext userContext) {
        this.userContext = userContext;
    }

    @Given("I am viewing the renovation view page for a renovation I own with a team")
    public void i_am_viewing_the_renovation_view_page_for_a_renovation_i_own_with_a_team() {
        User user = userContext.getUser();

        renovationRecord = new RenovationRecord(user, "Record " + System.currentTimeMillis(), "", List.of());
        renovationRecord.setLocation(new Location("1 Test St", "NZ", "8011", "Christchurch", "CBD", -43.5309, 172.6365));
        renovationRecordRepository.save(renovationRecord);

        teamsRepository.save(new Team(renovationRecord));

        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/renovations/view?id=" + renovationRecord.getId());
    }

    @When("I click on the button called {string}")
    public void i_click_on_the_button_called(String buttonName) {
        String buttonId = "#" + buttonName.toLowerCase().replaceAll("[^a-z0-9]+", "-") + "-button";
        RunPlaywrightTests.page.locator(buttonId).click();
    }

    @Then("I see the delete team confirmation prompt")
    public void i_see_the_delete_team_confirmation_prompt() {
        Locator overlay = RunPlaywrightTests.page.locator("#overlay");
        assertThat(overlay).isVisible();
    }
    @Then("the prompt message asks me to confirm that I want to delete the team")
    public void the_prompt_message_asks_me_to_confirm_that_i_want_to_delete_the_team() {
        String confirmText = RunPlaywrightTests.page.locator("#promptText").innerText().toLowerCase();

        org.junit.jupiter.api.Assertions.assertTrue(
                confirmText.contains("delete") && confirmText.contains("team"),
                () -> "Prompt text did not contain deleting the team. Actual text: " + confirmText
        );
    }

    @Then("the prompt shows {string} and {string} actions")
    public void the_prompt_shows_and_actions(String action1, String action2) {
        Locator confirmButton = RunPlaywrightTests.page.locator("#confirmButton");
        Locator cancelButton  = RunPlaywrightTests.page.locator("#cancelButton");

        assertThat(confirmButton).isVisible();
        assertThat(cancelButton).isVisible();

        String confirmText = confirmButton.innerText().trim().toLowerCase();
        String cancelText  = cancelButton.innerText().trim().toLowerCase();

        assertEquals(action1.toLowerCase(), confirmText.toLowerCase());
        assertEquals(action2.toLowerCase(), cancelText.toLowerCase());
    }

    @When("I choose {string} in the prompt")
    public void i_choose_in_the_prompt(String action) {
        if (action.equalsIgnoreCase("cancel")) {
            RunPlaywrightTests.page.click("#cancelButton");
        } else {
            RunPlaywrightTests.page.click("#confirmButton");
        }
    }

    @Then("I still see the {string} button for that renovation")
    public void i_still_see_the_button_for_that_renovation(String buttonName) {
        String buttonId = "#" + buttonName.toLowerCase().replaceAll("[^a-z0-9]+", "-") + "-button";
        assertThat(RunPlaywrightTests.page.locator(buttonId)).isVisible();
    }

    @Then("I remain on the renovation view page")
    public void i_remain_on_the_renovation_view_page() {
        assertEquals(RunPlaywrightTests.baseUrl + "/renovations/view?id=" + renovationRecord.getId(), RunPlaywrightTests.page.url());
    }

    @Then("The team associated with the renovation I am viewing is deleted")
    public void theTeamAssociatedWithTheRenovationIAmViewingIsDeleted() {
        RunPlaywrightTests.page.waitForSelector("#create-team-button");
        assertNull(teamsRepository.findByRenovationRecord(renovationRecord));
    }
}
