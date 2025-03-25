package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.jupiter.api.Test;
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
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "jane@doe.com")
public class CreateTaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RenovationTaskRepository renovationTaskRepository;
    @MockBean
    private UserRepository userRepository;

    @Test
    public void testAddTask_validTask_TaskAddedAndRedirect() throws Exception {
        User user = new User("Jane", "Doe", "jane@doe.com", "password");
        Mockito.when(userRepository.findByEmailIgnoreCase(Mockito.anyString()))
            .thenReturn(Optional.of(user));
        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/view/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Demolish walls")
                .param("description", "Demolish all the stuff")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(view().name("redirect:/renovations/view"));
    }
}
