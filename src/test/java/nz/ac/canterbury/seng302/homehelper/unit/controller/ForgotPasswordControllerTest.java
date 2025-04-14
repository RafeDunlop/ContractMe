package nz.ac.canterbury.seng302.homehelper.unit.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.controller.ForgotPasswordController;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.ForgotPasswordService;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ForgotPasswordControllerTest {

    @Mock
    private ForgotPasswordService forgotPasswordServiceMock;

    @Mock
    private VerificationCodeService verificationCodeServiceMock;

    @InjectMocks
    private ForgotPasswordController forgotPasswordController;

    @Mock
    private RedirectAttributes redirectAttributesMock;

    @Mock
    private HttpServletRequest httpServletRequestMock;

    @Mock
    private Model modelMock;

    static User user;

    @BeforeAll
    static void createUser() {
        user = new User("Jane", "Doe", "jane@doe.com", "password");
    }

    /**
     * Tests the getter for the forgot password form. This getter will be used when
     * the user pressed the forgot password button on the login page.
     * Expects the form to be returned and opened.
     */
    @Test
    void getForgotPassword_goToForm_returnForgotPasswordForm() {
        String expectedForm = "forgotPasswordTemplate";
        String forgotPasswordForm = forgotPasswordController.forgotPassword();
        assertEquals(expectedForm, forgotPasswordForm);
    }

    /**
     * Tests the post function on the forgot password page with a valid email. When an
     * email connected to an existing account is posted, the user stays on the same
     * page and a message shows up.
     */
    @Test
    void postForgotPassword_enterValidEmail_returnSentMessageAndForm() {
        String expectedRedirect = "redirect:/password/forgot";
        String expectedMessage = "An email was sent to the address if it was recognised";
        String validEmail = "jane@doe.com";
        Mockito.when(httpServletRequestMock.getLocale()).thenReturn(Locale.ENGLISH);
        Mockito.when(forgotPasswordServiceMock.validateEmail(validEmail, Locale.ENGLISH)).thenReturn("");

        String forgotPasswordForm = forgotPasswordController.submitEmail(validEmail, httpServletRequestMock, redirectAttributesMock);

        assertEquals(expectedRedirect, forgotPasswordForm);
        Mockito.verify(redirectAttributesMock).addFlashAttribute("emailMessage", expectedMessage);
    }

    /**
     * Tests the post function on the forgot password page with an invalid email. When an
     * invalid email is posted, an error message shows up to tell the user what format the
     * email should be in.
     */
    @Test
    void postForgotPassword_enterInvalidEmail_returnErrorMessageAndForm() {
        String expectedRedirect = "redirect:/password/forgot";
        String expectedMessage = "Email address must be in the form ‘jane@doe.nz’.";
        String validEmail = "jane@@doe.com";
        Mockito.when(httpServletRequestMock.getLocale()).thenReturn(Locale.ENGLISH);
        Mockito.when(forgotPasswordServiceMock.validateEmail(validEmail, Locale.ENGLISH)).thenReturn(expectedMessage);

        String forgotPasswordForm = forgotPasswordController.submitEmail(validEmail, httpServletRequestMock, redirectAttributesMock);

        assertEquals(expectedRedirect, forgotPasswordForm);
        Mockito.verify(redirectAttributesMock).addFlashAttribute("errorMessage", expectedMessage);
    }

    /**
     * Tests the getter for the on the reset password page with a valid token. When a link
     * to the page is inputted with a token created by the forgot password form, the user
     * is taken to the reset password form.
     */
    @Test
    void getResetPassword_enterValidToken_returnResetPasswordForm() {
        String token = "VaLiDtOkEn";
        String expectedForm = "resetPasswordTemplate";
        Mockito.when(verificationCodeServiceMock.getUserByToken(token)).thenReturn(Optional.of(user));

        String resetPasswordForm = forgotPasswordController.resetPassword(token, redirectAttributesMock, modelMock);

        Mockito.verify(modelMock).addAttribute("token", token);
        assertEquals(expectedForm, resetPasswordForm);
    }

    /**
     * Tests the getter for the on the reset password page with an invalid token. When a
     * link to the page is inputted with a token that doesn't exist in the token repository,
     * the user is redirected to the login page with a message.
     */
    @Test
    void getResetPassword_enterInvalidToken_returnRedirectLoginPage() {
        String token = "InVaLiDtOkEn";
        String expectedRedirect = "redirect:/login";
        String expectedRedirectMessage = "Reset password link has expired";
        Mockito.when(verificationCodeServiceMock.getUserByToken(token)).thenReturn(Optional.empty());

        String loginPageRedirect = forgotPasswordController.resetPassword(token, redirectAttributesMock, modelMock);

        Mockito.verify(redirectAttributesMock).addAttribute("error", expectedRedirectMessage);
        assertEquals(expectedRedirect, loginPageRedirect);
    }

    /**
     * Tests the post function on the reset password page with valid password inputs.
     * When a valid password is retyped and both are posted, the user is redirected to
     * the login page, the user's password is reset, and the token is consumed.
     */
    @Test
    void postResetPassword_enterValidPasswords_returnRedirectLoginPage() {
        String token = "VaLiDtOkEn";
        String newPassword = "Test123!";
        String retypePassword = "Test123!";
        String expectedRedirect = "redirect:/login";
        Mockito.when(verificationCodeServiceMock.getUserByToken(token)).thenReturn(Optional.of(user));
        Mockito.when(forgotPasswordServiceMock.validatePasswords(newPassword, retypePassword)).thenReturn(List.of());
        Mockito.when(httpServletRequestMock.getLocale()).thenReturn(Locale.ENGLISH);

        String loginPageRedirect = forgotPasswordController.submitPassword(token, newPassword, retypePassword, redirectAttributesMock, httpServletRequestMock);

        Mockito.verify(verificationCodeServiceMock, times(1)).consumeResetPasswordToken(token);
        Mockito.verify(forgotPasswordServiceMock, times(1)).sendNewPasswordEmail(user.getEmail(), user.getFirstName(), httpServletRequestMock.getLocale());
        Mockito.verify(forgotPasswordServiceMock, times(1)).updatePassword(user, newPassword);
        assertEquals(expectedRedirect, loginPageRedirect);
    }

    /**
     * Tests the post function on the reset password page with different new password and
     * retyped password inputs. When the new and retyped passwords are different, the
     * user stays on the forgot password page with an error message.
     */
    @Test
    void postResetPassword_enterDifferentPasswords_returnErrorMessageAndForm() {
        String token = "VaLiDtOkEn";
        String newPassword = "Test123!";
        String retypePassword = "Test12345?";
        String expectedRedirect = "redirect:/password/reset/VaLiDtOkEn";
        List<String> expectedErrorMessages = List.of("The passwords do not match.");
        Mockito.when(verificationCodeServiceMock.getUserByToken(token)).thenReturn(Optional.of(user));
        Mockito.when(forgotPasswordServiceMock.validatePasswords(newPassword, retypePassword)).thenReturn(expectedErrorMessages);

        String resetPasswordForm = forgotPasswordController.submitPassword(token, newPassword, retypePassword, redirectAttributesMock, httpServletRequestMock);

        Mockito.verify(redirectAttributesMock).addFlashAttribute("errorMessages", expectedErrorMessages);
        Mockito.verify(redirectAttributesMock).addFlashAttribute("token", token);
        assertEquals(expectedRedirect, resetPasswordForm);
    }

    /**
     * Tests the post function on the reset password page with an invalid token.
     * When a post request is made on the reset password page with a token that doesn't
     * exist, the user is redirected to the login page with a message.
     */
    @Test
    void postResetPassword_enterInvalidToken_returnRedirectLoginPage() {
        String token = "VaLiDtOkEn";
        String newPassword = "Test123!";
        String retypePassword = "Test12345?";
        String expectedRedirect = "redirect:/login";
        String expectedRedirectMessage = "Reset password link has expired";
        Mockito.when(verificationCodeServiceMock.getUserByToken(token)).thenReturn(Optional.empty());

        String resetPasswordForm = forgotPasswordController.submitPassword(token, newPassword, retypePassword, redirectAttributesMock, httpServletRequestMock);

        Mockito.verify(redirectAttributesMock).addAttribute("error", expectedRedirectMessage);
        assertEquals(expectedRedirect, resetPasswordForm);
    }
}
