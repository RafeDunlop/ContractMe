package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EditProfileService;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        editProfileService = new EditProfileService(userRepository, userValidation);
    }

    @Test
    public void updateUser_validDetails_returnNoError() {
        User updatedUser = new User("John", "Smith", "john@smith.com", "password");
        updatedUser.grantAuthority("ROLE_USER");
        when(userValidation.validateNameString(updatedUser.getFirstName(), "First")).thenReturn(List.of());
        when(userValidation.validateNameString(updatedUser.getLastName(), "Last")).thenReturn(List.of());
        when(userRepository.findByEmailIgnoreCase(updatedUser.getEmail())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> editProfileService.updateUser(updatedUser, false));
    }

    @Test
    public void updateUser_namesInvalid_throwSameEmailError() {
        User updatedUser = new User("John!", "Smith?", "john@smith.com", "password");
        when(userValidation.validateNameString(updatedUser.getFirstName(), "First")).thenReturn(List.of("First name must only include letters, spaces, hyphens, or apostrophes."));
        when(userValidation.validateNameString(updatedUser.getLastName(), "Last")).thenReturn(List.of("Last name must only include letters, spaces, hyphens, or apostrophes."));
        when(userRepository.findByEmailIgnoreCase(updatedUser.getEmail())).thenReturn(Optional.empty());

        IllegalArgumentException errorMessage = assertThrows(IllegalArgumentException.class, () -> editProfileService.updateUser(updatedUser, false));
        assertEquals("First name must only include letters, spaces, hyphens, or apostrophes. Last name must only include letters, spaces, hyphens, or apostrophes.", errorMessage.getMessage());
    }

    @Test
    public void updateUser_sameEmailUsed_throwSameEmailError() {
        User currentUser = new User("John", "Smith", "john@smith.com", "password");
        User updatedUser = new User("John", "Smith", "john.smith@example.com", "password");
        when(userValidation.validateNameString(updatedUser.getFirstName(), "First")).thenReturn(List.of());
        when(userValidation.validateNameString(updatedUser.getLastName(), "Last")).thenReturn(List.of());
        when(userRepository.findByEmailIgnoreCase(updatedUser.getEmail())).thenReturn(Optional.of(currentUser));

        IllegalArgumentException errorMessage = assertThrows(IllegalArgumentException.class, () -> editProfileService.updateUser(updatedUser, false));
        assertEquals("This email address is already in use.", errorMessage.getMessage());
    }

    @Test
    void updateProfilePicture_DirectoryDoesNotExist_directoryCreatedProfilePictureSaved() throws Exception {
        User mockUser = new User("Jake", "Smith", "jake@example.com", "password");
        MultipartFile profilePicture = Mockito.mock(MultipartFile.class);
        when(profilePicture.getOriginalFilename()).thenReturn("avatar.jpg");
        when(profilePicture.getBytes()).thenReturn(new byte[]{4, 5, 6});

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);
            mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class), any())).thenReturn(null);
            editProfileService.updateProfilePicture(mockUser, profilePicture);
        }

        assertNotNull(mockUser.getProfilePicture());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void updateProfilePicture_DirectoryExists_saveProfilePicture() throws Exception {
        User mockUser = new User("Jake", "Smith", "jake@example.com", "password");
        MultipartFile profilePicture = Mockito.mock(MultipartFile.class);
        when(profilePicture.getOriginalFilename()).thenReturn("avatar.jpg");
        when(profilePicture.getBytes()).thenReturn(new byte[]{4, 5, 6});

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class), any())).thenReturn(null);
            editProfileService.updateProfilePicture(mockUser, profilePicture);
        }

        assertNotNull(mockUser.getProfilePicture());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void updateProfilePicture_fileWriteError_IOExceptionThrown() throws Exception {
        User mockUser = new User("Jake", "Smith", "jake@example.com", "password");
        MultipartFile profilePicture = Mockito.mock(MultipartFile.class);
        when(profilePicture.getOriginalFilename()).thenReturn("avatar.jpg");
        when(profilePicture.getBytes()).thenReturn(new byte[]{4, 5, 6});

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class), any())).thenThrow(new IOException("Failed to write file"));

            Exception exception = assertThrows(RuntimeException.class, () -> {
                editProfileService.updateProfilePicture(mockUser, profilePicture);
            });

            assertEquals("Failed to store file", exception.getMessage());
            verify(userRepository, never()).save(mockUser);
        }
    }
}
