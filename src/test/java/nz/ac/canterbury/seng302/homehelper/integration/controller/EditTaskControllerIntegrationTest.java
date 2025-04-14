package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import nz.ac.canterbury.seng302.homehelper.controller.EditTaskController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
@ActiveProfiles("test")
@SpringBootTest
public class EditTaskControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private EditTaskController editTaskController;

    @MockBean
    private RenovationTaskRepository renovationTaskRepository;

    @MockBean
    private RenovationRecordRepository renovationRecordRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RenovationTaskService renovationTaskService;

    @MockBean
    private RenovationRecordService renovationRecordService;



    @BeforeEach
    public void setup_user() {
        mockMvc = MockMvcBuilders.standaloneSetup(editTaskController).build();
        User user = new User("Jane", "Doe", "jane@doe.com", "Password");
        user.grantAuthority("ROLE_USER");
        Mockito.when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        // Mock renovation record & task
        RenovationRecord renovationRecord = new RenovationRecord(user, "Renovation 1", "Description", List.of("Room 1", "Room 2"));
        Mockito.when(renovationRecordService.getRecordById(1L)).thenReturn(renovationRecord);
        RenovationTask renovationTask = new RenovationTask("Task 1", "New Task", new ArrayList<>(), null, renovationRecord);
        Mockito.when(renovationTaskService.getTaskById(1L)).thenReturn(renovationTask);
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void editTask_validTask_editTaskAndRedirect() throws Exception {

        // Perform request
        mockMvc.perform(MockMvcRequestBuilders.post("/editTask")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Demolish walls")
                        .param("description", "Demolish all the stuff")
                        .param("rooms", "Room 1", "Room 2")
                        .param("taskId", "1")
                        .param("renovationId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(view().name("redirect:/renovations/view?id=1"));

        // Capture the saved task to verify its fields
        ArgumentCaptor<RenovationTask> taskCaptor = ArgumentCaptor.forClass(RenovationTask.class);
        Mockito.verify(renovationTaskRepository, Mockito.times(1)).save(taskCaptor.capture());

        RenovationTask savedTask = taskCaptor.getValue();
        assertEquals("Demolish walls", savedTask.getName());
        assertEquals("Demolish all the stuff", savedTask.getDescription());
        assertEquals(List.of("Room 1", "Room 2"), savedTask.getRoomList());
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testEditTask_invalidTaskName_TaskNotEditStaysOnCreateEditTask() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/editTask")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "@#$%")
                        .param("description", "Description")
                        .param("rooms", "Room 1", "Room 2")
                        .param("taskId", "1")
                        .param("renovationId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/editTask?taskId=1&renovationId=1"))
                .andExpect(flash().attribute("errorMessages", hasItem("Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes.")));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testEditTask_noTaskDescription_TaskNotEditedStaysOnEditTask() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/editTask")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Task name")
                        .param("description", "")
                        .param("roomList", "Room 1", "Room 2")
                        .param("taskId", "1")
                        .param("renovationId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/editTask?taskId=1&renovationId=1"))
                .andExpect(flash().attribute("errorMessages", hasItem("Task description cannot be empty.")));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testEditTask_taskDescriptionTooLong_TaskNotEditedStaysOnEditTask() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/editTask")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Task name")
                        .param("description", """
                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa""")
                        .param("roomList", "Room 1", "Room 2")
                        .param("taskId", "1")
                        .param("renovationId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/editTask?taskId=1&renovationId=1"))
                .andExpect(flash().attribute("errorMessages", hasItem("Task description must be 512 characters or less.")));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testEditTask_dueDateInPast_TaskNotEditedStaysOnEditTask() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/editTask")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "@#$%")
                        .param("description", "Description")
                        .param("roomList", "Room 1", "Room 2")
                        .param("taskId", "1")
                        .param("renovationId", "1")
                        .param("DueDate", String.valueOf(LocalDate.now().minusDays(1)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/editTask?taskId=1&renovationId=1"))
                .andExpect(flash().attribute("errorMessages", hasItem("Due date must be in the future.")));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    @WithMockUser(username = "jane@doe.com", roles = {"USER"})
    public void testEditTask_editTaskIcon_taskIconChangedReturnsToRenovations() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/editTask/edit-icon/1")
                    .contentType(MediaType.APPLICATION_JSON)
                        .param("id", "1")
                    .content("{\"iconName\":\"task-icon.png\"}")
                    .accept(MediaType.APPLICATION_JSON))

                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }
}
