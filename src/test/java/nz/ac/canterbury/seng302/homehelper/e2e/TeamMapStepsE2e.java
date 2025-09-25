package nz.ac.canterbury.seng302.homehelper.e2e;

import com.microsoft.playwright.Page;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class TeamMapStepsE2e {

    private final UserContext userContext;

    private RenovationRecord renovationRecord;

    private Contractor contractor;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private TeamsRepository teamRepository;

    public TeamMapStepsE2e(UserContext userContext) {
        this.userContext = userContext;
    }

    @Given("I am viewing a renovation I own")
    public void i_am_viewing_a_renovation_i_own() {
        renovationRecord = new RenovationRecord(userContext.getUser(), "Team Record", "", List.of());
        renovationRecord = renovationRecordRepository.save(renovationRecord);

        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/renovations/view?id=" + renovationRecord.getId());
    }

    @Given("I am viewing a renovation I own with a team which has one contractor assigned")
    public void i_am_viewing_a_renovation_i_own_with_a_team_which_has_at_least_one_contractor_assigned() {
        renovationRecord = new RenovationRecord(userContext.getUser(), "Team Record", "", List.of());
        renovationRecord.setLocation(new Location("1 Test St", "NZ", "8011", "Christchurch", "CBD", -43.53333, 172.63333));
        renovationRecord = renovationRecordRepository.save(renovationRecord);

        contractor = new Contractor("Jane", "Doe", "jane@doe.com", "password");
        contractor.setSkills(Set.of(Skill.ASBESTOS_REMOVAL));
        contractor.setLocation(new Location("2 Test St", "NZ", "8011", "Christchurch", "CBD", -43.53444, 172.63444));
        contractor = contractorRepository.save(contractor);

        Role role = new Role(Skill.ASBESTOS_REMOVAL);
        role.setContractor(contractor);
        Team team = new Team(renovationRecord);
        team.setAutomaticFilling(false);
        team.addRole(role);
        team = teamRepository.save(team);

        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/renovations/team/view?id=" + team.getId());
        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/renovations/view?id=" + renovationRecord.getId());
    }

    @When("The renovation has an address listed")
    public void the_renovation_has_an_address_listed() {
        renovationRecord.setLocation(new Location("1 Test St", "NZ", "8011", "Christchurch", "CBD", -43.53333, 172.63333));
        renovationRecordRepository.save(renovationRecord);

        RunPlaywrightTests.page.reload();
    }

    @When("I view the renovation map")
    public void i_view_the_renovation_map() {
        RunPlaywrightTests.page.waitForSelector("#view-location-tab-item");
        RunPlaywrightTests.page.click("#view-location-tab-item");
    }

    @Then("There is a tab where I can see a map with a house icon at the renovation's address")
    public void there_is_a_tab_where_i_can_see_a_map_with_a_house_icon_at_the_renovations_address() {
        RunPlaywrightTests.page.waitForSelector("#view-location-tab-item");
        RunPlaywrightTests.page.click("#view-location-tab-item");

        RunPlaywrightTests.page.waitForSelector("img[src*='user-renovation.png']",
                new Page.WaitForSelectorOptions().setTimeout(5000));

        boolean markerExists = RunPlaywrightTests.page.locator("img[src*='user-renovation.png']").isVisible();
        Assertions.assertTrue(markerExists);
    }

    @Then("The contractor is shown on the map")
    public void the_contractors_are_shown_on_the_map() {
        String contractorIcon = contractor.getProfilePicture();

        RunPlaywrightTests.page.waitForSelector("img.contractor-img[src*='" + contractorIcon + "']",
                new Page.WaitForSelectorOptions().setTimeout(5000));

        boolean markerExists = RunPlaywrightTests.page.locator("img.contractor-img[src*='" + contractorIcon + "']").isVisible();
        Assertions.assertTrue(markerExists);
    }
}