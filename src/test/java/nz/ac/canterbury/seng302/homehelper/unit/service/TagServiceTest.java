package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import nz.ac.canterbury.seng302.homehelper.validation.TagValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class TagServiceTest {
    private TagRepository tagRepository;
    private RenovationRecordRepository renovationRecordRepository;
    private TagValidation tagValidation;
    private TagService tagService;
    @BeforeEach
    void setUp() {
        tagRepository = Mockito.mock(TagRepository.class);
        renovationRecordRepository = Mockito.mock(RenovationRecordRepository.class);
        tagValidation = Mockito.mock(TagValidation.class);
        tagService = new TagService(tagRepository, renovationRecordRepository, tagValidation);
    }

    @Test
    public void autocompleteTags_matchingTags_returnsListOfTagNames() {
        List<Tag> testTags = new ArrayList<>();
        testTags.add(new Tag("cars"));
        testTags.add(new Tag("castle"));
        when(tagRepository.findByNameContainingIgnoreCase("Car")).thenReturn(testTags);

        List<String> result = tagService.autocompleteTags("Car");

        assertEquals(2, result.size());
        assertTrue(result.contains("cars"));
        assertTrue(result.contains("castle"));
    }

    @Test
    public void autocompleteTags_noMatchingTags_returnsEmptyList() {
        when(tagRepository.findByNameContainingIgnoreCase("Bedroom")).thenReturn(Collections.emptyList());

        List<String> result = tagService.autocompleteTags("Bedroom");

        assertEquals(0, result.size());
    }

    @Test
    public void autocompleteTags_wrongCaseMatchingTags_ListOfTagNames() {
        List<Tag> testTags = new ArrayList<>();
        testTags.add(new Tag("apartment"));
        testTags.add(new Tag("arena"));
        when(tagRepository.findByNameContainingIgnoreCase("Ar")).thenReturn(testTags);

        List<String> result = tagService.autocompleteTags("Ar");

        assertEquals(2, result.size());
        assertTrue(result.contains("apartment"));
        assertTrue(result.contains("apartment"));
    }
}
