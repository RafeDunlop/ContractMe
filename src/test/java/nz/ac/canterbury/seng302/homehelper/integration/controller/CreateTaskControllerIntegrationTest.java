package nz.ac.canterbury.seng302.homehelper.integration.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

import nz.ac.canterbury.seng302.homehelper.entity.TaskState;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import org.hamcrest.Matchers;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import nz.ac.canterbury.seng302.homehelper.controller.CreateTaskController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
public class CreateTaskControllerIntegrationTest {

    private MockMvc mockMvc;


    @Autowired
    private CreateTaskController createTaskController;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RenovationRecordRepository renovationRecordRepository;

    @MockBean
    private RenovationTaskRepository renovationTaskRepository;

    @MockBean
    private RenovationRecordService renovationRecordService;


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(createTaskController).build();
        User user = new User("Jane", "Doe", "jane@doe.com", "Password");
        user.grantAuthority("ROLE_USER");
        user.activate();
        Mockito.when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        RenovationRecord renovationRecord = new RenovationRecord(user, "Renovation 1", "Description", List.of("Room 1", "Room 2"));
        Mockito.when(renovationRecordRepository.findById(1)).thenReturn(Optional.of(renovationRecord));
        Mockito.when(renovationRecordService.getRecordById(1L)).thenReturn(renovationRecord);
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testAddTask_validTask_TaskAddedAndRedirect() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/view/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Demolish walls")
                .param("description", "Demolish all the stuff")
                .param("roomList", "Room 1", "Room 2")
                .param("renovationId", "1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(view().name("redirect:/renovations/view?id=1"));
        Mockito.verify(renovationTaskRepository, Mockito.times(1)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testAddTask_fromCalendar_TaskAddedAndRedirect() throws Exception {
        String dateToReturnTo = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/view/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Demolish walls")
                        .param("description", "Demolish all the stuff")
                        .param("roomList", "Room 1", "Room 2")
                        .param("renovationId", "1")
                        .param("dateToReturnTo", dateToReturnTo)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(view().name(String.format("redirect:/renovations/view?id=1&dateEdited=%s#cellEdited", dateToReturnTo)));
        Mockito.verify(renovationTaskRepository, Mockito.times(1)).save(Mockito.any(RenovationTask.class));
    }


    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testAddTask_validTask_taskHasTheNotStartedState() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/view/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Demolish walls")
                        .param("description", "Demolish all the stuff")
                        .param("roomList", "Room 1", "Room 2")
                        .param("renovationId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(view().name("redirect:/renovations/view?id=1"));

        ArgumentCaptor<RenovationTask> taskCaptor = ArgumentCaptor.forClass(RenovationTask.class);
        Mockito.verify(renovationTaskRepository, Mockito.times(1)).save(taskCaptor.capture());

        RenovationTask savedTask = taskCaptor.getValue();
        assertEquals(TaskState.NOT_STARTED, savedTask.getState(), "The task state should be NOT_STARTED");
    }


    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testAddTask_invalidTaskName_TaskNotAddedStaysOnCreateTask() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/view/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "@#$%")
                .param("description", "Description")
                .param("roomList", "Room 1", "Room 2")
                        .param("renovationId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view/create?id=1"))
                .andExpect(flash().attribute("nameError", contains("Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes.")));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }


    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testAddTask_noTaskDescription_TaskNotAddedStaysOnCreateTask() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/view/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Task name")
                        .param("description", "")
                        .param("roomList", "Room 1", "Room 2")
                        .param("renovationId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view/create?id=1"))
                .andExpect(flash().attribute("descriptionError", contains("Task description cannot be empty.")));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testAddTask_taskDescriptionTooLong_TaskNotAddedStaysOnCreateTask() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/view/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Task name")
                        .param("description", """
                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa""")
                        .param("roomList", "Room 1", "Room 2")
                        .param("renovationId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view/create?id=1"))
                .andExpect(flash().attribute("descriptionError", contains("Task description must be 512 characters or less.")));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testAddTask_dueDateInPast_TaskNotAddedStaysOnCreateTask() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/view/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Testname")
                        .param("description", "Description")
                        .param("roomList", "Room 1", "Room 2")
                        .param("renovationId", "1")
                        .param("dueDate", LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view/create?id=1"))
                .andExpect(flash().attribute("dueDateError", contains("Due date must be in the future.")));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testAddTask_dueDateInvalidFormatISO_TaskNotAddedStaysOnCreateTask() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/view/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Testname")
                        .param("description", "Description")
                        .param("roomList", "Room 1", "Room 2")
                        .param("renovationId", "1")
                        .param("dueDate", (LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view/create?id=1"))
                .andExpect(flash().attribute("dueDateError", contains("Date is not in valid format, DD/MM/YYYY.")));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testAddTask_dueDateInvalidFormatRandomChar_TaskNotAddedStaysOnCreateTask() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/view/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Testname")
                        .param("description", "Description")
                        .param("roomList", "Room 1", "Room 2")
                        .param("renovationId", "1")
                        .param("dueDate",  "NotADate")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view/create?id=1"))
                .andExpect(flash().attribute("dueDateError", contains("Date is not in valid format, DD/MM/YYYY.")));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }



    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testAddTask_roomsInvalid_TaskNotAddedStaysOnCreateTask() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/view/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Test name")
                        .param("description", "Description")
                        .param("rooms", "Room 1", "Room 2", "otherRoom")
                        .param("renovationId", "1")
                        .param("DueDate", String.valueOf(LocalDate.now().plusDays(1)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view/create?id=1"))
                .andExpect(flash().attribute("roomError", contains("Whoops, it looks like \"otherRoom\" is not a valid room anymore")))
                .andExpect(flash().attribute("renovationId", 1L))
                .andExpect(flash().attribute("renovationTaskDTO", Matchers.hasProperty("name", Matchers.equalTo("Test name"))))
                .andExpect(flash().attribute("renovationTaskDTO", Matchers.hasProperty("description", Matchers.equalTo("Description"))));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void testViewCreatePage_userNotOwner_redirectToMain() throws Exception {
        User owner = new User("Owner", "User", "owner@doe.com", "Password");
        owner.grantAuthority("ROLE_USER");

        User notOwner = new User("Not", "Owner", "not.owner@doe.com", "Password");
        notOwner.grantAuthority("ROLE_USER");

        Mockito.when(userRepository.findByEmailIgnoreCase(notOwner.getEmail())).thenReturn(Optional.of(notOwner));

        RenovationRecord renovationRecord = new RenovationRecord(owner, "Test Renovation", "Test Desc", List.of("Room A"));
        Mockito.when(renovationRecordRepository.findById(2)).thenReturn(Optional.of(renovationRecord));
        Mockito.when(renovationRecordService.getRecordById(2L)).thenReturn(renovationRecord);

        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/view/create")
                        .param("id", "2"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testViewCreatePage_dateProvided_dateSet() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/view/create")
                .param("id", "1")
                .param("fromDate", "01-01-2020"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("fromDate", "01-01-2020"))
                .andExpect(model().attribute("renovationTaskDTO",
                        Matchers.hasProperty("dueDate", Matchers.equalTo("2020-01-01"))));
    }
}
