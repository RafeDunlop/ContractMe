package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.EditTaskController;
import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationTaskService;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationTaskValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class EditTaskControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private EditTaskController editTaskController;

    @MockBean
    private RenovationTaskRepository renovationTaskRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RenovationTaskService renovationTaskService;

    @MockBean
    private RenovationRecordService renovationRecordService;

    private User user;

    @PostConstruct
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(editTaskController).build();
    }

    @BeforeEach
    public void setup_user() {
        User user = new User("Jane", "Doe", "jane@doe.com", "Password");
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));

        User notOwner = new User("Not", "Owner", "not.owner@doe.com", "Password");
        when(userRepository.findByEmailIgnoreCase("not.owner@doe.com")).thenReturn(Optional.of(notOwner));

        RenovationRecord renovationRecord = new RenovationRecord(user, "Renovation 1", "Description", List.of("Room 1", "Room 2"));
        when(renovationRecordService.getRecordById(1L)).thenReturn(renovationRecord);

        RenovationTask renovationTask = new RenovationTask("Task 1", "New Task", new ArrayList<>(), null, renovationRecord);
        when(renovationTaskService.getTaskById(1L)).thenReturn(renovationTask);

        RenovationTaskValidation renovationTaskValidation = new RenovationTaskValidation();
        ReflectionTestUtils.setField(renovationTaskService, "renovationTaskValidation", renovationTaskValidation);

        Mockito.doCallRealMethod().when(renovationTaskService).validateTaskDetails(Mockito.any(RenovationTaskDTO.class), Mockito.any(RenovationRecord.class));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void editTask_validTask_editTaskAndRedirect() throws Exception {

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
                .andExpect(flash().attribute("nameError", hasItem("Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes.")));
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
                .andExpect(flash().attribute("descriptionError", hasItem("Task description cannot be empty.")));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testEditTask_invalidRooms_TaskNotEditedStaysOnEditTask() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/editTask")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Task name")
                        .param("description", "Description")
                        .param("rooms", "Room 1", "Room 2", "otherRoom")
                        .param("taskId", "1")
                        .param("renovationId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/editTask?taskId=1&renovationId=1"))
                .andExpect(flash().attribute("roomError", hasItem("Whoops, it looks like \"otherRoom\" is not a valid room anymore")));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void testEditTaskGET_invalidUser_TaskNotEditedStaysOnEditTask() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/editTask")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("taskId", "1")
                    .param("renovationId", "1")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testEditTask_invalidTaskId_TaskNotEditedStaysOnEditTask()
            throws Exception {
        RenovationRecord renovationRecord = new RenovationRecord(user, "Renovation 2", "Description", List.of("Room 1", "Room 2"));
        when(renovationRecordService.getRecordById(2L)).thenReturn(renovationRecord);
        RenovationTask anotherTask = new RenovationTask("Task 2", "New Task", new ArrayList<>(), null, renovationRecord);
        when(renovationTaskService.getTaskById(2L)).thenReturn(anotherTask);
        mockMvc.perform(MockMvcRequestBuilders.get("/editTask")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("taskId", "2")
                        .param("renovationId", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
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
                .andExpect(flash().attribute("descriptionError", hasItem("Task description must be 512 characters or less.")));
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
                        .param("dueDate", String.valueOf(LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/editTask?taskId=1&renovationId=1"))
                .andExpect(flash().attribute("dueDateError", hasItem("Due date must be in the future.")));
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

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void testEditTask_userNotOwner_404() throws Exception {
        User owner = new User("Owner", "User", "owner@doe.com", "Password");
        owner.grantAuthority("ROLE_USER");

        RenovationRecord renovationRecord = new RenovationRecord(owner, "Test Renovation", "Test Desc", List.of("Room A"));
        when(renovationRecordService.getRecordById(1L)).thenReturn(renovationRecord);

        mockMvc.perform(MockMvcRequestBuilders.get("/editTask")
                        .param("taskId", "1")
                        .param("renovationId", "1"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }
}
