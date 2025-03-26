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

    @Column
    private String profilePicture;

    @Column(name = "activated")
    private boolean activated;

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
        this.profilePicture = "default/default.jpg";
        this.activated = false;
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

    /**
     * Gets id of the user
     * @return ID of the user as a long integer
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the first name of the user
     * @return First name of user
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the user
     * @param firstName Inputted first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name of the user
     * @return Last name of user
     */
    public  String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the user
     * @param lastName Inputted last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the email of the user
     * @return Email of user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email of the user
     * @param email Inputted email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets password of user
     * @return Password of user
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set password of user
     * @param password Inputted password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the timestamp of when the user was registered
     * @return Timestamp of user registration
     */
    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    /**
     * Gets the profile picture file name of user
     * @return profile picture file name
     */
    public String getProfilePicture() { return profilePicture; }

    /**
     * Sets the profile picture file name of user
     * @param profilePicture profile picture file name
     */
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }


    /**
     * Set the user account active and grants role user.
     * This means that they have verified their email address.
     */
    public void activate() {
        this.activated = true;
        this.grantAuthority("ROLE_USER");
    }

    /**
     * Get the activated status of the user account.
     *
     * @return true if the user has verified their email address and therefore
     * has an active account
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * Overrides the existing equals method for the User object. When a User is compared to another object, it checks if the
     * other object is of a User and that the all the parameters from both objects are equal.
     * @param o The object the User is being compared to
     * @return A boolean whether the two objects are the same
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName) && Objects.equals(email, user.email);
    }
}
