package nz.ac.canterbury.seng302.homehelper.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Represents a team entity, associated with a renovation record
 * and comprising a collection of roles. Each role defines a specific contractor,
 * skill, and accepted status, representing various roles within the team.
 */
@Entity
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RenovationRecord renovationRecord;

    @ElementCollection
    private final List<Role> roles = new ArrayList<>();

    protected Team() {}

    public Team(RenovationRecord renovationRecord) {
        this.renovationRecord = renovationRecord;
    }

    public Long getId() {
        return id;
    }

    public RenovationRecord getRenovationRecord() {
        return renovationRecord;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    /**
     * Replaces the contractor for a specific role in the team.
     * @param existingRole   the role to update
     * @param newContractor  the contractor to assign to the role
     */
    public void replaceRoleContractor(Role existingRole, Contractor newContractor) {
        int index = roles.indexOf(existingRole);
        if (index != -1) {
            Role updatedRole = new Role(newContractor, existingRole.getSkill(), existingRole.isAccepted());
            roles.set(index, updatedRole);
        }
    }
}
