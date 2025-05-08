package nz.ac.canterbury.seng302.homehelper.config;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.security.GenerationStrategy;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
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
@Profile("!test & !cucumber & !production")
public class DefaultDataConfigurator {

    private final RegisterService registerService;

    private final RenovationRecordService renovationRecordService;

    private final RenovationTaskService renovationTaskService;

    private final VerificationCodeService verificationCodeService;
    private final TagService tagService;

    private User default1;

    private User default2;

    private RenovationRecord default1Renovation1;

    private RenovationRecord default2Renovation1;

    private static final int numGenericTasksToAdd = 101;

    private static final List<String> defaultJERooms = List.of("131", "133", "Fabian's office");

    @Autowired
    public DefaultDataConfigurator(RegisterService registerService,
                                   RenovationRecordService renovationRecordService,
                                   RenovationTaskService renovationTaskService,
                                   VerificationCodeService verificationCodeService,
                                   TagService tagService) {
        this.registerService = registerService;
        this.renovationRecordService = renovationRecordService;
        this.renovationTaskService = renovationTaskService;
        this.verificationCodeService = verificationCodeService;
        this.tagService = tagService;
    }

    @EventListener(ApplicationReadyEvent.class)
    private void onApplicationReady() {
        setupDefaultUsers();
        setupDefaultRenovations();
        setupDefaultRenovationTasks();
        setupDefaultTags();
    }

    private void setupDefaultUsers() {
        UserRegisterDTO user = new UserRegisterDTO();
        user.setEmail("seng302.team200.test@gmail.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setPassword("P4$$word");
        user.setConfirmPassword("P4$$word");
        default1 = registerService.registerUser(user);
        String code = verificationCodeService.issueVerificationCode(GenerationStrategy.SIGNUP, default1, Locale.ENGLISH);
        verificationCodeService.consumeSignupCode(code);

        user.setEmail("seng302.team200.test1@gmail.com");
        default2 = registerService.registerUser(user);
        code = verificationCodeService.issueVerificationCode(GenerationStrategy.SIGNUP, default2, Locale.ENGLISH);
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

        default2Renovation1 = renovationRecordService.addRenovationRecord(
                new RenovationRecord(default2,
                        "Jack Erskine revamp",
                        "CSSE building => palace of slay",
                        defaultJERooms
                )
        );

        // Add 200 test renovations for default1
        for (int i = 1; i <= 200; i++) {
            renovationRecordService.addRenovationRecord(
                    new RenovationRecord(default1,
                            "Test Renovation " + i,
                            "Description for renovation " + i,
                            defaultJERooms
                    )
            );
        }
    }

    private void setupDefaultRenovationTasks() {
        RenovationTaskDTO renovationTask = new RenovationTaskDTO(
                "Build Fabian monument",
                "at least 100 feet high, pokes out the top of the building",
                LocalDate.now().plusYears(5),
                List.of(defaultJERooms.get(2))
        );
        renovationTaskService.addRenovationTask(renovationTask, default2Renovation1);

        for (int i = 1; i < numGenericTasksToAdd + 1; i++) {
            renovationTask.setName(String.format("Renovation Task %d", i));
            renovationTask.setDescription(String.format("Renovation Task Description %d", i));
            renovationTask.setDueDate(LocalDate.now().plusDays(i));
            renovationTask.setRooms(defaultJERooms);
            renovationTaskService.addRenovationTask(renovationTask, default1Renovation1);
        }
    }

    private void setupDefaultTags() {
        tagService.addTag("Historic");
        tagService.addTag("History");
        tagService.addTag("His");
        tagService.addTag("histrionic");
    }
}
