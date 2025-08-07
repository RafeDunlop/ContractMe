package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EditProfileService;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EditProfileServiceTest {

    private UserRepository userRepository;
    private UserValidation userValidation;
    private EditProfileService editProfileService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userValidation = Mockito.mock(UserValidation.class);
        LocationService locationService = Mockito.mock(LocationService.class);
        editProfileService = new EditProfileService(userRepository, userValidation, locationService);

        // Set up mock authentication for tests
        User currentUser = new User("John", "Smith", "john@smith.com", "password");
        currentUser.grantAuthority("ROLE_USER");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(currentUser, currentUser.getPassword(), currentUser.getAuthorities());
        SecurityContext context = Mockito.mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void updateUser_validDetails_userSavedSuccessfully() {
        User updatedUser = new User("John", "Smith", "john@smith.com", "password");
        updatedUser.grantAuthority("ROLE_USER");

        when(userRepository.findByEmailIgnoreCase(updatedUser.getEmail())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> editProfileService.updateUser(updatedUser));
        verify(userRepository, times(1)).save(updatedUser);
    }

    @Test
    public void validateUser_validName_noErrors() {
        User user = new User("John", "Smith", "john@smith.com", "password");

        List<String> firstNameErrors = userValidation.validateNameString(user.getFirstName(), "First");
        List<String> lastNameErrors = userValidation.validateNameString(user.getLastName(), "Last");

        assertTrue(firstNameErrors.isEmpty());
        assertTrue(lastNameErrors.isEmpty());
    }

    @Test
    public void validateUser_invalidName_returnsErrors() {
        User user = new User("J@hn!", "Sm1th#", "john@smith.com", "password");
        userValidation = new UserValidation();

        List<String> firstNameErrors = userValidation.validateNameString(user.getFirstName(), "First");
        List<String> lastNameErrors = userValidation.validateNameString(user.getLastName(), "Last");

        assertFalse(firstNameErrors.isEmpty());
        assertFalse(lastNameErrors.isEmpty());

        assertEquals("First name must only include letters, spaces, hyphens, or apostrophes.", firstNameErrors.get(0));
        assertEquals("Last name must only include letters, spaces, hyphens, or apostrophes.", lastNameErrors.get(0));
    }

    @Test
    void updateProfilePicture_DirectoryDoesNotExist_directoryCreatedProfilePictureSaved() throws Exception {
        User mockUser = new User("Jake", "Smith", "jake@example.com", "password");
        MultipartFile profilePicture = Mockito.mock(MultipartFile.class);

        when(profilePicture.getOriginalFilename()).thenReturn("avatar.jpg");
        when(profilePicture.getBytes()).thenReturn(new byte[]{4, 5, 6});
        InputStream inputStream = new ByteArrayInputStream(new byte[]{4, 5, 6});
        when(profilePicture.getInputStream()).thenReturn(inputStream);

        BufferedImage dummyImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

        try (MockedStatic<ImageIO> mockedImageIO = mockStatic(ImageIO.class)) {
            mockedImageIO.when(() -> ImageIO.read(any(InputStream.class))).thenReturn(dummyImage);

            try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
                mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);
                mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenReturn(null);
                mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class), any())).thenReturn(null);

                editProfileService.updateProfilePicture(mockUser, profilePicture);
            }

            assertNotNull(mockUser.getProfilePicture());
            verify(userRepository, times(1)).save(mockUser);
            mockedImageIO.verify(() -> ImageIO.read(any(InputStream.class)), times(1));
        }
    }

    @Test
    void updateProfilePicture_DirectoryExists_saveProfilePicture() throws Exception {
        User mockUser = new User("Jake", "Smith", "jake@example.com", "password");
        MultipartFile profilePicture = Mockito.mock(MultipartFile.class);

        when(profilePicture.getOriginalFilename()).thenReturn("avatar.jpg");
        when(profilePicture.getBytes()).thenReturn(new byte[]{4, 5, 6});
        InputStream inputStream = new ByteArrayInputStream(new byte[]{4, 5, 6});
        when(profilePicture.getInputStream()).thenReturn(inputStream);

        BufferedImage dummyImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

        try (MockedStatic<ImageIO> mockedImageIO = mockStatic(ImageIO.class)) {
            mockedImageIO.when(() -> ImageIO.read(any(InputStream.class))).thenReturn(dummyImage);

            try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
                mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(true);
                mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class), any())).thenReturn(null);

                editProfileService.updateProfilePicture(mockUser, profilePicture);
            }

            assertNotNull(mockUser.getProfilePicture());
            verify(userRepository, times(1)).save(mockUser);
            mockedImageIO.verify(() -> ImageIO.read(any(InputStream.class)), times(1));
        }
    }


    @Test
    void updateProfilePicture_fileWriteError_IOExceptionThrown() throws Exception {
        User mockUser = new User("Jake", "Smith", "jake@example.com", "password");
        MultipartFile profilePicture = Mockito.mock(MultipartFile.class);

        when(profilePicture.getOriginalFilename()).thenReturn("avatar.jpg");
        when(profilePicture.getBytes()).thenReturn(new byte[]{4, 5, 6});
        InputStream inputStream = new ByteArrayInputStream(new byte[]{4, 5, 6});
        when(profilePicture.getInputStream()).thenReturn(inputStream);

        BufferedImage dummyImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

        try (MockedStatic<ImageIO> mockedImageIO = mockStatic(ImageIO.class)) {
            mockedImageIO.when(() -> ImageIO.read(any(InputStream.class))).thenReturn(dummyImage);
            mockedImageIO.when(() -> ImageIO.write(any(BufferedImage.class), eq("jpg"), any(File.class)))
                    .thenThrow(new IOException("Failed to write file."));

            try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
                mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(true); // Directory exists

                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                    editProfileService.updateProfilePicture(mockUser, profilePicture);
                });

                assertEquals("Failed to store file.", exception.getMessage());
                verify(userRepository, never()).save(mockUser);
                mockedImageIO.verify(() -> ImageIO.read(any(InputStream.class)), times(1));
                mockedImageIO.verify(() -> ImageIO.write(any(BufferedImage.class), eq("jpg"), any(File.class)), times(1));
            }
        }
    }
}
