package nz.ac.canterbury.seng302.homehelper.unit.controller;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.controller.ChangePasswordController;
import nz.ac.canterbury.seng302.homehelper.controller.RegisterController;
import nz.ac.canterbury.seng302.homehelper.dto.UpdatePasswordDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.UpdatePasswordService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


public class ChangePasswordControllerTest {

    /**
     * Tests if, given the user successfully updates password, they are re-routed
     * to the user details page
     * */
    @Test
    void testValidPathway_FromUpdatePasswordPage_ToUserProfilePage() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        UpdatePasswordService updatePasswordServiceSpy = Mockito.mock(UpdatePasswordService.class);
        ChangePasswordController changePasswordController = new ChangePasswordController(updatePasswordServiceSpy);

        BindingResult bindingResultMock = Mockito.mock(BindingResult.class);
        Model modelMock = Mockito.mock(Model.class);
        RedirectAttributes redirectAttributesMock = Mockito.mock(RedirectAttributes.class);

        UpdatePasswordDTO validPasswordDTO = new UpdatePasswordDTO("old","new","new");

        doNothing().when(updatePasswordServiceSpy).updatePassword(validPasswordDTO);

        String result = changePasswordController.tryChangePassword(validPasswordDTO, bindingResultMock, modelMock, redirectAttributesMock);

        assertEquals("redirect:/user", result);

        verify(updatePasswordServiceSpy, times(1)).updatePassword(validPasswordDTO);

        verify(redirectAttributesMock, times(1)).addFlashAttribute("successMessage", "Password updated successfully.");

    }

    /**
     * Tests if, given the user doesn't pass the validation check updating password, they are re-routed
     * to the Update Password Page
     * */
    @Test
    void testValidationFailure_ThrowException_ReturnsToUpdatePasswordTemplate() {

        UpdatePasswordService updatePasswordServiceMock = Mockito.mock(UpdatePasswordService.class);
        ChangePasswordController changePasswordController = new ChangePasswordController(updatePasswordServiceMock);

        BindingResult bindingResultMock = Mockito.mock(BindingResult.class);
        Model modelMock = Mockito.mock(Model.class);
        RedirectAttributes redirectAttributesMock = Mockito.mock(RedirectAttributes.class);


        UpdatePasswordDTO invalidPasswordDTO = new UpdatePasswordDTO("old", "new", "retype");


        doThrow(new IllegalArgumentException("Old Password does not match.")).when(updatePasswordServiceMock).updatePassword(invalidPasswordDTO);


        String result = changePasswordController.tryChangePassword(invalidPasswordDTO, bindingResultMock, modelMock, redirectAttributesMock);


        assertEquals("updatePasswordTemplate", result);
    }

}
