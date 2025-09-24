package nz.ac.canterbury.seng302.homehelper.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class SelectContractorsStepsE2e {

    private final UserContext userContext;

    private RenovationRecord renovationRecord;

    @Autowired
    private TeamsRepository teamsRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;



    public SelectContractorsStepsE2e(UserContext userContext) {
        this.userContext = userContext;
    }

    @Given("I own a renovation with an address and no team")
    public void i_own_a_renovation_with_an_address_and_no_team() {
        User user = userContext.getUser();

        renovationRecord = new RenovationRecord(user, "Record " + System.currentTimeMillis(), "", List.of());
        renovationRecord.setLocation(new Location("1 Test St", "NZ", "8011", "Christchurch", "CBD", -43.5309, 172.6365));
        renovationRecordRepository.save(renovationRecord);
        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/renovations/view?id=" + renovationRecord.getId());
        assertTrue(RunPlaywrightTests.page.locator("#create-team-button").isVisible());

    }

    @When("I create a team for that renovation")
    public void i_create_a_team_for_that_renovation() {
        RunPlaywrightTests.page.locator("#create-team-button").click();
        RunPlaywrightTests.page.locator("#skills-select").waitFor();
        RunPlaywrightTests.page.selectOption("#skills-select", "ELECTRICAL");
        RunPlaywrightTests.page.locator("#form-submit").click();
        RunPlaywrightTests.page.locator("#confirmButton:visible").click();

    }

    @Then("I am prompted whether I want to select contractors automatically or choose them myself from a map")
    public void i_am_prompted_whether_i_want_to_select_contractors_automatically_or_choose_them_myself_from_a_map() {
        assertEquals("Do you want ContractMe to automatically invite contractors to your team?",
                RunPlaywrightTests.page.locator("#promptTitle:visible").innerText().trim());
        assertEquals("Yes, send invitations automatically",
                RunPlaywrightTests.page.locator("#confirmButton:visible").innerText().trim());
        assertEquals("No, I will invite contractors manually",
                RunPlaywrightTests.page.locator("#cancelButton:visible").innerText().trim());
    }

    @Given("I have created a team")
    public void i_have_created_a_team() {
        User user = userContext.getUser();
        renovationRecord = new RenovationRecord(user, "Record " + System.currentTimeMillis(), "", List.of());
        renovationRecord.setLocation(new Location("1 Test St", "NZ", "8011", "Christchurch", "CBD", -43.5309, 172.6365));
        renovationRecordRepository.save(renovationRecord);
    }
    @When("I select to choose contractors manually")
    public void i_select_to_choose_contractors_manually() {
        Team team = new Team(renovationRecord);
        Role role = new Role(Skill.CARPENTRY);
        team.addRole(role);
        team.setAutomaticFilling(false);
        teamsRepository.save(team);
        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/renovations/team/view?id=" + team.getId());

    }

    @Then("next to each role I see an option to select a contractor for that role on a map")
    public void next_to_each_role_i_see_an_option_to_select_a_contractor_for_that_role_on_a_map() {
        var page = RunPlaywrightTests.page;
        Locator inviteButtons = page.locator(".skill-card button:has-text('Invite Contractor')");
        int count = inviteButtons.count();
        assertTrue(count >= 1, "Expected at least one Invite Contractor button");
    }

}
