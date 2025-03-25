package nz.ac.canterbury.seng302.homehelper.event;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;


public class OnResetPasswordSubmittedEvent extends ApplicationEvent {
    private final Locale locale;
    private final User user;

    public OnResetPasswordSubmittedEvent(User user, Locale locale) {
        super(user);
        this.user = user;
        this.locale = locale;
    }

    public User getUser() {
        return user;
    }

    public Locale getLocale() {
        return locale;
    }
}
