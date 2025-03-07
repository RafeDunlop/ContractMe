package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.DemoFormController;
import nz.ac.canterbury.seng302.homehelper.entity.FormResult;
import nz.ac.canterbury.seng302.homehelper.repository.FormRepository;
import nz.ac.canterbury.seng302.homehelper.service.FormService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class FormControllerIntegrationTest {

    @Autowired
    private DemoFormController demoFormController; // Autowire a real controller

    private MockMvc mockMvc;

    @Autowired
    private FormService formService; // Autowire a real service

    @MockBean
    private FormRepository formRepository; // Mock the repository so we don't actually deal with data storage

    // Set up MockMvc manually
    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(demoFormController).build();
    }

    @Test
    void testGetForm_DefaultValues() throws Exception {
        mockMvc.perform(get("/form"))
                .andExpect(status().isOk())
                .andExpect(view().name("demoFormTemplate"))
                .andExpect(model().attribute("displayName", ""))
                .andExpect(model().attribute("displayFavouriteLanguage", ""))
                .andExpect(model().attribute("isJava", false))
                .andExpect(model().attributeDoesNotExist("errorMessage"));
    }

    @Test
    void testGetForm_WithParameters() throws Exception {
        mockMvc.perform(get("/form")
                        .param("displayName", "Alice")
                        .param("displayFavouriteLanguage", "Java")
                        .param("errorMessages", "Invalid input"))
                .andExpect(status().isOk())
                .andExpect(view().name("demoFormTemplate"))
                .andExpect(model().attribute("displayName", "Alice"))
                .andExpect(model().attribute("displayFavouriteLanguage", "Java"))
                .andExpect(model().attribute("isJava", true))
                .andExpect(model().attribute("errorMessage", "Invalid input"));
    }

    @Test
    void testGetForm_WithNonJavaLanguage() throws Exception {
        mockMvc.perform(get("/form")
                        .param("displayName", "Bob")
                        .param("displayFavouriteLanguage", "Python"))
                .andExpect(status().isOk())
                .andExpect(view().name("demoFormTemplate"))
                .andExpect(model().attribute("displayName", "Bob"))
                .andExpect(model().attribute("displayFavouriteLanguage", "Python"))
                .andExpect(model().attribute("isJava", false));
    }

    @Test
    void testPostForm_validInput_callsFormRepository() throws Exception {
        mockMvc.perform(post("/form")
                        .param("name", "John")
                        .param("favouriteLanguage", "Python"))
                .andExpect(status().isOk())
                .andExpect(view().name("demoFormTemplate"))
                .andExpect(model().attribute("displayName", "John"))
                .andExpect(model().attribute("displayFavouriteLanguage", "Python"))
                .andExpect(model().attribute("isJava", false))
                .andExpect(model().attributeDoesNotExist("errorMessage"));

        ArgumentCaptor<FormResult> formResultCaptor = ArgumentCaptor.forClass(FormResult.class);
        Mockito.verify(formRepository).save(formResultCaptor.capture());
        FormResult capturedFormResult = formResultCaptor.getValue();
        Assertions.assertEquals("John", capturedFormResult.getName());
        Assertions.assertEquals("Python", capturedFormResult.getLanguage());
        Assertions.assertNotNull(capturedFormResult.getCreatedTimestamp());
    }

    @Test
    void testPostForm_validInputJavaLanguage_callsFormRepository() throws Exception {
        mockMvc.perform(post("/form")
                        .param("name", "John")
                        .param("favouriteLanguage", "Java"))
                .andExpect(status().isOk())
                .andExpect(view().name("demoFormTemplate"))
                .andExpect(model().attribute("displayName", "John"))
                .andExpect(model().attribute("displayFavouriteLanguage", "Java"))
                .andExpect(model().attribute("isJava", true))
                .andExpect(model().attributeDoesNotExist("errorMessage"));

        ArgumentCaptor<FormResult> formResultCaptor = ArgumentCaptor.forClass(FormResult.class);
        Mockito.verify(formRepository).save(formResultCaptor.capture());
        FormResult capturedFormResult = formResultCaptor.getValue();
        Assertions.assertEquals("John", capturedFormResult.getName());
        Assertions.assertEquals("Java", capturedFormResult.getLanguage());
        Assertions.assertNotNull(capturedFormResult.getCreatedTimestamp());
    }

    @Test
    void testPostForm_invalidInputLanguage() throws Exception {
        mockMvc.perform(post("/form")
                        .param("name", "John")
                        .param("favouriteLanguage", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("demoFormTemplate"))
                .andExpect(model().attribute("name", "John"))
                .andExpect(model().attribute("favouriteLanguage", ""))
                .andExpect(model().attribute("errorMessage", "Invalid input: Please provide a valid language."))
                .andExpect(model().attributeDoesNotExist("displayName"))
                .andExpect(model().attributeDoesNotExist("displayLanguage"))
                .andExpect(model().attributeDoesNotExist("isJava"));
    }

    @Test
    void testPostForm_invalidInputName() throws Exception {
        mockMvc.perform(post("/form")
                        .param("name", " ")
                        .param("favouriteLanguage", "Python"))
                .andExpect(status().isOk())
                .andExpect(view().name("demoFormTemplate"))
                .andExpect(model().attribute("name", ""))
                .andExpect(model().attribute("favouriteLanguage", "Python"))
                .andExpect(model().attribute("errorMessage", "Invalid input: Please provide a valid name."))
                .andExpect(model().attributeDoesNotExist("displayName"))
                .andExpect(model().attributeDoesNotExist("displayLanguage"))
                .andExpect(model().attributeDoesNotExist("isJava"));
    }

    @Test
    void testPostForm_invalidInputNameAndLanguage() throws Exception {
        mockMvc.perform(post("/form")
                        .param("name", " ")
                        .param("favouriteLanguage", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("demoFormTemplate"))
                .andExpect(model().attribute("name", ""))
                .andExpect(model().attribute("favouriteLanguage", ""))
                .andExpect(model().attribute("errorMessage", "Invalid input: Please provide a valid name. Please provide a valid language."))
                .andExpect(model().attributeDoesNotExist("displayName"))
                .andExpect(model().attributeDoesNotExist("displayLanguage"))
                .andExpect(model().attributeDoesNotExist("isJava"));
    }


    @Test
    void testGetResponses_noResponses() throws Exception {
        mockMvc.perform(get("/form/responses"))
                .andExpect(status().isOk())
                .andExpect(view().name("demoResponsesTemplate"))
                .andExpect(model().attribute("responses", List.of()));
        Mockito.verify(formRepository).findAll();
        Mockito.verify(formRepository, Mockito.never()).findByNameContainingIgnoreCase(anyString());
    }


    @Test
    void testGetResponses() throws Exception {
        List<FormResult> expectedResult = List.of(new FormResult("John", "Java"));
        Mockito.when(formRepository.findAll()).thenReturn(expectedResult);
        mockMvc.perform(get("/form/responses"))
                .andExpect(status().isOk())
                .andExpect(view().name("demoResponsesTemplate"))
                .andExpect(model().attribute("responses", expectedResult));
        Mockito.verify(formRepository).findAll();
        Mockito.verify(formRepository, Mockito.never()).findByNameContainingIgnoreCase(anyString());
    }

    @Test
    void testGetResponses_nameFilter() throws Exception {
        List<FormResult> expectedResult = List.of(new FormResult("John", "Java"));
        Mockito.when(formRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(expectedResult);
        mockMvc.perform(get("/form/responses")
                        .param("name", "john"))
                .andExpect(status().isOk())
                .andExpect(view().name("demoResponsesTemplate"))
                .andExpect(model().attribute("responses", expectedResult));
        Mockito.verify(formRepository).findByNameContainingIgnoreCase("john");
        Mockito.verify(formRepository, Mockito.never()).findAll();
    }


    @Test
    void testGetResponses_emptyNameFilter() throws Exception {
        List<FormResult> expectedResult = List.of(new FormResult("John", "Java"));
        Mockito.when(formRepository.findAll()).thenReturn(expectedResult);
        mockMvc.perform(get("/form/responses")
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("demoResponsesTemplate"))
                .andExpect(model().attribute("responses", expectedResult));
        Mockito.verify(formRepository).findAll();
        Mockito.verify(formRepository, Mockito.never()).findByNameContainingIgnoreCase(anyString());
    }
}