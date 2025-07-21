package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.ProfileController;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class ProfileControllerIntegrationTest {
    @Autowired
    private ProfileController profileController;

    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @PostConstruct
    public void setup() {

        mockMvc = MockMvcBuilders.standaloneSetup(profileController).build();
        SecurityContext securityContextMock = Mockito.mock(SecurityContext.class);
        Authentication authenticationMock = Mockito.mock(Authentication.class);

        Mockito.when(authenticationMock.getName()).thenReturn("john.smith@example.com");
        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);

    }

    @Test
    public void testGetProfile_validId_dataAdded() throws Exception {
        User expectedUser = new User("John", "Smith", "john@example.com", "password");
        Mockito.when(userRepository.findByEmailIgnoreCase(Mockito.anyString())).thenReturn(Optional.of(expectedUser));

        mockMvc.perform(get("/user/"))
                .andExpect(status().isOk())
                .andExpect(view().name("profileTemplate"))
                .andExpect(model().attribute("firstName", expectedUser.getFirstName()))
                .andExpect(model().attribute("lastName", expectedUser.getLastName()))
                .andExpect(model().attribute("email", expectedUser.getEmail()))
                .andExpect(model().attribute("dateAdded", expectedUser.getCreatedTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .andExpect(model().attribute("profilePicture", expectedUser.getProfilePicture()));
    }

    @Test
    public void testGetProfile_invalidId_errorResponse() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetProfile_validIdNoUser_errorResponse() throws Exception {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/user"))
                .andExpect(status().isNotFound());
    }
}
