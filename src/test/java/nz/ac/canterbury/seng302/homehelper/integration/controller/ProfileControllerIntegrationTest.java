package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import java.util.List;
import nz.ac.canterbury.seng302.homehelper.controller.ProfileController;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class ProfileControllerIntegrationTest {
    @Autowired
    private ProfileController profileController;

    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ContractorRepository contractorRepository;


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


    @Test
    public void changeAvailability_setTrue_contractorIsAvailable() throws Exception {
        Contractor contractor = new Contractor("John", "Smith", "john@example.com", "password");
        Location location = new Location("20 Kirkwood Avenue", "New Zealand", "8041", "Christchuch", "Upper Riccarton");
        contractor.setLocation(location);
        contractorRepository.save(contractor);
        Long id = contractor.getId();

        mockMvc.perform(post("/editAvailability/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"isAvailable\": true}")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user"));

        Contractor updated = contractorRepository.findById(id).orElseThrow();
        assertTrue(updated.getAvailable(), "Publicity flag should be updated to true");
    }



    @Test
    public void changeAvailability_setFalse_contractorIsUnavailable() throws Exception {
        Contractor contractor = new Contractor("John", "Smith", "john@example.com", "password");
        Location location = new Location("20 Kirkwood Avenue", "New Zealand", "8041", "Christchuch", "Upper Riccarton");
        contractor.setLocation(location);
        contractorRepository.save(contractor);
        Long id = contractor.getId();

        mockMvc.perform(post("/editAvailability/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"isAvailable\": false}")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user"));

        Contractor updated = contractorRepository.findById(id).orElseThrow();
        assertFalse(updated.getAvailable(), "Publicity flag should be updated to false");
    }
}
