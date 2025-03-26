package nz.ac.canterbury.seng302.homehelper.security;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

/**
 * Custom Authentication Provider class, based on the one given in the spring security handout. Code and some Javadoc
 * made by the SENG302 teaching team.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private LoginService loginService;

    public CustomAuthenticationProvider() {
        super();
    }


    /**
     * Custom authentication implementation
     *
     * @param authentication An implementation object that must have non-empty email (username) and password
     * @return A new {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken} if email
     * and password are valid with user's authorities
     * @throws AuthenticationException if an exception occurs
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            User user = loginService.getUserByEmailAndPassword(email, password);
            if (!user.isActivated()) {
                throw new AccountNotActivatedException("Account not activated.") {
                };
            }
            return new UsernamePasswordAuthenticationToken(user.getEmail(), null, user.getAuthorities());
        } catch (IllegalArgumentException | NoSuchElementException e) {
            throw new BadCredentialsException(e.getMessage());
        } catch (AccountNotActivatedException e) {
            throw e;
        }
    }

    /**
     * Returns true if this AuthenticationProvider supports the given authentication type.
     *
     * @param authentication the class of the authentication object
     * @return true if this auth provider supports the class
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
