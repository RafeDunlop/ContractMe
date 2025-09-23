package nz.ac.canterbury.seng302.homehelper.dto;

import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;

public record MappedContractor(String fullName, String email, Location location, String phoneNumberFormatted,
                               Skill skill, Float hourlyRate, String profilePicture) {

}