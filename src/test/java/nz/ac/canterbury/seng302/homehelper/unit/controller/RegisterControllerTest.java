package nz.ac.canterbury.seng302.homehelper.unit.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.controller.RegisterController;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.ContractorService;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.RegisterService;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class RegisterControllerTest {
    private HttpServletRequest request;
    private RegisterController registerController;
    private RegisterService registerServiceSpy;
    private ContractorService contractorServiceMock;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    public void setup() {
        request = Mockito.mock(HttpServletRequest.class);
        registerServiceSpy = Mockito.mock(RegisterService.class);
        VerificationCodeService verificationCodeServiceMock = Mockito.mock(VerificationCodeService.class);
        ApplicationEventPublisher applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        contractorServiceMock = Mockito.mock(ContractorService.class);
        LocationService locationServiceMock = Mockito.mock(LocationService.class);
        redirectAttributes = Mockito.mock(RedirectAttributes.class);
        registerController = new RegisterController(
                registerServiceSpy, applicationEventPublisher, verificationCodeServiceMock, locationServiceMock, contractorServiceMock);
    }

    @Test
    public void testValidPathway_FromRegistrationPage_ToConfirmRegistrationPage() {
        UserRegisterDTO mockedUser = new UserRegisterDTO("","","","","");
        AddressDTO mockedLocation = new AddressDTO();

        User trialUser = Mockito.spy(new User("test", "test", "test", "test"));
        Mockito.when(registerServiceSpy.registerUser(mockedUser)).thenReturn((trialUser));

        Mockito.when(trialUser.getId()).thenReturn(1L);
        String viewName = (registerController.submitRegistration(mockedUser, mockedLocation, request, redirectAttributes));

        Assertions.assertEquals("redirect:/confirm-registration", viewName);
    }

    @Test
    public void submitRegistration_contractorToggled_registersAsContractor() {
        UserRegisterDTO mockedUser = new UserRegisterDTO("","","","","");
        mockedUser.setIsContractor(true);
        mockedUser.setHourlyRate(27.80f);
        mockedUser.setPhoneNumber("12345678");
        AddressDTO mockedLocation = new AddressDTO();

        Contractor contractor = Mockito.mock(Contractor.class);
        Mockito.when(contractorServiceMock.registerContractor(mockedUser, mockedLocation)).thenReturn(contractor);
        String viewName = registerController.submitRegistration(mockedUser, mockedLocation, request, redirectAttributes);
        Assertions.assertEquals("redirect:/confirm-registration", viewName);
        Mockito.verify(contractorServiceMock).registerContractor(mockedUser, mockedLocation);
        Mockito.verify(registerServiceSpy, Mockito.never()).registerUser(mockedUser);
    }

    @Test
    public void submitRegistration_contractorNotToggled_registersAsUser() {
        UserRegisterDTO mockedUser = new UserRegisterDTO("","","","","");
        mockedUser.setIsContractor(false);

        AddressDTO mockedLocation = new AddressDTO();
        User user = Mockito.mock(User.class);
        Mockito.when(registerServiceSpy.registerUser(mockedUser)).thenReturn(user);
        String viewName = registerController.submitRegistration(mockedUser, mockedLocation, request, redirectAttributes);
        Assertions.assertEquals("redirect:/confirm-registration", viewName);
        Mockito.verify(registerServiceSpy).registerUser(mockedUser);
        Mockito.verify(contractorServiceMock, Mockito.never()).registerContractor(mockedUser, mockedLocation);
    }

    @Test
    public void submitRegistration_contractorToggled_validatesContractor() {
        UserRegisterDTO mockedUser = new UserRegisterDTO("","","","","");
        mockedUser.setIsContractor(true);
        mockedUser.setHourlyRate(27.80f);
        mockedUser.setPhoneNumber("12345678");
        AddressDTO mockedLocation = new AddressDTO();

        Contractor contractor = Mockito.mock(Contractor.class);
        Mockito.when(contractorServiceMock.registerContractor(mockedUser, mockedLocation)).thenReturn(contractor);
        String viewName = registerController.submitRegistration(mockedUser, mockedLocation, request, redirectAttributes);
        Assertions.assertEquals("redirect:/confirm-registration", viewName);
        Mockito.verify(contractorServiceMock).validateContractor(mockedUser, true);
    }















}
