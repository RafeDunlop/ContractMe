package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class verificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long id;

    @Column(nullable = false, updatable = false)
    private byte[] code;

    @Column(nullable = false, updatable = false)
    private LocalDateTime expiryTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    User user;

    public verificationCode(byte[] code, User user, LocalDateTime expiryTime) {
        this.user = user;
        this.code = code;
        this.expiryTime = expiryTime;
    }

    /**
     * JPA required no-args constructor
     */
    public verificationCode() {}

    public long getId() {
        return id;
    }

    public byte[] getCode() {
        return code;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public User getUser() {
        return user;
    }
}
