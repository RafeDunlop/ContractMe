package nz.ac.canterbury.seng302.homehelper.unit.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.controller.RegisterController;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.RegisterService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class RegisterControllerTest {


    /**
     * Tests if, given the user successfully registers, they are re-routed
     * to the user details page
     * */
    @Test
    void testValidPathway_FromRegistrationPage_ToUserProfilePage() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        RegisterService registerServiceSpy = Mockito.mock(RegisterService.class);
        RegisterController registerController = new RegisterController(registerServiceSpy);

        Model model = Mockito.mock(Model.class);
        UserRegisterDTO mockedUser = new UserRegisterDTO("","","","","");

        User trialUser = Mockito.spy(new User("test", "test", "test", "test"));
        Mockito.when(registerServiceSpy.registerUser(mockedUser)).thenReturn((trialUser));

        Mockito.when(trialUser.getId()).thenReturn(1L);
        String viewName = (registerController.submitRegistration(mockedUser, model, request));

        Assertions.assertEquals("redirect:/user", viewName);

    }















}
