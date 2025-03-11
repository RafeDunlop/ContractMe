package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

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

    protected Authority() {
        // JPA empty constructor
    }

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Authority authority = (Authority) o;
        return id == authority.id && Objects.equals(user, authority.user) && Objects.equals(role, authority.role);
    }
}
