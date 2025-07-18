package nz.ac.canterbury.seng302.homehelper.dto;

import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;

import java.util.List;

public class UserRegisterDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String confirmPassword;
    private int countryCode;

    private String phoneNumber;

    private List<Skill> skills;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getCountryCode() {
        return countryCode;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public Float getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Float hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    private Float hourlyRate;


    public UserRegisterDTO() {}

    public UserRegisterDTO(String firstName, String lastName, String email, String password, String confirmPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public UserRegisterDTO(String firstName, String lastName, String email, String password, String confirmPassword, int countryCode, String phoneNumber, List<Skill> skills, Float hourlyRate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
        this.skills = skills;
        this.hourlyRate = hourlyRate;
    }

    /**
     * @return the user's first name
     */
    public String getFirstName() { return firstName; }

    /**
     * @param firstName user's firstname
     */
    public void setFirstName(String firstName) {this.firstName = firstName;}

    /**
     * @return the user's last name
     */
    public String getLastName() { return lastName; }

    /**
     * @param lastName user's last name
     */
    public void setLastName(String lastName) {this.lastName = lastName;}

    /**
     * @return the user's email
     */
    public String getEmail() { return email; }

    /**
     * @param email user's email
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * @return the user's password
     */
    public String getPassword() { return password; }

    /**
     * @param password user's password
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * @return the user's confirmation password
     */
    public String getConfirmPassword() { return confirmPassword; }

    /**
     * @param confirmPassword user's password confirmation
     */
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}

