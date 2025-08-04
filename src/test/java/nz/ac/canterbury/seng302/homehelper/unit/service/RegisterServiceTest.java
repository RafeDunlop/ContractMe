package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.RegisterService;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegisterServiceTest {

    @Test
    public void testValidateEmail_emailNotUsed_returnEmptyList() {
        UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
        UserValidation userValidationMock = Mockito.mock(UserValidation.class);
        LocationService locationServiceMock = Mockito.mock(LocationService.class);
        RegisterService registerService = new RegisterService(userRepositoryMock, userValidationMock, locationServiceMock);
        Mockito.when(userRepositoryMock.findByEmailIgnoreCase(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(userValidationMock.validateEmailString(Mockito.anyString())).thenReturn( new ArrayList<>());
        List<String> returnValue = registerService.validateEmail("jane@doe.nz");
        Assertions.assertEquals(0, returnValue.size());
        Assertions.assertTrue(returnValue.isEmpty());
    }

    @Test
    public void testValidateEmail_emailUsed_returnError() {
        UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
        UserValidation userValidationMock = Mockito.mock(UserValidation.class);
        LocationService locationServiceMock = Mockito.mock(LocationService.class);
        RegisterService registerService = new RegisterService(userRepositoryMock, userValidationMock, locationServiceMock);
        Mockito.when(userRepositoryMock.findByEmailIgnoreCase(Mockito.anyString())).thenReturn(Optional.of(new User("Jane", "Doe", "jane@doe.nz", "password")));
        Mockito.when(userValidationMock.validateEmailString(Mockito.anyString())).thenReturn( new ArrayList<>());
        List<String> returnValue = registerService.validateEmail("jane@doe.nz");
        List<String> expectedErrorList = List.of("This email address is already in use.");
        Assertions.assertEquals(1, returnValue.size());
        Assertions.assertLinesMatch(expectedErrorList, returnValue);
    }
}
