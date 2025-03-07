package nz.ac.canterbury.seng302.homehelper.unit.controller;

import nz.ac.canterbury.seng302.homehelper.controller.DemoFormController;
import nz.ac.canterbury.seng302.homehelper.entity.FormResult;
import nz.ac.canterbury.seng302.homehelper.service.FormService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.Model;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;

public class FormControllerTest {

    @Test
    void testForm_DefaultValues() {
        FormService formServiceSpy = Mockito.mock(FormService.class);
        DemoFormController demoFormController = new DemoFormController(formServiceSpy);

        Model model = Mockito.mock(Model.class);

        String viewName = demoFormController.form("", "", null, model);

        Assertions.assertEquals("demoFormTemplate", viewName);
        Mockito.verify(model).addAttribute("displayName", "");
        Mockito.verify(model).addAttribute("displayFavouriteLanguage", "");
        Mockito.verify(model).addAttribute("isJava", false);
        Mockito.verify(model).addAttribute("errorMessage", null);
    }


    @Test
    void testForm_WithParameters_JavaLanguage() {
        FormService formServiceSpy = Mockito.mock(FormService.class);
        DemoFormController demoFormController = new DemoFormController(formServiceSpy);

        Model model = Mockito.mock(Model.class);

        String viewName = demoFormController.form("Alice", "Java", null, model);

        Assertions.assertEquals("demoFormTemplate", viewName);
        Mockito.verify(model).addAttribute("displayName", "Alice");
        Mockito.verify(model).addAttribute("displayFavouriteLanguage", "Java");
        Mockito.verify(model).addAttribute("isJava", true);
        Mockito.verify(model).addAttribute("errorMessage", null);
    }

    @Test
    void testForm_WithParameters_NonJavaLanguage() {
        FormService formServiceSpy = Mockito.mock(FormService.class);
        DemoFormController demoFormController = new DemoFormController(formServiceSpy);

        Model model = Mockito.mock(Model.class);

        String viewName = demoFormController.form("Alice", "Python", null, model);

        Assertions.assertEquals("demoFormTemplate", viewName);
        Mockito.verify(model).addAttribute("displayName", "Alice");
        Mockito.verify(model).addAttribute("displayFavouriteLanguage", "Python");
        Mockito.verify(model).addAttribute("isJava", false);
        Mockito.verify(model).addAttribute("errorMessage", null);
    }


    @Test
    void testForm_WithParameters_ErrorMessage_LanguageTrimmed() {
        FormService formServiceSpy = Mockito.mock(FormService.class);
        DemoFormController demoFormController = new DemoFormController(formServiceSpy);

        Model model = Mockito.mock(Model.class);

        String viewName = demoFormController.form("Alice", " ", "Language cam not be empty.", model);

        Assertions.assertEquals("demoFormTemplate", viewName);
        Mockito.verify(model).addAttribute("displayName", "Alice");
        Mockito.verify(model).addAttribute("displayFavouriteLanguage", "");
        Mockito.verify(model).addAttribute("isJava", false);
        Mockito.verify(model).addAttribute("errorMessage", "Language cam not be empty.");
    }

    @Test
    void testForm_WithParameters_ErrorMessage_NameTrimmed() {
        FormService formServiceSpy = Mockito.mock(FormService.class);
        DemoFormController demoFormController = new DemoFormController(formServiceSpy);

        Model model = Mockito.mock(Model.class);

        String viewName = demoFormController.form(" ", "Python", "Name cam not be empty.", model);

        Assertions.assertEquals("demoFormTemplate", viewName);
        Mockito.verify(model).addAttribute("displayName", "");
        Mockito.verify(model).addAttribute("displayFavouriteLanguage", "Python");
        Mockito.verify(model).addAttribute("isJava", false);
        Mockito.verify(model).addAttribute("errorMessage", "Name cam not be empty.");
    }

    @Test
    void testSubmitForm() {
        FormService formServiceSpy = Mockito.mock(FormService.class);
        DemoFormController demoFormController = new DemoFormController(formServiceSpy);

        Model model = Mockito.mock(Model.class);
        String viewName = demoFormController.submitForm("John", "Java", model);

        Assertions.assertEquals("demoFormTemplate", viewName);
        Mockito.verify(model).addAttribute("displayName", "John");
        Mockito.verify(model).addAttribute("displayFavouriteLanguage", "Java");
        Mockito.verify(model).addAttribute("isJava", true);
    }

    @Test
    void testSubmitForm_nonJavaLanguage() {
        FormService formServiceSpy = Mockito.mock(FormService.class);
        DemoFormController demoFormController = new DemoFormController(formServiceSpy);

        Model model = Mockito.mock(Model.class);
        String viewName = demoFormController.submitForm("John", "Python", model);

        Assertions.assertEquals("demoFormTemplate", viewName);
        Mockito.verify(model).addAttribute("displayName", "John");
        Mockito.verify(model).addAttribute("displayFavouriteLanguage", "Python");
        Mockito.verify(model).addAttribute("isJava", false);
    }

