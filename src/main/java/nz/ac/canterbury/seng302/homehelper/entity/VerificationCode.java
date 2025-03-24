package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Verification token entity, based on the example given <a href="https://www.baeldung.com/registration-verify-user-by-email">here</a>
 *
 * @author Sean
 * @author Baeldung (tutorial)
 */
@Entity
public class VerificationCode {
    /**
     * Expiration time for verification codes in <em>minutes</em>.
     */
    private static final int EXPIRATION = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long id;

    @Column(nullable = false, updatable = false)
    private String code;

    @Column(nullable = false, updatable = false)
    private Locale locale;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Date expiryDate;

    /**
     * Creates a VerificationCode instance and sets the expiry date.
     *
     * @param user the user to associate with the code
     * @param code the code string
     */
    public VerificationCode(User user, String code, Locale locale) {
        this.user = user;
        this.code = code;
        this.locale = locale;
        expiryDate = calculateExpiryDate(locale);
    }

    /**
     * JPA required no-args constructor
     */
    public VerificationCode() {}

    /**
     * Get the unique id of the verification code.
     * For database purposes.
     *
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * Calculate the expiry date based on the current time and the constant expiry time in minutes.
     *
     * @return the specific calendar Date timestamp when the token will expire and the user will be deleted
     */
    private Date calculateExpiryDate(Locale locale) {
        Calendar calendar = Calendar.getInstance(locale);
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.MINUTE, VerificationCode.EXPIRATION);
        return new Date(calendar.getTime().getTime());
    }

    /**
     * Checks if the current date time is after the expiry date.
     *
     * @return true if the code has expired
     */
    public boolean isExpired() {
        return Calendar.getInstance(locale).after(expiryDate);
    }

    /**
     * Gets the user this code is associated with.
     *
     * @return user
     */
    public User getUser() {
        return user;
    }

    /**
     * Get the actual code string.
     *
     * @return code
     */
    public String getCode() {
        return code;
    }
}
