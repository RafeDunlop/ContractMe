package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class RenovationControllerAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginService loginService;

    @MockBean
    private RenovationRecordService renovationRecordService;

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void testViewCreatePage_userNotOwner_redirectToMain() throws Exception {
        // Owner of the renovation
        User owner = new User("Owner", "User", "owner@doe.com", "Password");
        owner.grantAuthority("ROLE_USER");

        // Logged-in user who is not the owner
        User notOwner = new User("Not", "Owner", "not.owner@doe.com", "Password");
        notOwner.grantAuthority("ROLE_USER");

        // Mock the login service to return the logged-in user
        Mockito.when(loginService.getUserByEmail()).thenReturn(notOwner);

        // Create a renovation record belonging to a different user
        RenovationRecord renovationRecord = new RenovationRecord(owner, "Test Renovation", "Test Desc", List.of("Room A"));
        Mockito.when(renovationRecordService.getRecordById(1L)).thenReturn(renovationRecord);

        // Perform the request and assert redirection
        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/edit")
                        .param("id", "1"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/main"));
    }
}
