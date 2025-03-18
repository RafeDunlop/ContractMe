package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.service.SignupCodeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
public class SignupCode {

    private static final SecureRandom random = new SecureRandom();

    @Id
    private long code;

    @Column
    private Timestamp issueTime;

    public SignupCode() {

    }
}
