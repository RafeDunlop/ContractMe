package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.hamcrest.Matchers.*;

import nz.ac.canterbury.seng302.homehelper.controller.CreateTaskController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;

@SpringBootTest
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

    @BeforeEach
    public void setup_user() {
       mockMvc = MockMvcBuilders.standaloneSetup(createTaskController).build();
    }

    @Test
    @WithMockUser(username = "jane@doe.com", roles = {"USER"})
    public void testAddTask_validTask_TaskAddedAndRedirect() throws Exception {
        User user = new User("Jane", "Doe", "jane@doe.com", "Password");
        user.grantAuthority("ROLE_USER");
        Mockito.when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        RenovationRecord renovationRecord = new RenovationRecord(user, "Renovation 1", "Description", List.of("Room 1", "Room 2"));
        Mockito.when(renovationRecordRepository.findById(1)).thenReturn(Optional.of(renovationRecord));
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/view/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Demolish walls")
                .param("description", "Demolish all the stuff")
                .param("roomList", "Room 1", "Room 2")
                .param("renovationId", "1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(view().name("redirect:/renovations/view"));
        Mockito.verify(renovationTaskRepository, Mockito.times(1)).save(Mockito.any(RenovationTask.class));
    }


    @Test
    @WithMockUser(username = "jane@doe.com", roles = {"USER"})
    public void testAddTask_invalidTaskName_TaskNotAddedStaysOnCreateTask() throws Exception {
        User user = new User("Jane", "Doe", "jane@doe.com", "Password");
        user.grantAuthority("ROLE_USER");
        Mockito.when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        RenovationRecord renovationRecord = new RenovationRecord(user, "Renovation 1", "Description", List.of("Room 1", "Room 2"));
        Mockito.when(renovationRecordRepository.findById(1)).thenReturn(Optional.of(renovationRecord));
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/view/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "@#$%")
                .param("description", "Description")
                .param("roomList", "Room 1", "Room 2")
                        .param("renovationId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("forward:/renovations/view/create"))
                .andExpect(model().attribute("errorMessages", hasItem("Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes.")));
        Mockito.verify(renovationTaskRepository, Mockito.times(0)).save(Mockito.any(RenovationTask.class));
    }

}
