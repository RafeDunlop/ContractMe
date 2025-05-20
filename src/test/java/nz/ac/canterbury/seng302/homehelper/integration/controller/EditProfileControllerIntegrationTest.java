package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.controller.EditProfileController;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "jane@doe.com")
@ActiveProfiles("test")
public class EditProfileControllerIntegrationTest {

    @Autowired
    private EditProfileController editProfileController;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginService loginService;

    @PostConstruct
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(editProfileController).build();
    }

    /**
     * Tests the edit profile page with a valid id in the URL path.
     * Test simulates a user clicking the edit button from the profile page to edit their details.
     * Expects to get the edit profile page with their current details added to it.
     * @throws Exception if the request processing fails
     */
    @Test
    @WithMockUser(username = "jane@doe.com")
    public void getForm_validUserId_returnForm() throws Exception {
        User expectedUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(expectedUser);
        mockMvc.perform(get("/user/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("firstName", expectedUser.getFirstName()))
                .andExpect(model().attribute("lastName", expectedUser.getLastName()))
                .andExpect(model().attribute("email", expectedUser.getEmail()))
                .andExpect(model().attribute("profilePicture", expectedUser.getProfilePicture()));
    }

    /**
     * Tests the edit function on the page when the updated details are in the correct format.
     * Test simulates a user making an edit to the user details so that all of them are in the correct format.
     * Expects the edit to be approved and to go back to the profile page.
     * @throws Exception if the request processing fails
     */
    @Test
    @WithMockUser(username = "jane@doe.com")
    public void postForm_validUserDetails_exitEditor() throws Exception {
        User expectedUser = new User("Jane", "Doe", "jane@doe.com", "password");
        User updatedUser = new User("John", "Doe", "john@doe.com", "password");
        expectedUser.grantAuthority("ROLE_USER");
        updatedUser.grantAuthority("ROLE_USER");
        userRepository.save(expectedUser);

        mockMvc.perform(post("/user/edit")
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", updatedUser.getLastName())
                        .param("email", updatedUser.getEmail())
                        .param("password", updatedUser.getPassword())
                        .param("profilePicture", updatedUser.getProfilePicture()))
                .andExpect(status().isFound())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "/user"));

        User savedUser = userRepository.findByEmailIgnoreCase(updatedUser.getEmail()).orElseThrow();

        Assertions.assertEquals(updatedUser.getFirstName(), savedUser.getFirstName());
        Assertions.assertEquals(updatedUser.getLastName(), savedUser.getLastName());
        Assertions.assertEquals(updatedUser.getEmail(), savedUser.getEmail());
        Assertions.assertEquals(updatedUser.getProfilePicture(), savedUser.getProfilePicture());
    }

    /**
     * Tests the edit function on the page when the name details are missing/invalid.
     * Test simulates a user making an edit to the user details but the names are missing.
     * Expects to get "First name cannot be empty." error messages.
     * @throws Exception if the request processing fails
     */
    @Test
    @WithMockUser(username = "jane@doe.com")
    public void postForm_missingNames_returnNameFormatError() throws Exception {
        User expectedUser = new User("Jane", "Doe", "jane@doe.com", "password");
        User updatedUser = new User("", "", "jane@doe.com", "password");
        List<String> expectedErrors = List.of("First name cannot be empty.");
        userRepository.save(expectedUser);
        mockMvc.perform(post("/user/edit")
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", updatedUser.getLastName())
                        .param("email", updatedUser.getEmail())
                        .param("password", updatedUser.getPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/edit"))
                .andExpect(flash().attribute("firstNameError", expectedErrors))
                .andExpect(flash().attributeExists("user"))
                .andExpect(flash().attribute("firstName", expectedUser.getFirstName()))
                .andExpect(flash().attribute("lastName", expectedUser.getLastName()))
                .andExpect(flash().attribute("email", expectedUser.getEmail()))
                .andExpect(flash().attribute("profilePicture", expectedUser.getProfilePicture()));
    }

    /**
     * Tests the edit function on the page when the email details are incorrect (email can't exist).
     * Test simulates a user making an edit to the user details but the email is formatted incorrectly.
     * Expects to get "Email address must be in the form ‘jane@doe.nz’." error message.
     * @throws Exception if the request processing fails
     */
    @Test
    @WithMockUser(username = "jane@doe.com")
    public void postForm_invalidEmailFormat_returnEmailFormatError() throws Exception {
        User expectedUser = new User("Jane", "Doe", "jane@doe.com", "password");
        User updatedUser = new User("Jane", "Doe", "jane@", "password");
        List<String> expectedErrors = new ArrayList<>();
        expectedErrors.add("Email address must be in the form 'jane@doe.nz'.");
        userRepository.save(expectedUser);
        mockMvc.perform(post("/user/edit")
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", updatedUser.getLastName())
                        .param("email", updatedUser.getEmail())
                        .param("password", updatedUser.getPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/edit"))
                .andExpect(flash().attribute("emailError", expectedErrors))
                .andExpect(flash().attributeExists("user"))
                .andExpect(flash().attribute("firstName", expectedUser.getFirstName()))
                .andExpect(flash().attribute("lastName", expectedUser.getLastName()))
                .andExpect(flash().attribute("email", expectedUser.getEmail()))
                .andExpect(flash().attribute("profilePicture", expectedUser.getProfilePicture()));
    }

    /**
     * Tests the edit function on the page when the email details already exist with another user.
     * Test simulates a user making an edit to the user details but the email is already associates with another account.
     * Expects to get "This email address is already in use." error message.
     * @throws Exception if the request processing fails
     */
    @Test
    @WithMockUser(username = "jane@doe.com") // Use the same email as expectedUser1
    public void postForm_emailAlreadyExists_returnEmailExistsError() throws Exception {
        // Mock users with different emails for the test
        User expectedUser1 = new User("Jane", "Doe", "jane@doe.com", "password");
        User expectedUser2 = new User("Jane", "Doe", "doe@jane.com", "password");
        List<String> expectedErrors = List.of("This email address is already in use.");

        userRepository.save(expectedUser1);
        userRepository.save(expectedUser2);

        // Perform the POST request with updatedUser's details (changing email to 'doe@jane.com')
        mockMvc.perform(post("/user/edit")
                        .param("firstName", expectedUser1.getFirstName())
                        .param("lastName", expectedUser1.getLastName())
                        .param("email", expectedUser2.getEmail())
                        .param("password", expectedUser1.getPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/edit"))
                .andExpect(flash().attribute("emailError", expectedErrors))
                .andExpect(flash().attributeExists("user"))
                .andExpect(flash().attribute("firstName", expectedUser1.getFirstName()))
                .andExpect(flash().attribute("lastName", expectedUser1.getLastName()))
                .andExpect(flash().attribute("email", expectedUser1.getEmail()))
                .andExpect(flash().attribute("profilePicture", expectedUser1.getProfilePicture()));
    }



    /**
     * Tests the upload profile picture function when the user submits a file for invalid format
     * Expects to get "Image must be of type png, jpg or svg." error and redirect to /user/edit
     */
    @Test
    @WithMockUser(username = "jane@doe.com")
    public void uploadProfilePicture_invalidFormat_returnError() throws Exception {
        User testUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(testUser);

        List<String> expectedErrors = List.of("Image must be of type png, jpg or svg.");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "invalid image".getBytes()
        );
        mockMvc.perform(MockMvcRequestBuilders.multipart("/user/edit/profile-picture")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/edit"))
                .andExpect(flash().attribute("profilePictureError", expectedErrors));
    }

    /**
     * Tests the upload profile picture function when the user submits a file larger than 10MB
     * Expects to get "Image must be less than 10MB." error and redirect to /user/edit
     */
    @Test
    @WithMockUser(username = "jane@doe.com")
    public void uploadProfilePicture_fileTooLarge_returnError() throws Exception {
        User testUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(testUser);

        List<String> expectedErrors = List.of("Image must be less than 10MB.");

        byte[] largeFileContent = new byte[(10 * 1024 * 1024) + 1];
        Arrays.fill(largeFileContent, (byte) '0');

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                largeFileContent
        );
        mockMvc.perform(MockMvcRequestBuilders.multipart("/user/edit/profile-picture")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/edit"))
                .andExpect(flash().attribute("profilePictureError", expectedErrors));
    }

    /**
     * Tests the upload profile picture function when the user submits a valid image file
     * Expects redirect to /user
     */
    @Test
    @WithMockUser(username = "jane@doe.com")
    public void uploadProfilePicture_validImageFile_success() throws Exception {
        User testUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(testUser);

        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/test_1.jpg"));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test_1.jpg",
                "image/jpeg",
                imageBytes
        );
        mockMvc.perform(MockMvcRequestBuilders.multipart("/user/edit/profile-picture")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user"));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void getForm_userWithLocation_locationPrefilled() throws Exception {
        User testUser = new User("Jane", "Doe", "jane@doe.com", "password");
        Location location = new Location("123 Linwood Ave", "New Zealand", "8045", "Christchurch", "Linwood");
        testUser.setLocation(location);
        userRepository.save(testUser);

        mockMvc.perform(get("/user/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("addressDTO",
                        Matchers.allOf(
                                Matchers.hasProperty("address_line1", Matchers.is("123 Linwood Ave")),
                                Matchers.hasProperty("country", Matchers.is("New Zealand")),
                                Matchers.hasProperty("postcode", Matchers.is("8045")),
                                Matchers.hasProperty("city", Matchers.is("Christchurch")),
                                Matchers.hasProperty("region", Matchers.is("Linwood"))
                        )
                ));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void postForm_validDataWithLocation_shouldUpdateUserAndPersistLocation() throws Exception {
        User testUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(testUser);

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress_line1("164 Ingoldsby Street");
        addressDTO.setCountry("New Zealand");
        addressDTO.setPostcode("8023");
        addressDTO.setCity("Christchurch");
        addressDTO.setRegion("Beckenham");

        mockMvc.perform(post("/user/edit")
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane@doe.com")
                        .param("address_line1", addressDTO.getAddress_line1())
                        .param("country", addressDTO.getCountry())
                        .param("postcode", addressDTO.getPostcode())
                        .param("city", addressDTO.getCity())
                        .param("region", addressDTO.getRegion()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user"));

        User savedUser = userRepository.findByEmailIgnoreCase("jane@doe.com").orElseThrow();
        assertNotNull(savedUser.getLocation());
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void postForm_invalidLocation_shouldRedirectWithErrors() throws Exception {
        User testUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(testUser);

        mockMvc.perform(post("/user/edit")
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane@doe.com")
                        .param("address_line1", "!!!")
                        .param("country", "@@@")
                        .param("postcode", "ABC")
                        .param("city", "###")
                        .param("region", "909"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/edit"))
                .andExpect(flash().attributeExists("addressDTO"))
                .andExpect(flash().attribute("addressDTO",
                        Matchers.allOf(
                                Matchers.hasProperty("address_line1", Matchers.is("!!!")),
                                Matchers.hasProperty("country", Matchers.is("@@@")),
                                Matchers.hasProperty("postcode", Matchers.is("ABC")),
                                Matchers.hasProperty("city", Matchers.is("###")),
                                Matchers.hasProperty("region", Matchers.is("909"))
                        )
                ));

        User savedUser = userRepository.findByEmailIgnoreCase("jane@doe.com").orElseThrow();
        assertNull(savedUser.getLocation());
    }
}