package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.controller.ChangePasswordController;
import nz.ac.canterbury.seng302.homehelper.controller.RegisterController;
import nz.ac.canterbury.seng302.homehelper.dto.UpdatePasswordDTO;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.UpdatePasswordService;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@SpringBootTest
@AutoConfigureMockMvc
public class ChangePasswordControllerIntegrationTest {

    @Autowired
    private ChangePasswordController changePasswordController;

    /**
     * MockMvc instance used for simulating HTTP requests.
     */
    private MockMvc mockMvc;


    @MockBean
    private LoginService loginService;

    /**
     * Initializes the {@link MockMvc} instance with a new setup of the
     * {@link ChangePasswordController}.
     */
    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(changePasswordController).build();
    }

    /**
     * Tests the updating password of a valid user.
     * This test simulates a user submitting a valid updatePassword form and expects:
     * A redirection  upon successful change.
     * A redirection to the user profile page.
     *
     * @throws Exception if the request processing fails.
     */
    @WithMockUser(username = "jane@doe.com")
    @Test
    public void testPostChangePassword_validPassword_success() throws Exception {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        User expectedUser = new User("Jane", "Doe", "jane@doe.nz",
                passwordEncoder.encode("Test123!"));
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO("Test123!", "FinalTesting23!#",
                "FinalTesting23!#");
        Mockito.when(loginService.getUserByEmail()).thenReturn(expectedUser);
        mockMvc.perform(post("/user/edit/updatePassword")
                        .param("currentPassword", updatePasswordDTO.getCurrentPassword())
                        .param("newPassword", updatePasswordDTO.getNewPassword())
                        .param("retypePassword", updatePasswordDTO.getRetypePassword()))
                .andExpect(status().isFound())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "/user"))
                .andExpect(flash().attribute("successMessage", "Password updated successfully."));

    }
    /**
     * Tests the updating password of a valid user.
     * This test simulates a user submitting a invalid updatePassword form and expects:
     * A redirection  upon successful change.
     * A redirection to the user profile page.
     *
     * @throws Exception if the request processing fails.
     */
    @WithMockUser(username = "jane@doe.com")
    @Test
    public void testPostChangePassword_invalidCurrentPassword_error() throws Exception {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        List<String> expectedErrors = List.of("Old Password does not match.");
        User expectedUser = new User("Jane", "Doe", "jane@doe.nz",
                passwordEncoder.encode("RightPassword!"));
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO("WrongPassword",
                "FinalTesting23!#", "FinalTesting23!#");

        Mockito.when(loginService.getUserByEmail())
                .thenReturn(expectedUser);

        mockMvc.perform(post("/user/edit/updatePassword")
                        .param("currentPassword", updatePasswordDTO.getCurrentPassword())
                        .param("newPassword", updatePasswordDTO.getNewPassword())
                        .param("retypePassword", updatePasswordDTO.getRetypePassword()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errorMessages", expectedErrors));
    }

}