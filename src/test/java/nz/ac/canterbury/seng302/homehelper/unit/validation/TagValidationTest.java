package nz.ac.canterbury.seng302.homehelper.unit.validation;


import nz.ac.canterbury.seng302.homehelper.profanityFilter.ProfanityFilter;
import nz.ac.canterbury.seng302.homehelper.profanityFilter.dictionary.Profanity;
import nz.ac.canterbury.seng302.homehelper.validation.TagValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class TagValidationTest {

    private static TagValidation tagValidation;
    private static ProfanityFilter profanityFilter;

    @BeforeAll
    static void validatorSetup() {
        tagValidation = new TagValidation();
        profanityFilter = Mockito.mock(ProfanityFilter.class);
    }

    @Test
    public void tagValidation_emptyTag_returnsErrors() {
        when(profanityFilter.find("en","")).thenReturn(null);
        List<String> errors = tagValidation.validateName("");
        assertTrue(errors.contains("Tags must contain one or more letters."));
    }

    @Test
    public void tagValidation_tagMoreThan128Characters_returnsErrors() {
        String name129 = "a".repeat(129);
        when(profanityFilter.find("en",name129)).thenReturn(null);
        List<String> errors = tagValidation.validateName(name129);
        assertTrue(errors.contains("Tag cannot be greater than 128 characters."));
    }

    @Test
    public void tagValidation_tagLessThan128Characters_returnsNoErrors() {
        String name128 = "a".repeat(128);
        when(profanityFilter.find("en",name128)).thenReturn(null);
        List<String> errors = tagValidation.validateName(name128);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void tagValidation_oneLetters_returnsNoErrors() {
        when(profanityFilter.find("en","A 123")).thenReturn(null);
        List<String> errors = tagValidation.validateName("A 123");
        assertTrue(errors.isEmpty());
    }



    @Test
    public void tagValidation_noLetters_returnsErrors() {
        when(profanityFilter.find("en","123")).thenReturn(null);
        List<String> errors = tagValidation.validateName("123");
        assertTrue(errors.contains("Tags must contain one or more letters."));
    }

    @Test
    public void tagValidation_noLettersAndTagMoreThan128Characters_returnsErrors() {
        String name129 = "1".repeat(129);
        when(profanityFilter.find("en",name129)).thenReturn(null);
        List<String> errors = tagValidation.validateName(name129);
        assertTrue(errors.contains("Tags must contain one or more letters.") && errors.contains("Tag cannot be greater than 128 characters."));
    }

    @Test
    public void tagValidation_noProfanityAndTagMoreThan128Characters_returnsErrors() {
        String name129 = "ass ".repeat(129);
        when(profanityFilter.find("en",name129)).thenReturn(Mockito.mock(Profanity.class));
        List<String> errors = tagValidation.validateName(name129);
        assertTrue(errors.contains("Name does not follow the system language standards.") && errors.contains("Tag cannot be greater than 128 characters."));
    }


    @Test
    public void profanityValidation_validTagName_returnsNoErrors() {
        when(profanityFilter.find("en","kitchen")).thenReturn(null);
        List<String> errors = tagValidation.validateName("kitchen");
        assertTrue(errors.isEmpty());
    }

    @Test
    public void profanityValidation_invalidTagName_returnsErrors() {
        when(profanityFilter.find("en","ass")).thenReturn(Mockito.mock(Profanity.class));
        List<String> errors = tagValidation.validateName("ass");
        assertTrue(errors.contains("Name does not follow the system language standards."));
    }

}
