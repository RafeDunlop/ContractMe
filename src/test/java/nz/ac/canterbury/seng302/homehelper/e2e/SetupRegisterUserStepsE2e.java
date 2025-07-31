package nz.ac.canterbury.seng302.homehelper.e2e;

import io.cucumber.java.Before;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootTest
public class SetupRegisterUserStepsE2e {

    @Autowired
    private UserRepository userRepository;

    private final UserContext userContext;

    public SetupRegisterUserStepsE2e(UserContext userContext) {
        this.userContext = userContext;
    }

    @Before("@registerUser")
    public void i_am_an_existing_user() {

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String uniqueEmail = "test" + System.currentTimeMillis() + "@user.nz";
        User user = new User("Test", "User", uniqueEmail, encoder.encode("Test123!"));
        Location location = new Location("20 Kirkwood Avenue", "New Zealand", "8041", "Christchuch", "Upper Riccarton");
        user.setLocation(location);
        user.activate();
        userRepository.save(user);
        userContext.setUser(user);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        RunPlaywrightTests.page.navigate(RunPlaywrightTests.baseUrl + "/login");
        RunPlaywrightTests.page.locator("#username").fill(uniqueEmail);
        RunPlaywrightTests.page.locator("#password").fill("Test123!");

        RunPlaywrightTests.page.locator("#sign-in-button").click();
        String homeUrl = RunPlaywrightTests.baseUrl + "/main";
        String currentUrl = RunPlaywrightTests.page.url();
        Assertions.assertEquals(homeUrl, currentUrl);
    }
}
