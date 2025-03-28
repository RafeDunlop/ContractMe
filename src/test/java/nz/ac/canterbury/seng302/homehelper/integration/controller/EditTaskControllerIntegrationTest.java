package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import nz.ac.canterbury.seng302.homehelper.controller.EditTaskController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@AutoConfigureMockMvc
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

    @BeforeEach
    public void setup_user() {
        mockMvc = MockMvcBuilders.standaloneSetup(editTaskController).build();
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void editTask_validTask_editTaskAndRedirect() throws Exception {
        // Mock user
        User user = new User("Jane", "Doe", "jane@doe.com", "Password");
        user.grantAuthority("ROLE_USER");
        Mockito.when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        // Mock renovation record & task
        RenovationRecord renovationRecord = new RenovationRecord(user, "Renovation 1", "Description", List.of("Room 1", "Room 2"));
        RenovationTask renovationTask = new RenovationTask("Task 1", "New Task", new ArrayList<>(), null, renovationRecord);

        Mockito.when(renovationRecordRepository.findById(1)).thenReturn(Optional.of(renovationRecord));
        Mockito.when(renovationTaskRepository.findById(1)).thenReturn(Optional.of(renovationTask));

        // Perform request
        mockMvc.perform(MockMvcRequestBuilders.post("/editTask")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Demolish walls")
                        .param("description", "Demolish all the stuff")
                        .param("roomList", "Room 1", "Room 2")
                        .param("taskId", "1")
                        .param("renovationId", "1"))
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

}
