package nz.ac.canterbury.seng302.homehelper.config;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.security.GenerationStrategy;
import nz.ac.canterbury.seng302.homehelper.service.RegisterService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationTaskService;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Sets up the default data to show the functionality of our stories
 *
 * @author Rafe Dunlop
 */
@Component
public class DefaultDataConfigurator {

    private final RegisterService registerService;

    private final RenovationRecordService renovationRecordService;

    private final RenovationTaskService renovationTaskService;

    private final VerificationCodeService verificationCodeService;

    private User default1;

    private RenovationRecord default1Renovation1;

    private static final int numGenericTasksToAdd = 100;

    private static final List<String> defaultJERooms = List.of("131", "133", "Fabian's office");

    @Autowired
    public DefaultDataConfigurator(RegisterService registerService,
                                   RenovationRecordService renovationRecordService,
                                   RenovationTaskService renovationTaskService,
                                   VerificationCodeService verificationCodeService) {
        this.registerService = registerService;
        this.renovationRecordService = renovationRecordService;
        this.renovationTaskService = renovationTaskService;
        this.verificationCodeService = verificationCodeService;
    }

    @EventListener(ApplicationReadyEvent.class)
    private void onApplicationReady() {
        setupDefaultUsers();
        setupDefaultRenovations();
        setupDefaultRenovationTasks();
    }

    private void setupDefaultUsers() {
        UserRegisterDTO user = new UserRegisterDTO();
        user.setEmail("seng302.team200.test@gmail.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setPassword("P4$$word");
        user.setConfirmPassword("P4$$word");
        default1 = registerService.registerUser(user);
        String code = verificationCodeService.issueSignupCode(GenerationStrategy.READABLE, default1, Locale.ENGLISH);
        verificationCodeService.consumeSignupCode(code);
    }

    private void setupDefaultRenovations() {
        default1Renovation1 = renovationRecordService.addRenovationRecord(
                new RenovationRecord(default1,
                        "Jack Erskine revamp",
                    "CSSE building => palace of slay",
                        defaultJERooms
                )
        );
    }

    private void setupDefaultRenovationTasks() {
        RenovationTaskDTO renovationTask = new RenovationTaskDTO(
                "Build Fabian monument",
                "at least 100 feet high, pokes out the top of the building",
                LocalDate.now().plusYears(5),
                List.of(defaultJERooms.get(2))
        );
        renovationTaskService.addRenovationTask(renovationTask, default1Renovation1);

        for (int i = 2; i < numGenericTasksToAdd + 2; i++) {
            renovationTask.setName(String.format("Renovation Task %d", i));
            renovationTask.setDescription(String.format("Renovation Task Description %d", i));
            renovationTask.setDueDate(LocalDate.now().plusDays(i));
            renovationTask.setRooms(defaultJERooms);
            renovationTaskService.addRenovationTask(renovationTask, default1Renovation1);
        }
    }
}
