package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EditProfileService;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

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

    /**
     * Tests the update user and validate methods when the user inputs valid details for the update.
     * Expects that no exception is thrown and the updates are accepted.
     */
    @Test
    public void updateUser_validDetails_returnNoError() {
        User updatedUser = new User("John", "Smith", "john@smith.com", "password");
        updatedUser.grantAuthority("ROLE_USER");
        Mockito.when(userValidation.validateNameString(updatedUser.getFirstName(), "First")).thenReturn(List.of());
        Mockito.when(userValidation.validateNameString(updatedUser.getLastName(), "Last")).thenReturn(List.of());
        Mockito.when(userRepository.findByEmailIgnoreCase(updatedUser.getEmail())).thenReturn(Optional.empty());
        Assertions.assertDoesNotThrow(() -> editProfileService.updateUser(updatedUser, false));
    }

    /**
     * Tests the update user and validate methods when the user inputs names with an invalid format.
     * Expects that an exception is thrown for both the invalid first and last name.
     */
    @Test
    public void updateUser_namesInvalid_throwSameEmailError() {
        User updatedUser = new User("John!", "Smith?", "john@smith.com", "password");
        Mockito.when(userValidation.validateNameString(updatedUser.getFirstName(), "First")).thenReturn(List.of("First name must only include letters, spaces, hyphens, or apostrophes."));
        Mockito.when(userValidation.validateNameString(updatedUser.getLastName(), "Last")).thenReturn(List.of("Last name must only include letters, spaces, hyphens, or apostrophes."));
        Mockito.when(userRepository.findByEmailIgnoreCase(updatedUser.getEmail())).thenReturn(Optional.empty());
        IllegalArgumentException errorMessage = Assertions.assertThrows(IllegalArgumentException.class, () -> editProfileService.updateUser(updatedUser, false));
        Assertions.assertEquals("First name must only include letters, spaces, hyphens, or apostrophes. Last name must only include letters, spaces, hyphens, or apostrophes.", errorMessage.getMessage());
    }

    /**
     * Tests the update user and validate methods when the user inputs an existing email address.
     * Expects that an exception is thrown for the existing email.
     */
    @Test
    public void updateUser_sameEmailUsed_throwSameEmailError() {
        User currentUser = new User("John", "Smith", "john@smith.com", "password");
        User updatedUser = new User("John", "Smith", "john.smith@example.com", "password");
        Mockito.when(userValidation.validateNameString(updatedUser.getFirstName(), "First")).thenReturn(List.of());
        Mockito.when(userValidation.validateNameString(updatedUser.getLastName(), "Last")).thenReturn(List.of());
        Mockito.when(userRepository.findByEmailIgnoreCase(updatedUser.getEmail())).thenReturn(Optional.of(currentUser));
        IllegalArgumentException errorMessage = Assertions.assertThrows(IllegalArgumentException.class, () -> editProfileService.updateUser(updatedUser, false));
        Assertions.assertEquals("This email address is already in use.", errorMessage.getMessage());
    }
}
