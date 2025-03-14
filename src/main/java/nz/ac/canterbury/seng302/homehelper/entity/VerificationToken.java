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
public class VerificationToken {
    private static final int EXPIRATION = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Date expiryDate;

    /**
     * Calculate the expiry date based on the current time and the constant expiry time in minutes.
     *
     * @return the specific calendar Date timestamp when the token will expire and the user will be deleted
     */
    private Date calculateExpiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.MINUTE, VerificationToken.EXPIRATION);
        return new Date(calendar.getTime().getTime());
    }

    public VerificationToken() {
        expiryDate = calculateExpiryDate();
    }

    public Date getExpiryDate() {
        return expiryDate;
    }
}
