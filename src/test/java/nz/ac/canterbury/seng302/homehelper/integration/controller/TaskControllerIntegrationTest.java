package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.TaskState;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "jane@doe.com")
@ActiveProfiles("test")
public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationTaskRepository renovationTaskRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    private User currentUser;
    private RenovationRecord renovationRecord;

    @BeforeEach
    public void setupUser() {
        currentUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(currentUser);

        renovationRecord = new RenovationRecord(currentUser, "Test Renovation", "Test Desc", List.of("Room A"));
        renovationRecordRepository.save(renovationRecord);
    }

    @ParameterizedTest
    @ValueSource(strings = {"NOT_STARTED", "IN_PROGRESS", "BLOCKED", "COMPLETED", "CANCELLED"})
    public void testPatchTaskState_updatesCorrectlyForEachState(String stateName) throws Exception {
        RenovationTask task = new RenovationTask("Param Task", "Desc", new ArrayList<>(), null, renovationRecord);
        task.setState(TaskState.NOT_STARTED);
        renovationTaskRepository.save(task);

        mockMvc.perform(patch("/task/" + task.getId() + "/state")
                        .param("state", stateName)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("State updated"));

        RenovationTask updatedTask = renovationTaskRepository.findById(task.getId()).orElseThrow();
        assertEquals(TaskState.valueOf(stateName), updatedTask.getState());
    }

    @ParameterizedTest
    @ValueSource(strings = {"not_started", "aaaaa", "22", "", "IN PROGRESS"})
    public void testPatchTaskState_invalidStates_isBadRequest(String invalidStateName) throws Exception {
        RenovationTask task = new RenovationTask("Param Task", "Desc", new ArrayList<>(), null, renovationRecord);
        task.setState(TaskState.NOT_STARTED);
        renovationTaskRepository.save(task);

        mockMvc.perform(patch("/task/" + task.getId() + "/state")
                        .param("state", invalidStateName)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        RenovationTask updatedTask = renovationTaskRepository.findById(task.getId()).orElseThrow();
        assertEquals(TaskState.NOT_STARTED, updatedTask.getState(), "State should remain the same");
    }
}
