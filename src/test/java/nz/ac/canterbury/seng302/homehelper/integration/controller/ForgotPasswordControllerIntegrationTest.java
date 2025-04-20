package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.security.GenerationStrategy;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ForgotPasswordControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private VerificationCodeService verificationCodeService;

    private static PasswordEncoder passwordEncoder;

    private User resetUser;

    @BeforeAll
    static void passwordEncoderSetup() {
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @BeforeEach
    void userSetup() {
        resetUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(resetUser);
    }

    @AfterEach
    void repositoryCleanup() {
        verificationCodeRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Tests the getter for the forgot password form. This getter will be used when
     * the user pressed the forgot password button on the login page.
     * Expects the form to be returned and opened.
     * @throws Exception if the request processing fails
     */
    @Test
    void getForgotPassword_goToForm_returnForgotPasswordForm() throws Exception {
        mockMvc.perform(get("/password/forgot"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgotPasswordTemplate"));
    }

    /**
     * Tests the post function on the forgot password page with a valid email. When an
     * email connected to an existing account is posted, a token is created and an email
     * that has a link with the same token is sent to the email.
     * @throws Exception if the request processing fails
     */
    @Test
    void postForgotPassword_enterValidEmail_returnSentMessageAndForm() throws Exception {
        String email = resetUser.getEmail();
        String expectedMessage = "An email was sent to the address if it was recognised";
        mockMvc.perform(post("/password/forgot")
                .param("email", email)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/password/forgot"))
                .andExpect(flash().attribute("emailMessage", expectedMessage));

        List<VerificationCode> verificationCode = (List<VerificationCode>) verificationCodeRepository.findAll();
        assertEquals(resetUser, verificationCode.get(0).getUser());
    }

    /**
     * Tests the post function on the forgot password page with an unregistered email. When a
     * valid email not connected to an existing account is posted, a message shows up with
     * "An email was sent to the address if it was recognised" but no token or email is created.
     * @throws Exception if the request processing fails
     */
    @Test
    void postForgotPassword_enterUnknownEmail_returnSentMessageAndForm() throws Exception {
        String email = "john@doe.com";
        String expectedMessage = "An email was sent to the address if it was recognised";
        mockMvc.perform(post("/password/forgot")
                        .param("email", email)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/password/forgot"))
                .andExpect(flash().attribute("emailMessage", expectedMessage));

        List<VerificationCode> verificationCode = (List<VerificationCode>) verificationCodeRepository.findAll();
        assertTrue(verificationCode.isEmpty());
    }

    /**
     * Tests the post function on the forgot password page with an invalid email. When an
     * invalid email is posted, an error message shows up to tell the user what format the
     * email should be in.
     * @throws Exception if the request processing fails
     */
    @Test
    void postForgotPassword_enterInvalidEmail_returnErrorMessageAndForm() throws Exception {
        String email = "jane@@doe.com";
        String expectedMessage = "Email address must be in the form ‘jane@doe.nz’.";
        mockMvc.perform(post("/password/forgot")
                        .param("email", email)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/password/forgot"))
                .andExpect(flash().attribute("emailError", expectedMessage));
    }

    /**
     * Tests the getter for the on the reset password page with a valid token. When a link
     * to the page is inputted with a token created by the forgot password form, the user
     * is taken to the reset password form.
     * @throws Exception if the request processing fails
     */
    @Test
    void getResetPassword_enterValidToken_returnResetPasswordForm() throws Exception {
        String token = verificationCodeService.issueVerificationCode(GenerationStrategy.RESET_TOKEN,
                resetUser, Locale.ENGLISH);

        mockMvc.perform(get("/password/reset/" + token))
                .andExpect(status().isOk())
                .andExpect(view().name("resetPasswordTemplate"))
                .andExpect(model().attribute("token", token));
    }

    /**
     * Tests the getter for the on the reset password page with an invalid token. When a
     * link to the page is inputted with a token that doesn't exist in the token repository,
     * the user is redirected to the login page with a message.
     * @throws Exception if the request processing fails
     */
    @Test
    void getResetPassword_enterInvalidToken_returnRedirectLoginPage() throws Exception{
        String token = "InVaLiDtOkEn";

        mockMvc.perform(get("/password/reset/" + token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl( "/login?error=Reset+password+link+has+expired"));
    }

    /**
     * Tests the getter for the on the reset password page with an expired token. When a
     * link to the page is inputted with a token that had existed but is now expired, the
     * user is redirected to the login page with a message.
     * @throws Exception if the request processing fails
     */
    @Test
    void getResetPassword_enterExpiredToken_returnRedirectLoginPage() throws Exception {
        verificationCodeService.setDelay(100, TimeUnit.MILLISECONDS);
        String token = verificationCodeService.issueVerificationCode(GenerationStrategy.RESET_TOKEN,
                resetUser, Locale.ENGLISH);

        Optional<VerificationCode> verificationCode = verificationCodeRepository.findByCode(token);
        assertTrue(verificationCode.isPresent());

        verificationCodeService.getScheduledFutureDeletion(token).get();

        verificationCode = verificationCodeRepository.findByCode(token);
        assertFalse(verificationCode.isPresent());

        mockMvc.perform(get("/password/reset/" + token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl( "/login?error=Reset+password+link+has+expired"));

        verificationCodeService.setDelay(10, TimeUnit.MINUTES);
    }

    /**
     * Tests the post function on the reset password page with valid password inputs.
     * When a valid password is retyped and both are posted, the user is redirected to
     * the login page, the user's password is reset, and the token is consumed.
     * @throws Exception if the request processing fails
     */
    @Test
    void postResetPassword_enterValidPasswords_returnRedirectLoginPage() throws Exception {
        String token = verificationCodeService.issueVerificationCode(GenerationStrategy.RESET_TOKEN,
                resetUser, Locale.ENGLISH);
        String newPassword = "Test123!";
        String retypePassword = "Test123!";

        mockMvc.perform(post("/password/reset/" + token)
                        .param("newPassword", newPassword)
                        .param("retypePassword", retypePassword)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        Optional<VerificationCode> verificationCode = verificationCodeRepository.findByCode(token);
        assertFalse(verificationCode.isPresent());

        User updatedUser = userRepository.findByEmailIgnoreCase(resetUser.getEmail()).orElse(null);
        assertNotNull(updatedUser);
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    /**
     * Tests the post function on the reset password page with different new password and
     * retyped password inputs. When the new and retyped passwords are different, the
     * user stays on the forgot password page with an error message.
     * @throws Exception if the request processing fails
     */
    @Test
    void postResetPassword_enterDifferentPasswords_returnErrorMessageAndForm() throws Exception {
        String token = verificationCodeService.issueVerificationCode(GenerationStrategy.RESET_TOKEN,
                resetUser, Locale.ENGLISH);
        String newPassword = "Test123!";
        String retypePassword = "Test12345?";
        List<String> expectedErrorMessages = List.of("The passwords do not match.");

        mockMvc.perform(post("/password/reset/" + token)
                        .param("newPassword", newPassword)
                        .param("retypePassword", retypePassword)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessages", expectedErrorMessages))
                .andExpect(flash().attribute("token", token));
    }

    /**
     * Tests the post function on the reset password page when the token is expired.
     * When a post request is made on the reset password page with a token that had
     * existed but is now expired, the user is redirected to the login page with a message.
     * @throws Exception if the request processing fails
     */
    @Test
    void postResetPassword_enterExpiredToken_returnRedirectLoginPage() throws Exception {
        verificationCodeService.setDelay(100, TimeUnit.MILLISECONDS);
        String token = verificationCodeService.issueVerificationCode(GenerationStrategy.RESET_TOKEN,
                resetUser, Locale.ENGLISH);
        String newPassword = "Test123!";
        String retypePassword = "Test123!";

        verificationCodeService.getScheduledFutureDeletion(token).get();

        mockMvc.perform(post("/password/reset/" + token)
                        .param("newPassword", newPassword)
                        .param("retypePassword", retypePassword)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=Reset+password+link+has+expired"));
    }
}
