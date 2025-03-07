package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.FormResult;
import nz.ac.canterbury.seng302.homehelper.repository.FormRepository;
import nz.ac.canterbury.seng302.homehelper.service.FormService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;

@Import(FormService.class)
public class FormServiceTest {

    List<FormResult> mockFormResultResponses = Arrays.asList(
            new FormResult("Alice", "English"),
            new FormResult("Bob", "Spanish")
    );

    @Test
    public void addFormResultTest() {
        // mock (spy) setup \/
        FormRepository formRepositorySpy = Mockito.mock(FormRepository.class);
        Mockito.doAnswer(invocation -> invocation.getArgument(0)).when(formRepositorySpy).save(Mockito.any(FormResult.class));

        // actual test code \/
        FormService formService = new FormService(formRepositorySpy);
        formService.addFormResult(new FormResult("John", "Python"));

        // get argument passed to formRepository.save() \/
        ArgumentCaptor<FormResult> formResultCaptor = ArgumentCaptor.forClass(FormResult.class);
        Mockito.verify(formRepositorySpy).save(formResultCaptor.capture());

        // validate argument passed is as expected \/
        FormResult capturedFormResult = formResultCaptor.getValue();
        Assertions.assertNotNull(capturedFormResult);
        Assertions.assertEquals("John", capturedFormResult.getName());
        Assertions.assertEquals("Python", capturedFormResult.getLanguage());

        // validate timestamp is set and is recent \/
        Assertions.assertNotNull(capturedFormResult.getCreatedTimestamp());
        Assertions.assertTrue(capturedFormResult.getCreatedTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
        Assertions.assertTrue(capturedFormResult.getCreatedTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }


    static Stream<Arguments> invalidFormInputs() {
        return Stream.of(
                Arguments.of("", "Python"),   // Empty name
                Arguments.of(" ", "Python"),  // Whitespace name
                Arguments.of(null, "Python"), // Null name
                Arguments.of("John", ""),     // Empty language
                Arguments.of("John", " "),    // Whitespace language
                Arguments.of("John", null)    // Null language
        );
    }
    @ParameterizedTest
    @MethodSource("invalidFormInputs")
    public void addFormResultTest_invalidInputs(String name, String language) {
        FormRepository formRepositorySpy = Mockito.mock(FormRepository.class);
        FormService formService = new FormService(formRepositorySpy);

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                formService.addFormResult(new FormResult(name, language))
        );

        Mockito.verify(formRepositorySpy, never()).findAll();
    }
    @Test
    public void getFormResultsNoFilterCallsFindAll() {
        FormRepository formRepositorySpy = Mockito.mock(FormRepository.class);
        Mockito.when(formRepositorySpy.findAll()).thenReturn(mockFormResultResponses);

        FormService formService = new FormService(formRepositorySpy);
        List<FormResult> formResults = formService.getFormResults("");

        Assertions.assertEquals(mockFormResultResponses, formResults);
        Mockito.verify(formRepositorySpy).findAll();
    }


    @Test
    public void getFormResultsNullFilterCallsFindAll() {
        FormRepository formRepositorySpy = Mockito.mock(FormRepository.class);
        Mockito.when(formRepositorySpy.findAll()).thenReturn(mockFormResultResponses);

        FormService formService = new FormService(formRepositorySpy);
        List<FormResult> formResults = formService.getFormResults(null);

        Assertions.assertEquals(mockFormResultResponses, formResults);
        Mockito.verify(formRepositorySpy).findAll();
    }


    @Test
    public void getFormResultsNameFilterCallsFindByNameContainingIgnoreCase() {
        FormRepository formRepositorySpy = Mockito.mock(FormRepository.class);
        Mockito.when(formRepositorySpy.findByNameContainingIgnoreCase(anyString())).thenReturn(mockFormResultResponses);

        FormService formService = new FormService(formRepositorySpy);
        List<FormResult> formResults = formService.getFormResults("john");

        Assertions.assertEquals(mockFormResultResponses, formResults);
        Mockito.verify(formRepositorySpy, never()).findAll();
        Mockito.verify(formRepositorySpy).findByNameContainingIgnoreCase("john");
    }
}
