package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class LoginServiceTest {


    @Test
    public void testGetUserByEmail_correctlyRetrievesRepoQuery() {
        UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
        UserValidation userValidation = Mockito.mock(UserValidation.class);
        LoginService loginService = new LoginService(userRepositoryMock, userValidation);

        Authentication authenticationMock = Mockito.mock(Authentication.class);
        SecurityContext securityContextMock = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContextMock);

        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        Mockito.when(authenticationMock.getName()).thenReturn("john.smith@example.com");
        Mockito.when(userRepositoryMock.findByEmailIgnoreCase("john.smith@example.com")).thenReturn(Optional.of(new User("John", "Smith", "john.smith@example.com", "password")));


        User returnedUser = loginService.getUserByEmail();
        Assertions.assertEquals("john.smith@example.com", returnedUser.getEmail());

    }


    @Test
    public void testGetUserByEmail_invalidEmail_throwsException() {
        UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
        UserValidation userValidation = Mockito.mock(UserValidation.class);
        LoginService loginService = new LoginService(userRepositoryMock, userValidation);

        Authentication authenticationMock = Mockito.mock(Authentication.class);
        SecurityContext securityContextMock = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContextMock);

        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        Mockito.when(authenticationMock.getName()).thenReturn("eftbhjnipas");
        Mockito.when(userRepositoryMock.findByEmailIgnoreCase("john.smith@example.com")).thenReturn(Optional.of(new User("John", "Smith", "john.smith@example.com", "password")));

        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, loginService::getUserByEmail);
        Assertions.assertEquals("The user email is invalid", exception.getMessage());
    }



}
