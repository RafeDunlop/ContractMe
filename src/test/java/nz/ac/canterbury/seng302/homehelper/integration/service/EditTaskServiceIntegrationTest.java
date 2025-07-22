//package nz.ac.canterbury.seng302.homehelper.integration.service;
//
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
//import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
//import nz.ac.canterbury.seng302.homehelper.validation.RenovationTaskValidation;
//
//public class EditTaskServiceIntegrationTest {
//    private EditTaskService toTest;
//    private RenovationTaskRepository renovationTaskRepository;
//
//    @BeforeEach
//    public void setUp() {
//        renovationTaskRepository = mock(RenovationTaskRepository.class);
//        toTest = new EditTaskService(renovationTaskRepository, new RenovationTaskValidation());
//    }
//
//
//}
