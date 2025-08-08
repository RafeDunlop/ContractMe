package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.RegisterController;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;

import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RegisterControllerIntegrationTest {
    @Autowired
    private RegisterController registerController;

    /**
     * MockMvc instance used for simulating HTTP requests.
     */
    private MockMvc mockMvc;

    /**
     * Mocked repository to avoid actual database interactions.
     */
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ContractorRepository contractorRepository;
    @MockBean
    private VerificationCodeRepository verificationCodeRepository;
    @MockBean
    private EmailService emailService;
    @SpyBean
    private LocationService locationService;

    private User expectedUser;
    private String expectedPassword;

    private static Stream<Arguments> streamValidLocationInputsWithCoordinates() {
        return Stream.of(
                Arguments.of("1 Address", "Suburb", "City", "1111", "Country", 45D, 90D),
                Arguments.of("2 Address", "Suburb", "City", "", "", -45D, -90D),
                Arguments.of("3 Address", "", "", "", "", 0.001D, -0.001D)
        );
    }

    private static Stream<Arguments> streamValidLocationInputsWithoutCoordinates() {
        return Stream.of(
                Arguments.of("1 Address", "Suburb", "City", "1111", "Country"),
                Arguments.of("2 Address", "Suburb", "City", "", ""),
                Arguments.of("3 Address", "", "", "", "")
        );
    }

    private void createValidUser() {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        expectedPassword = "Test123!";
        expectedUser = Mockito.spy(new User("Jane", "Doe", "jane@doe.nz", passwordEncoder.encode(expectedPassword)));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(expectedUser);
    }

    /**
     * Initializes the {@link MockMvc} instance with a new setup of the {@link RegisterController}.
     */
    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(registerController).build();
    }

    /**
     * Tests the registration of a valid user.
     * This test simulates a user submitting a valid registration form and expects:
     * A redirection (3xx status) upon successful registration.
     * A redirection to .sendVerificationEmail(Mockito.anyString(), Mockito.anyString()),the user profile page.
     * @throws Exception if the request processing fails.
     */
    @Test
    public void testRegisterUser_validUser_success() throws Exception {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        User expectedUser = Mockito.spy(new User("Jane", "Doe", "jane@doe.nz", passwordEncoder.encode("Test123!")));
        Mockito.when(expectedUser.getId()).thenReturn(1L);
        Mockito.when(verificationCodeRepository.save(Mockito.any(VerificationCode.class))).thenAnswer((InvocationOnMock) -> null);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(expectedUser);
        Mockito.when(userRepository.findByEmailIgnoreCase(Mockito.anyString())).thenReturn(Optional.empty()).thenReturn(Optional.of(expectedUser));
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("firstName", "Jane")
            .param("lastName", "Doe")
            .param("email", "jane@doe.nz")
            .param("password", "Test123!")
            .param("confirmPassword", "Test123!")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(view().name("redirect:/confirm-registration"));
        verify(emailService, times(1)).sendVerificationEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Locale.class));
    }

    /**
     * Tests the registration of an invalid user.sendVerificationEmail(Mockito.anyString(), Mockito.anyString()),
     * This test simulates a user submitting an invalid registration form and expects:
     * A return to the page (200 status) with an error message.
     * First name, Last name and Email should be remembered
     * @throws Exception if the request processing fails.
     */
    @Test
    public void testRegisterUser_invalidUser_fail() throws Exception {
        List<String> expectedErrorList = List.of("Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email).");
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane@doe.nz")
                        .param("password", "password")
                        .param("confirmPassword", "password")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("passwordError", expectedErrorList))
                .andExpect(flash().attribute("userRegisterDTO", Matchers.hasProperty("firstName", Matchers.equalTo("Jane"))))
                .andExpect(flash().attribute("userRegisterDTO", Matchers.hasProperty("lastName", Matchers.equalTo("Doe"))))
                .andExpect(flash().attribute("userRegisterDTO", Matchers.hasProperty("email", Matchers.equalTo("jane@doe.nz"))));
        verify(emailService, Mockito.never()).sendVerificationEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Locale.class));
    }

    /**
     * Tests the user activation of a valid verification code
     * This test simulates a user submitting a valid verification code on the confirm registration page and expects:
     * A redirect to /login (3xx status).
     * User should be saved
     * @throws Exception if the request processing fails.
     */
    @Test
    public void testConfirmRegistration_validCode_userActivatedAndVerificationCodeDeleted() throws Exception {
        User mockUser = mock(User.class);
        String testCode = "123456";
        Locale testLocale = Locale.ENGLISH;
        VerificationCode verificationCode = new VerificationCode(mockUser, testCode, testLocale);

        Mockito.when(verificationCodeRepository.findByCode(testCode)).thenReturn(Optional.of(verificationCode));

        mockMvc.perform(MockMvcRequestBuilders.post("/confirm-registration")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("code", testCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"))
                .andExpect(flash().attribute("loginMessage", "Your account has been activated, please log in"));


        verify(mockUser, times(1)).activate();
        verify(verificationCodeRepository, times(1)).delete(verificationCode);
    }

    @Test
    public void testConfirmRegistration_invalidCode_userNotActivated() throws Exception {
        User mockUser = mock(User.class);
        String testCode = "123456";
        String expectedError = "Signup code invalid";

        Mockito.when(verificationCodeRepository.findByCode(testCode)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/confirm-registration")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("code", testCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/confirm-registration"))
                .andExpect(flash().attribute("errorMessage", expectedError));

        verify(verificationCodeRepository, times(1)).findByCode(testCode);
        verify(mockUser, never()).activate();
        verify(userRepository, never()).save(mockUser);
        verify(verificationCodeRepository, never()).delete(Mockito.any(VerificationCode.class));
    }

    @Test
    public void testRegisterContractor_validContractor_success() throws Exception {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        Contractor expectedUser = Mockito.spy(new Contractor("Jane", "Doe", "jane@doe.nz", passwordEncoder.encode("Test123!")));
        expectedUser.setHourlyRate(27.80f);
        expectedUser.setPhoneNumber("6412345678");
        Mockito.when(expectedUser.getId()).thenReturn(1L);
        Mockito.when(verificationCodeRepository.save(Mockito.any(VerificationCode.class))).thenAnswer((InvocationOnMock) -> null);
        when(contractorRepository.save(Mockito.any(Contractor.class))).thenReturn(expectedUser);
        Mockito.when(contractorRepository.findByEmailIgnoreCase(Mockito.anyString())).thenReturn(Optional.empty()).thenReturn(Optional.of(expectedUser));
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane@doe.nz")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .param("hourlyRate", "27.80")
                        .param("isContractor", "true")
                        .param("phoneNumber", "12345678")
                        .param("countryCode", "64")
                        .param("skills", "ELECTRICAL")
                        .param("address_line1", "62 Ilam Road") // <- IMPORTANT: make sure param name matches controller!
                        .param("suburb", "Riccarton")
                        .param("city", "Christchurch")
                        .param("postcode", "8041")
                        .param("country", "New Zealand")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(view().name("redirect:/confirm-registration"));
        verify(emailService, times(1)).sendVerificationEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Locale.class));
    }

    @Test
    public void testRegisterContractor_skillsAreNull_rejectInputWithSkillError() throws Exception {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        Contractor expectedUser = Mockito.spy(new Contractor("Jane", "Doe", "jane@doe.nz", passwordEncoder.encode("Test123!")));
        expectedUser.setHourlyRate(27.80f);
        expectedUser.setPhoneNumber("6412345678");
        Mockito.when(expectedUser.getId()).thenReturn(1L);
        Mockito.when(verificationCodeRepository.save(Mockito.any(VerificationCode.class))).thenAnswer((InvocationOnMock) -> null);
        when(contractorRepository.save(Mockito.any(Contractor.class))).thenReturn(expectedUser);
        Mockito.when(contractorRepository.findByEmailIgnoreCase(Mockito.anyString())).thenReturn(Optional.empty()).thenReturn(Optional.of(expectedUser));
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane@doe.nz")
                        .param("password", "Test123!")
                        .param("confirmPassword", "Test123!")
                        .param("hourlyRate", "27.80")
                        .param("isContractor", "true")
                        .param("phoneNumber", "12345678")
                        .param("countryCode", "64")
                        .param("skills", (String) null)
                        .param("address_line1", "62 Ilam Road") // <- IMPORTANT: make sure param name matches controller!
                        .param("suburb", "Riccarton")
                        .param("city", "Christchurch")
                        .param("postcode", "8041")
                        .param("country", "New Zealand")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(view().name("redirect:/register"))
                .andExpect(flash().attribute("skillsError", List.of("You must select one or more skills")));
        verify(emailService, times(0)).sendVerificationEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Locale.class));
    }

    @ParameterizedTest
    @MethodSource("streamValidLocationInputsWithCoordinates")
    public void submitRegistration_inputValidLocationsWithCoordinates_successfulRegistrationWithAutocompleteCoordinates(String address, String suburb, String city,
                                                                                                                        String postcode, String country, Double lat,
                                                                                                                        Double lon) throws Exception {
        createValidUser();
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", expectedUser.getFirstName())
                        .param("lastName", expectedUser.getLastName())
                        .param("email", expectedUser.getEmail())
                        .param("password", expectedPassword)
                        .param("confirmPassword", expectedPassword)
                        .param("address_line1", address)
                        .param("region", suburb)
                        .param("city", city)
                        .param("postcode", postcode)
                        .param("country", country)
                        .param("lat", Double.toString(lat))
                        .param("lon", Double.toString(lon)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(view().name("redirect:/confirm-registration"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.atLeastOnce()).save(userCaptor.capture());
        User registeredUser = userCaptor.getValue();
        Location registeredUserLocation = registeredUser.getLocation();

        Assertions.assertEquals(address, registeredUserLocation.getAddress());
        Assertions.assertEquals(suburb, registeredUserLocation.getSuburb());
        Assertions.assertEquals(city, registeredUserLocation.getCity());
        Assertions.assertEquals(postcode, registeredUserLocation.getPostcode());
        Assertions.assertEquals(country, registeredUserLocation.getCountry());
        Assertions.assertEquals(lat, registeredUserLocation.getLatitude());
        Assertions.assertEquals(lon, registeredUserLocation.getLongitude());
    }

    @ParameterizedTest
    @MethodSource("streamValidLocationInputsWithoutCoordinates")
    public void submitRegistration_inputValidLocationsWithoutCoordinates_successfulRegistrationWithGeolocateCoordinates(String address, String suburb, String city,
                                                                                                                        String postcode, String country) throws Exception {
        createValidUser();

        // Prevent call to API and instead add coordinates to DTO when injectCoordsViaGeocoding is called.
        Mockito.doAnswer(invocationOnMock -> {
            AddressDTO mockAddressDTO = invocationOnMock.getArgument(0);
            mockAddressDTO.setLat(1D);
            mockAddressDTO.setLon(1D);
            return null;
        }).when(locationService).injectCoordsViaGeocoding(Mockito.any(AddressDTO.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", expectedUser.getFirstName())
                        .param("lastName", expectedUser.getLastName())
                        .param("email", expectedUser.getEmail())
                        .param("password", expectedPassword)
                        .param("confirmPassword", expectedPassword)
                        .param("address_line1", address)
                        .param("region", suburb)
                        .param("city", city)
                        .param("postcode", postcode)
                        .param("country", country))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(view().name("redirect:/confirm-registration"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.atLeastOnce()).save(userCaptor.capture());
        User registeredUser = userCaptor.getValue();
        Location registeredUserLocation = registeredUser.getLocation();

        Assertions.assertEquals(address, registeredUserLocation.getAddress());
        Assertions.assertEquals(suburb, registeredUserLocation.getSuburb());
        Assertions.assertEquals(city, registeredUserLocation.getCity());
        Assertions.assertEquals(postcode, registeredUserLocation.getPostcode());
        Assertions.assertEquals(country, registeredUserLocation.getCountry());
        Assertions.assertEquals(1D, registeredUserLocation.getLatitude());
        Assertions.assertEquals(1D, registeredUserLocation.getLongitude());
    }

    @Test
    public void submitRegistration_inputInvalidLocationsWithoutCoordinates_successfulRegistrationWithIpCoordinates() throws Exception {
        createValidUser();
        Location expectedLocation = new Location("Fake Place", "Fake Country", "0000", "Fake City", "Fake Suburb");

        // Throw an exception to simulate the API failing to find coordinates for the inputted location.
        Mockito.doThrow(IllegalArgumentException.class).when(locationService).injectCoordsViaGeocoding(Mockito.any(AddressDTO.class));

        // Prevent call to API and instead add coordinates to DTO when injectCoordsViaIpGeolocation is called.
        Mockito.doAnswer(invocationOnMock -> {
            AddressDTO mockAddressDTO = invocationOnMock.getArgument(0);
            mockAddressDTO.setLat(1D);
            mockAddressDTO.setLon(1D);
            return null;
        }).when(locationService).injectCoordsViaIpGeolocation(Mockito.any(AddressDTO.class), Mockito.anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", expectedUser.getFirstName())
                        .param("lastName", expectedUser.getLastName())
                        .param("email", expectedUser.getEmail())
                        .param("password", expectedPassword)
                        .param("confirmPassword", expectedPassword)
                        .param("address_line1", expectedLocation.getAddress())
                        .param("region", expectedLocation.getSuburb())
                        .param("city", expectedLocation.getCity())
                        .param("postcode", expectedLocation.getPostcode())
                        .param("country", expectedLocation.getCountry()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(view().name("redirect:/confirm-registration"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.atLeastOnce()).save(userCaptor.capture());
        User registeredUser = userCaptor.getValue();
        Location registeredUserLocation = registeredUser.getLocation();

        Assertions.assertEquals(expectedLocation.getAddress(), registeredUserLocation.getAddress());
        Assertions.assertEquals(expectedLocation.getSuburb(), registeredUserLocation.getSuburb());
        Assertions.assertEquals(expectedLocation.getCity(), registeredUserLocation.getCity());
        Assertions.assertEquals(expectedLocation.getPostcode(), registeredUserLocation.getPostcode());
        Assertions.assertEquals(expectedLocation.getCountry(), registeredUserLocation.getCountry());
        Assertions.assertEquals(1D, registeredUserLocation.getLatitude());
        Assertions.assertEquals(1D, registeredUserLocation.getLongitude());
    }
}
