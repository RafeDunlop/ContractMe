package nz.ac.canterbury.seng302.homehelper.dto;

import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;

import java.util.Objects;

public record MappedContractor(String fullName, String email, Location location, String phoneNumberFormatted,
                               Skill skill, Float hourlyRate, String profilePicture) {
    public MappedContractor(Contractor contractor, Skill skill) {
        this(contractor.getFullName(), contractor.getEmail(), contractor.getLocation(),
                contractor.getPhoneNumberFormatted(), skill, contractor.getHourlyRate(), contractor.getProfilePicture());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MappedContractor that = (MappedContractor) o;
        return skill == that.skill && Objects.equals(email, that.email) && Objects.equals(fullName, that.fullName) && Objects.equals(hourlyRate, that.hourlyRate) && Objects.equals(location, that.location) && Objects.equals(profilePicture, that.profilePicture) && Objects.equals(phoneNumberFormatted, that.phoneNumberFormatted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, email, location, phoneNumberFormatted, skill, hourlyRate, profilePicture);
    }
}