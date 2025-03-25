package nz.ac.canterbury.seng302.homehelper.event;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

/**
 * A class to store registration complete event information.
 * Based on <a href=https://www.baeldung.com/registration-verify-user-by-email>this tutorial</a>, currently only
 * stores the user and locale.
 *
 * @author Sean
 * @author Baeldung (tutorial)
 */
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private final Locale locale;
    private final User user;

    /**
     * An event created when the user registration is complete, after the user has been saved to the database
     * (with the enabled field set to false).
     *
     * @param user the user who registered
     * @param locale the user's locale
     */
    public OnRegistrationCompleteEvent(User user, Locale locale) {
        super(user);
        this.user = user;
        this.locale = locale;
    }

    /**
     * Get the user associated with the event.
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Get the user's locale from the event. This is used for setting locale
     * information for the email message sent when the user registers.
     *
     * @return the user's locale
     */
    public Locale getLocale() {
        return locale;
    }
}
