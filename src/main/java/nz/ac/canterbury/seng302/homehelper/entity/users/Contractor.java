package nz.ac.canterbury.seng302.homehelper.entity.users;


import jakarta.persistence.*;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Represents a contractor in the system.
 * A contractor is a type of {@link User} who can have an hourly rate, phone number,
 * availability status, and a set of skills.
 */
@Entity
@Table(name = "contractorDetails")
public class Contractor extends User {

    @Column
    private float hourlyRate;

    @Column
    private String phoneNumber;

    @Column
    private int countryCode;

    @ElementCollection
    private Set<Skill> skills;

    @Column
    private boolean available;

    /**
     * Constructor for Contractor object
     * @param firstName The first name of the contractor
     * @param lastName the last name of the contractor
     * @param email The email of the contractor
     * @param password The password of the contractor
     */
    public Contractor(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, email, password);
        this.skills = new HashSet<>();
    }

    public Contractor() {}

    /**
     * Formats the phone number entry into a standard format
     * @return The representation of the phone number supplied by the {@code Contractor}
     */
    public String getPhoneNumberFormatted() {
        return String.format("+%d %s",
                countryCode,
                phoneNumber
        );
    }

    /**
     * Formats the hourly rate specified for this contractor according to their {@link Locale}
     * @param locale The {@link Locale} associated with a region, i.e. the request locale
     * @return The formatted hourly rate
     */
    public String getHourlyRateFormatted(Locale locale) {
        return NumberFormat.getCurrencyInstance(locale).format(hourlyRate);
    }

    /**
     * Returns the contractor's hourly rate.
     *
     * @return The hourly rate, or {@code null} if not set.
     */
    public float getHourlyRate() {
        return hourlyRate;
    }
    /**
     * Sets the contractor's hourly rate.
     *
     * @param hourlyRate The hourly rate to set.
     */
    public void setHourlyRate(float hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    /**
     * Returns the contractor's phone number.
     *
     * @return The phone number, or {@code null} if not set.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
    /**
     * Sets the contractor's phone number.
     *
     * @param phoneNumber The phone number to set.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the country code for the contractor's phone number.
     * @return the country code as an integer
     */
    public int getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the country code for the contractor's phone number
     * @param countryCode the country code
     */
    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Returns the set of skills associated with the contractor.
     *
     * @return A {@code Set} of {@code Skill} .
     */
    public Set<Skill> getSkills() {
        return skills;
    }
    /**
     * Adds a skill to the contractor's skill set.
     *
     * @param skill The skill to add.
     * @return {@code true} if the skill was added, {@code false} if it was already present.
     */
    public boolean addSkill(Skill skill) {
        return skills.add(skill);
    }

    /**
     * Set  a skill to the contractor's skill set.
     *
     * @param skills The skill to add.
     */
    public void setSkills(Set<Skill> skills) {
        this.skills = skills;
    }

    /**
     * Removes a skill from the contractor's skill set.
     *
     * @param skill The skill to remove.
     * @return {@code true} if the skill was removed, {@code false} if it was not found.
     */
    public boolean removeSkill(Skill skill) {
        return skills.remove(skill);
    }
    /**
     * Returns whether the contractor is available.
     *
     * @return {@code true} if available, {@code false} if not, or {@code null} if undefined.
     */
    public boolean getAvailable() {
        return available;
    }
    /**
     * Sets the contractor's availability status.
     *
     * @param available {@code true} if available, {@code false} otherwise.
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

}
