package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import nz.ac.canterbury.seng302.homehelper.validation.TagValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    public void checkExists_tagExists_returnsTrue() {
        when(tagRepository.findExactMatchTagByTagName("cars")).thenReturn(Optional.of(new Tag("cars")));
        assertFalse(tagService.checkExists("cars"));
    }

    @Test
    public void checkExists_tagNotExists_returnsFalse() {
        when(tagRepository.findExactMatchTagByTagName("hammer")).thenReturn(Optional.empty());
        assertTrue(tagService.checkExists("hammer"));
    }

    @Test
    public void addTagToRenovation_tagExists_succeeds() {
        RenovationRecord record = new RenovationRecord();
        record.setTags(new ArrayList<>());

        String tagName = "kitchen";
        Tag tag = new Tag(tagName);

        when(tagRepository.findExactMatchTagByTagName(tagName.toLowerCase().trim()))
                .thenReturn(Optional.of(tag));

        tagService.addTagToRenovation(record, tagName);

        verify(renovationRecordRepository, times(1)).save(record);
        assertTrue(record.getTags().contains(tag));
    }



    @Test
    public void validateTag_validTag_returnsNoErrors() {
        RenovationRecord record = mock(RenovationRecord.class);

        List<String> errors = tagService.validateTagAndRecord(record, "window");
        assertTrue(errors.isEmpty());
    }


    @Test
    public void validateTag_duplicateTag_returnsError() {
        RenovationRecord record = new RenovationRecord();

        record.setTags(new ArrayList<>(List.of(new Tag("bathroom"))));

        List<String> errors = tagService.validateTagAndRecord(record, "bathroom");

        assertTrue(errors.contains("Renovation cannot contain duplicate tag names."));
    }

    @Test
    public void validateTag_tooManyTags_returnsError() {
        RenovationRecord record = new RenovationRecord();
        List<Tag> tags = Arrays.asList(
                new Tag("kitchen"),
                new Tag("bathroom"),
                new Tag("living-room"),
                new Tag("bedroom"),
                new Tag("garage")
        );
        record.setTags(new ArrayList<>(tags));

        List<String> errors = tagService.validateTagAndRecord(record, "window");

        assertTrue(errors.contains("Renovation cannot have more than 5 tags."));
    }

    @Test
    public void removeTagFromRenovation_tagExistsNoOtherTags_tagRemovedFromRecord() {
        RenovationRecord record = new RenovationRecord(Mockito.mock(User.class), "Test Renovation", "Some words", List.of("Room1", "Room2"));
        Tag tag = new Tag("kitchen");
        List<Tag> tagList = new ArrayList<>();
        tagList.add(tag);
        record.setTags(tagList);

        tagService.removeTagFromRenovation(record, tag);
        verify(renovationRecordRepository, times(1)).save(record);
        assertFalse(record.getTags().contains(tag));
    }

    @Test
    public void removeTagFromRenovation_tagExistsWithOtherTags_tagRemovedFromRecord() {
        RenovationRecord record = new RenovationRecord(Mockito.mock(User.class), "Test Renovation", "Some words", List.of("Room1", "Room2"));
        Tag tag = new Tag("kitchen");
        Tag otherTag = new Tag("kitchen 2");
        List<Tag> tagList = new ArrayList<>();
        tagList.add(tag);
        tagList.add(otherTag);
        record.setTags(tagList);


        tagService.removeTagFromRenovation(record, tag);
        verify(renovationRecordRepository, times(1)).save(record);
        assertFalse(record.getTags().contains(tag));
        assertTrue(record.getTags().contains(otherTag));
    }
}
