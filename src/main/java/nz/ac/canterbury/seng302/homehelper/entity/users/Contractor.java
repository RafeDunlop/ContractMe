package nz.ac.canterbury.seng302.homehelper.entity.users;


import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "contractorDetails")
public class Contractor extends User {

    @Column
    private Float hourlyRate;

    @Column
    private String phoneNumber;

    @ElementCollection
    private Set<Skill> skills;

    @Column
    private Boolean available;

    public Contractor() {
        this.skills = new HashSet<>();
    }

    public Float getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Float hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public boolean addSkill(Skill skill) {
        return skills.add(skill);
    }

    public boolean removeSkill(Skill skill) {
        return skills.remove(skill);
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

}
