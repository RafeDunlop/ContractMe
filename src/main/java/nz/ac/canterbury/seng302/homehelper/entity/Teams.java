package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import org.checkerframework.checker.units.qual.A;

/**
 * Represents a team entity, associated with a renovation record
 * and comprising a collection of roles. Each role defines a specific contractor,
 * skill, and accepted status, representing various roles within the team.
 */
public class Teams {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private RenovationRecord renovationRecord;

    @ElementCollection
    @CollectionTable(name = "roles")
    private List<Role> roles = new ArrayList<>();

    protected Teams() {}

    public Teams(RenovationRecord renovationRecord, List<Role> roles) {
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



}
