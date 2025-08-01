package nz.ac.canterbury.seng302.homehelper.e2e;

import io.cucumber.java.Before;
import nz.ac.canterbury.seng302.homehelper.e2e.context.E2eUserContext;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
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
public class SetupRegisterUserHookE2e {

    @Autowired
    private UserRepository userRepository;

    private final E2eUserContext userContext;

    public SetupRegisterUserHookE2e(E2eUserContext userContext) {
        this.userContext = userContext;
    }

    @Before("@authoriseUser")
    public void i_am_an_existing_user() {

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String uniqueEmail = "test" + System.currentTimeMillis() + "@user.nz";
        User user = new User("Test", "User", uniqueEmail, encoder.encode("Test123!"));
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
}
