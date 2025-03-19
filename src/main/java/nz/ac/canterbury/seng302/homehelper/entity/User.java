package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a user in the system, containing personal and authentication details.
 */
@Entity
@Table(name = "userDetails")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<RenovationRecord> renovationRecords;

    @Column(nullable = false, length = 64)
    private String firstName;

    @Column(length = 64)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    @Column()
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<Authority> userRoles;

    /**
     * Creates a new User object
     * @param firstName first name of user
     * @param lastName last name of user
     * @param email user's email
     * @param password user's password
     */
    public User(String firstName, String lastName, String email,  String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.createdTimestamp = LocalDateTime.now();
    }

    /**
     * JPA required no-args constructor
     */
    public User() {}

    /**
     * Adds a new authority to the user's roles.
     * @param authority the name of the authority to add
     */
    public void grantAuthority(String authority) {
        if (userRoles == null) {
            userRoles = new ArrayList<>();
        }
        userRoles.add(new Authority(authority));
    }

    /**
     * Get the list of authorities associated with the user.
     * @return a list of spring {@link GrantedAuthority}
     */
    public List<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        this.userRoles.forEach(authority -> authorities.add(new SimpleGrantedAuthority(authority.getRole())));
        return authorities;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public  String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName) && Objects.equals(email, user.email);
    }
}
