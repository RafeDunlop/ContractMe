package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.ForgotPasswordService;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ForgotPasswordServiceTest {

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private UserValidation userValidationMock;

    @Mock
    private ApplicationEventPublisher eventPublisherMock;

    @Mock
    private EmailService emailServiceMock;

    @InjectMocks
    private ForgotPasswordService forgotPasswordService;

    private User user;

    @BeforeEach
    void createUser() {
        user = new User("Jane", "Doe", "jane@doe.com", "password");
    }

    @Test
    void validateEmail_enterValidEmail_returnEmptyMessage() {
        String email = "jane@doe.com";
        Locale locale = Locale.ENGLISH;
        String expectedMessage = "";
        user.activate();
        Mockito.when(userRepositoryMock.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        Mockito.when(userValidationMock.validateEmailString(email)).thenReturn(List.of());

        String errorMessage = forgotPasswordService.validateEmail(email, locale);

        Mockito.verify(eventPublisherMock, times(1)).publishEvent(Mockito.any());
        assertEquals(expectedMessage, errorMessage);
    }

    @Test
    void validateEmail_enterUnauthenticatedEmail_returnEmptyMessage() {
        String email = "jane@@doe.com";
        Locale locale = Locale.ENGLISH;
        String expectedMessage = "";
        Mockito.when(userRepositoryMock.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        Mockito.when(userValidationMock.validateEmailString(email)).thenReturn(List.of());

        String errorMessage = forgotPasswordService.validateEmail(email, locale);

        Mockito.verify(eventPublisherMock, times(0)).publishEvent(Mockito.any());
        assertEquals(expectedMessage, errorMessage);
    }

    @Test
    void validateEmail_enterNotExistsEmail_returnEmptyMessage() {
        String email = "john@doe.com";
        Locale locale = Locale.ENGLISH;
        String expectedMessage = "";
        Mockito.when(userRepositoryMock.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        Mockito.when(userValidationMock.validateEmailString(email)).thenReturn(List.of());

        String errorMessage = forgotPasswordService.validateEmail(email, locale);

        Mockito.verify(eventPublisherMock, times(0)).publishEvent(Mockito.any());
        assertEquals(expectedMessage, errorMessage);
    }

    @Test
    void validateEmail_enterInvalidEmail_returnErrorMessage() {
        String email = "jane@@doe.com";
        Locale locale = Locale.ENGLISH;
        String expectedMessage = "Email address must be in the form ‘jane@doe.nz’.";
        Mockito.when(userRepositoryMock.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        Mockito.when(userValidationMock.validateEmailString(email)).thenReturn(List.of(expectedMessage));

        String errorMessage = forgotPasswordService.validateEmail(email, locale);

        Mockito.verify(eventPublisherMock, times(0)).publishEvent(Mockito.any());
        assertEquals(expectedMessage, errorMessage);
    }

    @Test
    void sendNewPasswordEmail_enterDetails_createEmailWithService() {
        String email = "jane@doe.com";
        String firstName = "Jane";
        Locale locale = Locale.ENGLISH;
        String subject = "Password Updated";
        String pathToEmailHtml = "html/email-confirm-new-password";
        String onFailureMessage = "Send confirmation of password change email failed";

        forgotPasswordService.sendNewPasswordEmail(email, firstName, locale);

        Mockito.verify(emailServiceMock, times(1))
                .createEmail(Mockito.eq(email), Mockito.eq(subject), Mockito.eq(pathToEmailHtml),
                        Mockito.any(Context.class), Mockito.eq(onFailureMessage));
    }

    @Test
    void updatePasswords_enterUserAndPassword_updateUserPassword() {
        String newPassword = "Test123!";
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        forgotPasswordService.updatePassword(user, newPassword);

        Mockito.verify(userRepositoryMock, times(1)).save(user);
        assertTrue(passwordEncoder.matches(newPassword, user.getPassword()));
    }
}
