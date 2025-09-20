package nz.ac.canterbury.seng302.homehelper.e2e;

import io.cucumber.java.Before;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
public class SetupRegisterUserHookE2e {

    @Autowired
    private UserRepository userRepository;

    private final UserContext userContext;

    @Autowired
    private LocationService locationService;

    public SetupRegisterUserHookE2e(UserContext userContext) {
        this.userContext = userContext;
    }

    @Before("@authoriseUser")
    public void i_am_an_existing_user() {

        doNothing().when(locationService).injectCoordsViaGeocoding(any(AddressDTO.class));

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String uniqueEmail = "test" + System.currentTimeMillis() + "@user.nz";
        User user = new User("Test", "User", uniqueEmail, encoder.encode("Test123!"));
        Location location = new Location(
                "20 Kirkwood Avenue",
                "New Zealand",
                "8041",
                "Christchuch",
                "Upper Riccarton",
                1d,
                1d);
        user.setLocation(location);
        user.activate();
        userRepository.save(user);
        userContext.setUser(user);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Before(value = "@loginUser", order = 2)
    public void i_login() {
        i_am_an_existing_user();
        User user = userContext.getUser();

        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/login");
        RunPlaywrightTests.page.locator("#username").fill(user.getEmail());
        RunPlaywrightTests.page.locator("#password").fill("Test123!");

        RunPlaywrightTests.page.locator("#sign-in-button").click();
        RunPlaywrightTests.page.waitForURL("**/main");
    }
}
