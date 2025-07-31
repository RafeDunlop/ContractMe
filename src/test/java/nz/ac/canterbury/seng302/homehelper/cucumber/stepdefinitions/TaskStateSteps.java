package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.spring.CucumberContextConfiguration;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.TaskState;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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

        RenovationTask task = new RenovationTask("Task 1", "Desc",new ArrayList<>(), null,record);
        TaskState state = TaskState.NOT_STARTED;
        task.setState(state);
        renovationTaskRepository.save(task);
        this.taskId = task.getId();

        mockMvc.perform(get("/view")
                        .param("id", String.valueOf(renovationId))
                        .sessionAttr("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"));
    }

    @When("I select {string} from the task state dropdown")
    public void i_select_from_the_task_state_dropdown(String string) {

    }

    @Then("the task state is updated to {string}")
    public void the_task_state_is_updated_to(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("the task state displays the color {string}")
    public void the_task_state_displays_the_color(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
}
