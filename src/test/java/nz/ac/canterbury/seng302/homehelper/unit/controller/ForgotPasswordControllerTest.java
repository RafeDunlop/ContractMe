package nz.ac.canterbury.seng302.homehelper.unit.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.controller.ForgotPasswordController;
import nz.ac.canterbury.seng302.homehelper.service.ForgotPasswordService;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ForgotPasswordControllerTest {

    @Mock
    private ForgotPasswordService forgotPasswordServiceMock;

    @Mock
    private VerificationCodeService verificationCodeServiceMock;

    @InjectMocks
    private ForgotPasswordController forgotPasswordController;

    @Mock
    private Model modelMock;

    @Mock
    private HttpServletRequest httpServletRequestMock;

    @Mock
    private RedirectAttributes redirectAttributesMock;


    @Test
    void getForgotPassword_goToForm_returnForgotPasswordForm() {
        String expectedForm = "forgotPasswordTemplate";
        String forgotPasswordForm = forgotPasswordController.forgotPassword();
        assertEquals(expectedForm, forgotPasswordForm);
    }

    @Test
    void postForgotPassword_enterValidEmail_returnSentMessageAndForm() {
        String expectedForm = "forgotPasswordTemplate";
        String expectedMessage = "An email was sent to the address if it was recognised";
        String validEmail = "jane@doe.com";
        Mockito.when(forgotPasswordServiceMock.validateEmail(validEmail, Mockito.any())).thenReturn("");

        String forgotPasswordForm = forgotPasswordController.submitEmail(validEmail, modelMock, httpServletRequestMock);

        assertEquals(expectedForm, forgotPasswordForm);
        Mockito.verify(modelMock).addAttribute("emailMessage", expectedMessage);
    }
}
