package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.TaskState;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.hamcrest.CustomMatcher;
import org.hamcrest.core.StringContains;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLOptionElement;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class TaskStateSteps {

    private final UserContext userContext;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private RenovationTaskRepository renovationTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    private Long renovationId;

    private Long taskId;

    private ResultActions resultActions;
    private MvcResult result;


    public TaskStateSteps (UserContext userContext){
        this.userContext = userContext;
    }

    @Given("that i create a task on a renovation record")
    public void that_i_create_a_task_on_a_renovation_record() {

        RenovationRecord record = new RenovationRecord(userContext.getUser(), "Record " + System.currentTimeMillis(), "", List.of());
        renovationRecordRepository.save(record);
        this.renovationId = record.getId();

        RenovationTask task = new RenovationTask("Task 1", "Desc",new ArrayList<>(), null,record);
        TaskState state = TaskState.NOT_STARTED;
        task.setState(state);
        renovationTaskRepository.save(task);
        this.taskId = task.getId();

        Optional<RenovationTask> retrieved = renovationTaskRepository.findById(task.getId());
        assertTrue(retrieved.isPresent(), "Task should be saved and retrievable");
        assertEquals("Task 1", retrieved.get().getName());
    }

    @When("I view the task")
    public void i_view_the_task() throws Exception {
        mockMvc.perform(get("/renovations/retrieve/" + renovationId)
                        .param("page", "1")
                        .param("cardsPerPage", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Task 1"));
    }

    @Then("then the task is set to \"Not Started\"")
    public void then_the_task_is_set_to_not_started() {
        Optional<RenovationTask> taskOptional = renovationTaskRepository.findById(taskId);
        assertTrue(taskOptional.isPresent(), "Task should exist");
        RenovationTask task = taskOptional.get();
        assertEquals(TaskState.NOT_STARTED, task.getState(), "Task state should be NOT_STARTED");

    }

    @And("I can see the task state")
    public void i_can_see_the_task_state() throws Exception {
        mockMvc.perform(get("/renovations/retrieve/" + renovationId)
                        .param("page", "1")
                        .param("cardsPerPage", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].state").value("NOT_STARTED"));
    }


    @Given("that I am viewing one of my renovation records with tasks")
    public void that_i_am_viewing_one_of_my_renovation_records_with_tasks() throws Exception {
        RenovationRecord record = new RenovationRecord(userContext.getUser(), "Record " + System.currentTimeMillis(), "", List.of());
        renovationRecordRepository.save(record);
        this.renovationId = record.getId();

        RenovationTask task = new RenovationTask("Task 1", "Desc", new ArrayList<>(), null, record);
        TaskState state = TaskState.NOT_STARTED;
        task.setState(state);
        renovationTaskRepository.save(task);
        this.taskId = task.getId();

        result = mockMvc.perform(get("/renovations/view")
                        .param("id", String.valueOf(renovationId))
                        .sessionAttr("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @When("I update the task state {string}")
    public void i_update_the_task_state(String stateName) throws Exception {
        mockMvc.perform(patch("/task/" + taskId + "/state")
                        .param("state", stateName)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("the task state is set to state {string}")
    public void the_task_state_is_set_to_state(String expectedState) {
        Optional<RenovationTask> task = renovationTaskRepository.findById(taskId);
        assertTrue(task.isPresent(), "Task should exist");

        RenovationTask renovationTask = task.get();
        assertEquals(TaskState.valueOf(expectedState), renovationTask.getState(), "Task state should match expected state");
    }

    @Given("the task has the state {string} and is due today")
    public void the_task_has_the_state_and_is_due_today(String stateName) {
        RenovationTask renovationTask = renovationTaskRepository.findById(taskId).orElseThrow();

        TaskState state = TaskState.valueOf(stateName);
        renovationTask.setState(state);
        renovationTask.setDueDate(LocalDate.now());

        renovationTaskRepository.save(renovationTask);
    }

    @When("I view the task in the calendar")
    public void i_view_the_task_in_the_calendar() throws Exception {
        result = mockMvc.perform(get("/renovations/view")
                        .param("id", String.valueOf(renovationId))
                        .sessionAttr("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("the task should be highlighted with the colour {string} corresponding to the it's state")
    public void the_task_should_be_highlighted_with_the_colour_corresponding_to_the_it_s_state(String expectedHexColour) throws Exception {
        String html = result.getResponse().getContentAsString().toLowerCase();

        assertTrue(html.contains("background-color: " + expectedHexColour),"Expected calendar task with background-color: " + expectedHexColour);
    }

    @When("I view the tasks section")
    public void i_view_the_tasks_section() throws Exception {
        resultActions = mockMvc.perform(get("/renovations/view")
                        .param("id", String.valueOf(renovationId))
                        .sessionAttr("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext()))
                .andExpect(status().isOk())
                .andExpect(content().string(StringContains.containsString("task-grid")));
        result = resultActions.andReturn();
    }

    @Then("I can select option {string} to filter tasks by task state")
    public void i_can_select_option_to_filter_tasks_by_task_state(String state) throws UnsupportedEncodingException {
        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains(state));
    }

    @When("I select the option {string} to filter tasks by state")
    public void i_select_the_option_to_filter_tasks_by_state(String stateOption) throws Exception {
        resultActions = mockMvc.perform(get("/renovations/retrieve/" + renovationId)
                        .queryParam("status", stateOption))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result = resultActions.andReturn();
    }

    @Given("I have tasks")
    public void i_have_tasks(List<Map<String, String>> tasksTable) {
        RenovationRecord renovationRecord = renovationRecordRepository.findById(renovationId).orElseThrow();
        List<RenovationTask> taskList = new ArrayList<>();
        for (Map<String, String> taskMap : tasksTable) {
            RenovationTask task = new RenovationTask(taskMap.get("name"), "Desc", List.of(), null, renovationRecord);
            task.setState(TaskState.valueOf(taskMap.get("state")));
            taskList.add(task);
        }
        renovationTaskRepository.saveAll(taskList);
        renovationRecord.setRenovationTasks(taskList);
        renovationRecordRepository.save(renovationRecord);
    }

    @Then("The page is reloaded with only the {int} tasks shown")
    public void the_page_is_reloaded_with_only_the_tasks_shown(Integer expectedTasks) throws Exception {
       resultActions.andExpect(jsonPath("$.totalElements").value(expectedTasks));
    }
}
