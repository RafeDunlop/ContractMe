package nz.ac.canterbury.seng302.homehelper.security;

import org.springframework.security.core.AuthenticationException;

public class AccountNotActivatedException extends AuthenticationException {
    public AccountNotActivatedException(String msg) {
        super(msg);
    }
}
