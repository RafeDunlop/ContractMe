package nz.ac.canterbury.seng302.homehelper.e2e;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class TaskCalendarInteractionsStepsE2e {
    private final UserContext userContext;
    private RenovationTask renovationTask;
    private RenovationRecord renovationRecord;
    private final LocalDate today = LocalDate.now();

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private RenovationTaskRepository renovationTaskRepository;

    public TaskCalendarInteractionsStepsE2e(UserContext userContext) {
        this.userContext = userContext;
    }


    @Before
    public void beforeEach() {
        User user = userContext.getUser();
        renovationRecord = new RenovationRecord(user, "Test Renovation", "desc", new ArrayList<>());
        renovationRecordRepository.save(renovationRecord);
    }

    @Given("I have a renovation with a task due tomorrow")
    public void i_have_have_a_renovation_with_a_task_due_tomorrow() {


        renovationTask = new RenovationTask("Task", "desc", new ArrayList<>(), today.plusDays(1), renovationRecord);
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


    @When("I double click an empty space on a day")
    public void i_double_click_an_empty_space_on_a_day() {
        RunPlaywrightTests.page.locator("[data-cell-date='" + today.plusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "']").dblclick();
    }

    @Then("I see the edit task page")
    public void i_see_the_edit_task_page() {
        String expectedUrl = String.format("%s/editTask?taskId=%d&renovationId=%d&fromDate=%s",
                RunPlaywrightTests.baseUrl,
                renovationTask.getId(),
                renovationRecord.getId(),
                renovationTask.getDueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        );
        String actualUrl = RunPlaywrightTests.page.url();
        assertEquals(expectedUrl, actualUrl);
    }


    @Then("I see the add task form")
    public void i_see_the_add_task_form() {
        String expectedUrl = String.format("%s/renovations/view/create?id=%d&fromDate=%s",
                RunPlaywrightTests.baseUrl,
                renovationRecord.getId(),
                today.plusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        );
        String actualUrl = RunPlaywrightTests.page.url();
        assertEquals(expectedUrl, actualUrl);
    }

    @And("the due date is set to the date I clicked")
    public void the_due_date_is_set_to_the_date_i_clicked() {
        String dueDate = RunPlaywrightTests.page.locator("#dueDate").inputValue();
        assertEquals(today.plusDays(1).toString(), dueDate);
    }

    @Given("I am viewing the calendar for this month with a task due on day {int}")
    public void iAmViewingTheCalendarForThisMonth(int dayOfMonth) {
        renovationTask = new RenovationTask("Task", "desc", List.of(), today.withDayOfMonth(dayOfMonth), renovationRecord);
        renovationTask = renovationTaskRepository.save(renovationTask);
        renovationRecord.setRenovationTasks(List.of(renovationTask));
        renovationRecordRepository.save(renovationRecord);
        i_navigate_to_the_renovation();
    }

    @And("I double click on day {int} to go to the {string} form")
    public void iClickOnDayDayOfMonthToGoToTheTaskFormForm(int dayOfMonth, String formName) {
        LocalDate dayToClick = today.withDayOfMonth(dayOfMonth);
        switch (formName) {
            case "Create Task" -> RunPlaywrightTests.page.locator(
                        String.format("#cell-%s", dayToClick.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                ).dblclick();

            case "Edit Task" -> RunPlaywrightTests.page.locator(
                    String.format("[data-task-id='%s']", renovationTask.getId())
                ).dblclick();

            default -> throw new IllegalStateException(String.format("Unexpected value: %s", formName));
        }
    }

    @And("I enter valid details to the {string} form")
    public void iEnterValidDetailsToTheTaskFormForm(String formName) {
        switch (formName) {
            case "Create Task" -> {
                RunPlaywrightTests.page.locator("#name").fill("playwright test task");
                RunPlaywrightTests.page.locator("#description").fill("playwright test task description");
                RunPlaywrightTests.page.locator("#dueDate").fill(today.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }

            case "Edit Task" -> {
                RunPlaywrightTests.page.locator("#name").fill("adjusted playwright test task");
                RunPlaywrightTests.page.locator("#dueDate").fill(today.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }

            default -> throw new IllegalStateException(String.format("Unexpected value: %s", formName));

        }
    }

    @When("I click the form button labelled {string}")
    public void iClickTheButtonButton(String buttonName) {
        switch (buttonName) {
            case "Submit" -> RunPlaywrightTests.page.locator("button[type=submit]").click();

            case "Cancel" -> RunPlaywrightTests.page.locator(".cancel-task-form").click();
        }
    }

    @Then("I am returned to the calendar view with the {int} of the edited task highlighted yellow if it is not the current day")
    public void iAmReturnedToTheCalendarViewWithTheDayOfMonthOfTheEditedTaskHighlightedYellowIfItIsNotTheCurrentDay(int dayOfMonth) {
        String actualBackgroundColour = RunPlaywrightTests.page.locator("#cellEdited")
                .evaluate("element => window.getComputedStyle(element).getPropertyValue('background-color')").toString();
        String expectedBackgroundColour = (today.getDayOfMonth() == dayOfMonth) ? "rgb(135, 188, 250)" : "rgb(250, 250, 145)"; // blue : yellow
        assertEquals(expectedBackgroundColour, actualBackgroundColour);
    }

    @Then("I can see the {string} field prefilled with day {int}")
    public void iCanSeeTheFieldPrefilledWithDay(String fieldName, int dayOfMonth) {
        String expectedValue = today.withDayOfMonth(dayOfMonth).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String actualValue = RunPlaywrightTests.page.locator(String.format("#%s", fieldName)).inputValue();
        assertEquals(expectedValue, actualValue);
    }
}
