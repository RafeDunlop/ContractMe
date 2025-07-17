package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;

import java.util.Objects;

@Entity
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "authority_id")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column()
    private String role;

    /**
     * JPA required no-args constructor
     */
    protected Authority() {}

    /**
     * Authority constructor.
     * @param role the name of the role (should start with ROLE e.g. ROLE_USER)
     */
    public Authority(String role) {
        this.role = role;
    }

    /**
     * Get the name of the authority.
     * @return the role name for the authority
     */
    public String getRole() {
        return role;
    }

    /**
     * Overrides the existing equals method for the Authority object. When an Authority is compared to another object, it checks
     * if the other object is an Authority and that the all the parameters from both objects are equal.
     * @param o The object the Authority is being compared to
     * @return A boolean whether the two objects are the same
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Authority authority = (Authority) o;
        return id == authority.id && Objects.equals(user, authority.user) && Objects.equals(role, authority.role);
    }
}
