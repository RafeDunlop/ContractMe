package nz.ac.canterbury.seng302.homehelper.e2e;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class TaskCalendarInteractionsStepsE2e {
    private UserContext userContext;
    private RenovationTask renovationTask;
    private RenovationRecord renovationRecord;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private RenovationTaskRepository renovationTaskRepository;

    public TaskCalendarInteractionsStepsE2e(UserContext userContext) {
        this.userContext = userContext;
    }

    @Given("I have a renovation with a task due tomorrow")
    public void i_have_have_a_renovation_with_a_task_due_tomorrow() {
        User user = userContext.getUser();
        renovationRecord = new RenovationRecord(user, "Test Renovation", "desc", new ArrayList<>());
        renovationRecordRepository.save(renovationRecord);

        renovationTask = new RenovationTask("Task", "desc", new ArrayList<>(), LocalDate.now().plusDays(1), renovationRecord);
        renovationTaskRepository.save(renovationTask);
        renovationRecord.setRenovationTasks(List.of(renovationTask));
    }

    @Given("I navigate to the renovation")
    public void i_navigate_to_the_renovation() {
        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/renovations/view?id=" + renovationRecord.getId());
    }

    @When("I double click on the task in the calendar")
    public void i_double_click_on_the_task_in_the_calendar() {
        RunPlaywrightTests.page.locator("[data-task-id='" + renovationTask.getId() + "']").dblclick();
    }

    @Then("I see the edit task page")
    public void i_see_the_edit_task_page() {
        String profileUrl = RunPlaywrightTests.baseUrl + "/editTask?taskId=" + renovationTask.getId() + "&renovationId=" + renovationRecord.getId();
        String currentUrl = RunPlaywrightTests.page.url();
        Assertions.assertEquals(profileUrl, currentUrl);
    }
}
