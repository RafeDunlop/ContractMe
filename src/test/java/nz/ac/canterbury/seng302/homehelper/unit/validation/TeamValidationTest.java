package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.dto.TeamRequestDTO;
import nz.ac.canterbury.seng302.homehelper.validation.TeamValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TeamValidationTest {
    private static TeamValidation teamValidation;
    @BeforeAll
    static void Setup() {
        teamValidation = new TeamValidation();
    }

    @Test
    public void testValidateTeamSize_noSkills_returnsError() {
        TeamRequestDTO dto = new TeamRequestDTO();
        String error = teamValidation.validateTeamSize(dto);
        assertEquals("Your team request must have at least one role.", error);
    }

    @Test
    public void testValidateTeamSize_invalidSize_returnsError() {
        TeamRequestDTO dto = new TeamRequestDTO();
        dto.setSkills(List.of("One", "Two", "Three", "Four", "Five", "Six"));
        String error = teamValidation.validateTeamSize(dto);
        assertEquals("Your team request cannot have more than 5 roles.", error);
    }

    @Test
    public void testValidateSkills_allValidSkills_returnsNull() {
        TeamRequestDTO dto = new TeamRequestDTO();
        dto.setSkills(List.of("Electrical", "Plumbing", "Carpentry"));

        String error = teamValidation.validateSkills(dto);

        assertNull(error);
    }

    @Test
    public void testValidateSkills_invalidSkill_returnsError() {
        TeamRequestDTO dto = new TeamRequestDTO();
        String input = "Not Skill";
        dto.setSkills(List.of(input));

        String error = teamValidation.validateSkills(dto);

        assertEquals("Error: '" + input + "' is not a valid skill.", error);
    }

}
