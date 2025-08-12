package nz.ac.canterbury.seng302.homehelper.dto;

import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;

import java.util.Set;

public class ContractorDTO {
    private float hourlyRate;
    private String phoneNumber;
    private int countryCode;
    private Set<Skill> skills;

    public float getHourlyRate() {
        return hourlyRate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getCountryCode() {
        return countryCode;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public void setHourlyRate(float hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }

    public void setSkills(Set<Skill> skills) {
        this.skills = skills;
    }
}