    @Test
    void testSubmitForm_emptyName() {
        FormService formServiceSpy = Mockito.mock(FormService.class);
        Mockito.when(formServiceSpy.addFormResult(any())).thenThrow(new IllegalArgumentException(""));
        DemoFormController demoFormController = new DemoFormController(formServiceSpy);

        Model model = Mockito.mock(Model.class);
        String viewName = demoFormController.submitForm(" ", "Python", model);

        Assertions.assertEquals("demoFormTemplate", viewName);
        Mockito.verify(model).addAttribute("name", "");
        Mockito.verify(model).addAttribute("favouriteLanguage", "Python");
        Mockito.verify(model).addAttribute(Mockito.eq("errorMessage"), anyString());
        Mockito.verify(model, Mockito.never()).addAttribute(Mockito.eq("displayName"), anyString());
        Mockito.verify(model, Mockito.never()).addAttribute(Mockito.eq("displayFavouriteLanguage"), anyString());
        Mockito.verify(model, Mockito.never()).addAttribute(Mockito.eq("isJava"), anyBoolean());
    }

    @Test
    void testSubmitForm_emptyLanguage() {
        FormService formServiceSpy = Mockito.mock(FormService.class);
        Mockito.when(formServiceSpy.addFormResult(any())).thenThrow(new IllegalArgumentException(""));
        DemoFormController demoFormController = new DemoFormController(formServiceSpy);

        Model model = Mockito.mock(Model.class);
        String viewName = demoFormController.submitForm("John", " ", model);

        Assertions.assertEquals("demoFormTemplate", viewName);
        Mockito.verify(model).addAttribute("name", "John");
        Mockito.verify(model).addAttribute("favouriteLanguage", "");
        Mockito.verify(model).addAttribute(Mockito.eq("errorMessage"), anyString());
        Mockito.verify(model, Mockito.never()).addAttribute(Mockito.eq("displayName"), anyString());
        Mockito.verify(model, Mockito.never()).addAttribute(Mockito.eq("displayFavouriteLanguage"), anyString());
        Mockito.verify(model, Mockito.never()).addAttribute(Mockito.eq("isJava"), anyBoolean());
    }

    @Test
    void testGetResponses_noResponse() {
        FormService formServiceSpy = Mockito.mock(FormService.class);
        DemoFormController demoFormController = new DemoFormController(formServiceSpy);

        Model model = Mockito.mock(Model.class);
        String viewName = demoFormController.responses(null, model);

        Mockito.verify(formServiceSpy).getFormResults(null);
        Assertions.assertEquals("demoResponsesTemplate", viewName);
        Mockito.verify(model).addAttribute("responses", List.of());
    }

    @Test
    void testGetResponses() {
        FormService formServiceSpy = Mockito.mock(FormService.class);
        List<FormResult> expectedResult = List.of(new FormResult("John", "Java"));
        Mockito.when(formServiceSpy.getFormResults(any())).thenReturn(expectedResult);
        DemoFormController demoFormController = new DemoFormController(formServiceSpy);

        Model model = Mockito.mock(Model.class);
        String viewName = demoFormController.responses(null, model);

        Mockito.verify(formServiceSpy).getFormResults(null);
        Assertions.assertEquals("demoResponsesTemplate", viewName);
        Mockito.verify(model).addAttribute("responses", expectedResult);
    }


    @Test
    void testGetResponses_nameFilter() {
        FormService formServiceSpy = Mockito.mock(FormService.class);
        List<FormResult> expectedResult = List.of(new FormResult("John", "Java"));
        Mockito.when(formServiceSpy.getFormResults(any())).thenReturn(expectedResult);
        DemoFormController demoFormController = new DemoFormController(formServiceSpy);

        Model model = Mockito.mock(Model.class);
        String viewName = demoFormController.responses("john", model);

        Mockito.verify(formServiceSpy).getFormResults("john");
        Assertions.assertEquals("demoResponsesTemplate", viewName);
        Mockito.verify(model).addAttribute("responses", expectedResult);
    }

    @Test
    void testGetResponses_emptyNameFilter() {
        FormService formServiceSpy = Mockito.mock(FormService.class);
        List<FormResult> expectedResult = List.of(new FormResult("John", "Java"));
        Mockito.when(formServiceSpy.getFormResults(any())).thenReturn(expectedResult);
        DemoFormController demoFormController = new DemoFormController(formServiceSpy);

        Model model = Mockito.mock(Model.class);
        String viewName = demoFormController.responses("", model);

        Mockito.verify(formServiceSpy).getFormResults("");
        Assertions.assertEquals("demoResponsesTemplate", viewName);
        Mockito.verify(model).addAttribute("responses", expectedResult);
    }


}
