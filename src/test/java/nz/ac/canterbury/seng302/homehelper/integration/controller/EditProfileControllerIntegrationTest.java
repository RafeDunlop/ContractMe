package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.controller.EditProfileController;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
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
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "jane@doe.com")
@ActiveProfiles("test")
class EditProfileControllerIntegrationTest {

    @Autowired
    private EditProfileController editProfileController;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginService loginService;

    @SpyBean
    private LocationService locationService;

    @PostConstruct
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(editProfileController).build();
    }

    private static Stream<Arguments> streamInvalidLocationsWithCoords() {
        return Stream.of(
                Arguments.of("23a4s567g8h9uji0k", -43.535915D, 172.620323D),
                Arguments.of("4tttttttttttnvtoimqr40r9qucm4", -43.548888D, 172.620444D),
                Arguments.of("10000000 Fake Address Street", -43.565656D, 172.621555D)
        );
    }

    private static Stream<Arguments> streamValidContractorDetails() {
        return Stream.of(
                Arguments.of(27.08f, "64", "6412345678", Set.of(Skill.CARPENTRY)),
                Arguments.of(0, "1", "11111 1111", Set.of(Skill.EARTHMOVING)),
                Arguments.of(9999f, "99", "9 9 9 9 9 9 9 9", Set.of(Skill.SEPTIC_SYSTEMS, Skill.HVAC, Skill.MECHANICAL_ENGINEERING))
        );
    }

    private static Stream<Arguments> streamInvalidContractorDetails() {
        List<Object> errorsList = List.of(List.of("Invalid hourly rate"), List.of("Your phone number is invalid", "Invalid country code"), List.of("You must select one or more skills"));
        return Stream.of(
                Arguments.of(-10f, "0", "999", errorsList),
                Arguments.of(-999, "1000", "9999999999999999", errorsList),
                Arguments.of(-99999, "10001", "9 9 9 9 9#$$%", errorsList),
                Arguments.of(-99999, "10001", "99", errorsList)
        );
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
                .andExpect(flash().attribute("firstName", updatedUser.getFirstName()))
                .andExpect(flash().attribute("lastName", updatedUser.getLastName()))
                .andExpect(flash().attribute("email", updatedUser.getEmail()));
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
                .andExpect(flash().attribute("firstName", updatedUser.getFirstName()))
                .andExpect(flash().attribute("lastName", updatedUser.getLastName()))
                .andExpect(flash().attribute("email", updatedUser.getEmail()));
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
                .andExpect(flash().attribute("firstName", expectedUser1.getFirstName()))
                .andExpect(flash().attribute("lastName", expectedUser1.getLastName()))
                .andExpect(flash().attribute("email", expectedUser2.getEmail()));
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
        addressDTO.setLat(1D);
        addressDTO.setLon(1D);
        Location expectedLocation = new Location(addressDTO.getAddress_line1(), addressDTO.getCountry(), addressDTO.getPostcode(), addressDTO.getCity(), addressDTO.getRegion(), addressDTO.getLat(), addressDTO.getLon());
        doReturn(expectedLocation).when(locationService).locate(addressDTO);

        mockMvc.perform(post("/user/edit")
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane@doe.com")
                        .param("address_line1", addressDTO.getAddress_line1())
                        .param("country", addressDTO.getCountry())
                        .param("postcode", addressDTO.getPostcode())
                        .param("city", addressDTO.getCity())
                        .param("region", addressDTO.getRegion())
                        .param("lat", Double.toString(addressDTO.getLat()))
                        .param("lon", Double.toString(addressDTO.getLon())))
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

    @ParameterizedTest
    @MethodSource("streamInvalidLocationsWithCoords")
    void postForm_invalidLocationWithValidCoords_shouldRedirectWithErrors(String address, Double latitude, Double longitude) throws Exception {
        User testUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(testUser);

        mockMvc.perform(post("/user/edit")
                .param("firstName", "Jane")
                .param("lastName", "Doe")
                .param("email", "jane@doe.com")
                .param("address_line1", address)
                .param("country", "")
                .param("postcode", "")
                .param("city", "")
                .param("region", "")
                .param("lat", latitude.toString())
                .param("lon", longitude.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/edit"))
                .andExpect(flash().attributeExists("addressDTO"))
                .andExpect(flash().attribute("addressDTO",
                        Matchers.allOf(
                                Matchers.hasProperty("address_line1", Matchers.is(address)),
                                Matchers.hasProperty("country", Matchers.is("")),
                                Matchers.hasProperty("postcode", Matchers.is("")),
                                Matchers.hasProperty("city", Matchers.is("")),
                                Matchers.hasProperty("region", Matchers.is("")),
                                Matchers.hasProperty("lat", Matchers.is(latitude)),
                                Matchers.hasProperty("lon", Matchers.is(longitude))
                        )
                ));

        User savedUser = userRepository.findByEmailIgnoreCase("jane@doe.com").orElseThrow();
        assertNull(savedUser.getLocation());
    }

    @ParameterizedTest
    @MethodSource("streamValidContractorDetails")
    void testEditContractor_validUserDetails_exitEditor(float hourlyRate, String countryCode, String phoneNumber, Set<Skill> skills) throws Exception {
        Contractor current = new Contractor("Jane", "Doe", "jane@doe.com", "password");
        current.grantAuthority("ROLE_USER");
        current.setHourlyRate(27.80f);
        current.setPhoneNumber("6412345678");
        current.addSkill(Skill.CARPENTRY);
        Location location = new Location("123 Linwood Ave", "New Zealand", "8045", "Christchurch", "Linwood");
        current.setLocation(location);
        userRepository.save(current);
        AddressDTO expectedAddressDTO = new AddressDTO();
        expectedAddressDTO.setAddress_line1("123 Ilam Road");
        expectedAddressDTO.setCountry("New Zealand");
        expectedAddressDTO.setPostcode("8042");
        expectedAddressDTO.setCity("Christchurch");
        expectedAddressDTO.setRegion("Ilam");
        expectedAddressDTO.setLat(1.0);
        expectedAddressDTO.setLon(1.0);
        Location expectedLocation = new Location(
                expectedAddressDTO.getAddress_line1(),
                expectedAddressDTO.getCountry(),
                expectedAddressDTO.getPostcode(),
                expectedAddressDTO.getCity(),
                expectedAddressDTO.getRegion(),
                expectedAddressDTO.getLat(),
                expectedAddressDTO.getLon()
        );
        doReturn(expectedLocation).when(locationService).locate(expectedAddressDTO);
        mockMvc.perform(post("/user/edit")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@doe.com")
                        .param("password", "password")
                        .param("address_line1", "123 Ilam Road")
                        .param("country", "New Zealand")
                        .param("postcode", "8042")
                        .param("city", "Christchurch")
                        .param("region", "Ilam")
                        .param("lat", "1.0")
                        .param("lon", "1.0")
                        .param("hourlyRate", Float.toString(hourlyRate))
                        .param("countryCode", countryCode)
                        .param("phoneNumber", phoneNumber)
                        .param("skills", skills.stream().map(Skill::toString).toArray(String[]::new))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user"));

        User savedUser = userRepository.findByEmailIgnoreCase("john@doe.com").orElseThrow();

        Assertions.assertEquals("John", savedUser.getFirstName());
        Assertions.assertEquals("Doe", savedUser.getLastName());
        Assertions.assertEquals("john@doe.com", savedUser.getEmail());

        Assertions.assertNotNull(savedUser.getLocation());
        Assertions.assertEquals("123 Ilam Road", savedUser.getLocation().getAddress());
        Assertions.assertEquals("8042", savedUser.getLocation().getPostcode());
        Assertions.assertEquals("Ilam", savedUser.getLocation().getSuburb());

        Assertions.assertInstanceOf(Contractor.class, savedUser);
        Contractor savedContractor = (Contractor) savedUser;
        Assertions.assertEquals(hourlyRate, savedContractor.getHourlyRate());
        Assertions.assertEquals(countryCode, savedContractor.getCountryCode());
        Assertions.assertEquals(phoneNumber, savedContractor.getPhoneNumber());
    }

    @ParameterizedTest
    @MethodSource("streamInvalidContractorDetails")
    void testEditContractor_invalidContractorDetails_stayOnEditProfilePage(float hourlyRate, String countryCode, String phoneNumber, List<Object> expectedErrors) throws Exception {
        Contractor current = new Contractor("Jane", "Doe", "jane@doe.com", "password");
        current.grantAuthority("ROLE_USER");
        current.setHourlyRate(27.80f);
        current.setPhoneNumber("6412345678");
        current.addSkill(Skill.CARPENTRY);
        current.setLocation(
                new Location("123 Linwood Ave", "New Zealand", "8045", "Christchurch", "Linwood"));
        userRepository.save(current);

        mockMvc.perform(post("/user/edit")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john@doe.com")
                .param("password", "password")
                .param("address_line1", "123 Ilam Road")
                .param("country", "New Zealand")
                .param("postcode", "8042")
                .param("city", "Christchurch")
                .param("region", "Ilam")
                .param("lat", "1.0")
                .param("lon", "1.0")
                .param("hourlyRate", Float.toString(hourlyRate))
                .param("countryCode", countryCode)
                .param("phoneNumber", phoneNumber)
                .param("skills", (String) null)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/edit"))
                .andExpect(flash().attribute("hourlyRateError", expectedErrors.get(0)))
                .andExpect(flash().attribute("phoneNumberError", expectedErrors.get(1)))
                .andExpect(flash().attribute("skillsError", expectedErrors.get(2)));

    }

}