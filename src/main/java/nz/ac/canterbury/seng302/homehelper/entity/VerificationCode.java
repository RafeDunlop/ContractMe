package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Verification token entity, based on the example given <a href="https://www.baeldung.com/registration-verify-user-by-email">here</a>
 *
 * @author Sean
 * @author Baeldung (tutorial)
 */
@Entity
public class VerificationCode {
    private static final int EXPIRATION = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long id;

    @Column(nullable = false, updatable = false)
    private String code;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Date expiryDate;

    /**
     * Creates a VerificationCode instance and sets the expiry date.
     *
     * @param user the user to associate with the token
     * @param token the code string
     */
    public VerificationCode(User user, String token) {
        this.user = user;
        this.code = token;
        expiryDate = calculateExpiryDate();
    }

    /**
     * JPA required no-args constructor
     */
    public VerificationCode() {}

    public long getId() {
        return id;
    }

    /**
     * Calculate the expiry date based on the current time and the constant expiry time in minutes.
     *
     * @return the specific calendar Date timestamp when the token will expire and the user will be deleted
     */
    private Date calculateExpiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.MINUTE, VerificationCode.EXPIRATION);
        return new Date(calendar.getTime().getTime());
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public User getUser() {
        return user;
    }

    public String getCode() {
        return code;
    }
}
