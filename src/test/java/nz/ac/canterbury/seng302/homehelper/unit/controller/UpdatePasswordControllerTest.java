package nz.ac.canterbury.seng302.homehelper.unit.controller;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.controller.UpdatePasswordController;
import nz.ac.canterbury.seng302.homehelper.dto.UpdatePasswordDTO;
import nz.ac.canterbury.seng302.homehelper.service.UpdatePasswordService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


public class UpdatePasswordControllerTest {

    /**
     * Tests if, given the user successfully updates password, they are re-routed
     * to the user details page
     * */
    @Test
    void testValidPathway_FromUpdatePasswordPage_ToUserProfilePage() {
        UpdatePasswordService updatePasswordServiceSpy = Mockito.mock(UpdatePasswordService.class);
        UpdatePasswordController updatePasswordController = new UpdatePasswordController(updatePasswordServiceSpy);

        BindingResult bindingResultMock = Mockito.mock(BindingResult.class);
        Model modelMock = Mockito.mock(Model.class);
        RedirectAttributes redirectAttributesMock = Mockito.mock(RedirectAttributes.class);

        UpdatePasswordDTO validPasswordDTO = new UpdatePasswordDTO("old","new","new");

        doNothing().when(updatePasswordServiceSpy).updatePassword(validPasswordDTO);

        String result = updatePasswordController.tryChangePassword(validPasswordDTO, bindingResultMock, modelMock, redirectAttributesMock);

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
        UpdatePasswordController updatePasswordController = new UpdatePasswordController(updatePasswordServiceMock);

        BindingResult bindingResultMock = Mockito.mock(BindingResult.class);
        Model modelMock = Mockito.mock(Model.class);
        RedirectAttributes redirectAttributesMock = Mockito.mock(RedirectAttributes.class);


        UpdatePasswordDTO invalidPasswordDTO = new UpdatePasswordDTO("old", "new", "retype");


        doThrow(new IllegalArgumentException("Your old password is incorrect.")).when(updatePasswordServiceMock).updatePassword(invalidPasswordDTO);


        String result = updatePasswordController.tryChangePassword(invalidPasswordDTO, bindingResultMock, modelMock, redirectAttributesMock);


        assertEquals("updatePasswordTemplate", result);
    }

}
