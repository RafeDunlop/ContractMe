package nz.ac.canterbury.seng302.homehelper.event;

import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

/**
 * A class to store reset password complete event information.
 */
public class OnResetPasswordSubmittedEvent extends ApplicationEvent {

    private final Locale locale;

    private final User user;

    /**
     * An event created when a user enters an email associated with an account on the forgot password form.
     *
     * @param user The user to have their password reset
     * @param locale The user's locale
     */
    public OnResetPasswordSubmittedEvent(User user, Locale locale) {
        super(user);
        this.user = user;
        this.locale = locale;
    }

    /**
     * Get the user associated with the event.
     * @return The user
     */
    public User getUser() {
        return user;
    }

    /**
     * Get the user's locale from the event. This is used for setting locale
     * information for the email message sent when the user registers.
     *
     * @return The user's locale
     */
    public Locale getLocale() {
        return locale;
    }
}
