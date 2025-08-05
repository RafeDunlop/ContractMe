package nz.ac.canterbury.seng302.homehelper.config;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import java.util.ArrayList;
import java.util.Arrays;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.security.GenerationStrategy;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private final ContractorService contractorService;

    private User default1;

    private User default2;

    private Contractor defaultContractor1;

    private RenovationRecord default1Renovation1;

    private RenovationRecord default2Renovation1;

    private static final int numGenericTasksToAdd = 101;

    private static final List<String> defaultJERooms = List.of("131", "133", "Fabian's office");

    @Autowired
    public DefaultDataConfigurator(RegisterService registerService,
                                   RenovationRecordService renovationRecordService,
                                   RenovationTaskService renovationTaskService,
                                   VerificationCodeService verificationCodeService,
                                   TagService tagService, ContractorService contractorService) {
        this.registerService = registerService;
        this.renovationRecordService = renovationRecordService;
        this.renovationTaskService = renovationTaskService;
        this.verificationCodeService = verificationCodeService;
        this.contractorService = contractorService;
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
        List<Skill> skills = new ArrayList<>(
                Arrays.asList(Skill.ACOUSTIC_INSULATION, Skill.ANTIQUE_RESTORATION)
        );
        user.setHourlyRate(22.33f);
        user.setSkills(skills);
        user.setCountryCode(64);
        user.setPhoneNumber("226430022");
        Location location = new Location("20 Kirkwood Avenue", "New Zealand", "8041", "Christchuch", "Upper Riccarton");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress_line1(location.getAddress());
        addressDTO.setCity(location.getCity());
        addressDTO.setCountry(location.getCountry());
        addressDTO.setPostcode(location.getPostcode());
        addressDTO.setRegion(location.getSuburb());
        default2 = contractorService.registerContractor(user,addressDTO);
        code = verificationCodeService.issueVerificationCode(GenerationStrategy.SIGNUP, default2, Locale.ENGLISH);
        verificationCodeService.consumeSignupCode(code);

        user.setIsContractor(true);
        user.setEmail("seng302.team200.contractor@gmail.com");
        user.setSkills(List.of(Skill.SCAFFOLDING, Skill.RESOURCE_CONSENT_COMPLIANCE, Skill.CARPENTRY));
        user.setHourlyRate(30.0f);
        user.setCountryCode(64);
        user.setPhoneNumber("33692888");
        AddressDTO address = new AddressDTO();
        address.setAddress_line1("Jack Erskine");
        address.setCity("Christchurch");
        address.setRegion("Ilam");
        address.setCountry("New Zealand");
        address.setPostcode("");
        defaultContractor1 = contractorService.registerContractor(user, address);
        code = verificationCodeService.issueVerificationCode(GenerationStrategy.SIGNUP, defaultContractor1, Locale.ENGLISH);
        verificationCodeService.consumeSignupCode(code);
    }

    private void setupDefaultRenovations() {
        default2Renovation1 = new RenovationRecord(default2,
                "Jack Erskine revamp",
                "CSSE building => palace of slay",
                defaultJERooms
        );
        default2Renovation1.setLocation(new Location("Jack Erskine", "", "", "", ""));
        default2Renovation1 = renovationRecordService.addRenovationRecord(default2Renovation1);

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

        //Add 100 public records to default2, so they can be seen on default1
        for (int i=0; i<=100; i++) {
            RenovationRecord record = new RenovationRecord(default2,
                    "Test Public Renovation " + i,
                    "Description for renovation: " + 1,
                    defaultJERooms);
            record.setPublicity(true);
            renovationRecordService.addRenovationRecord(record);
        }

        default1Renovation1 = renovationRecordService.addRenovationRecord(
                new RenovationRecord(default1,
                        "Jack Erskine revamp",
                        "CSSE building => palace of slay",
                        defaultJERooms
                )
        );
    }



    private void setupDefaultRenovationTasks() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        RenovationTaskDTO renovationTask = new RenovationTaskDTO(
                "Build Fabian monument",
                "at least 100 feet high, pokes out the top of the building",
                LocalDate.now().plusYears(5).format(formatter),
                List.of(defaultJERooms.get(2))
        );
        renovationTaskService.addRenovationTask(renovationTask, default2Renovation1);


        //Counter is to make multiple tasks on the same day; also so that every so often a
        //task generates with a longer name to test text wrapping of task bubble
        int counter = 1;
        for (int i = 1; i < numGenericTasksToAdd + 1; i++) {
            if (counter > 18) {
                counter = 3;
                renovationTask.setName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            }
            else {
                renovationTask.setName(String.format("Renovation Task %d: ", i));
            }
            renovationTask.setDescription(String.format("Renovation Task Description %d", i));
            renovationTask.setDueDate(LocalDate.now().plusDays(counter).format(formatter));
            renovationTask.setRooms(defaultJERooms);
            renovationTaskService.addRenovationTask(renovationTask, default1Renovation1);
            counter += 1;
        }
    }

    // ChatGPT was used to generate this list of tags:
    // Prompt: generate me three tags er letter of the English alphabet that are related to renovations
    private void setupDefaultTags() {
        tagService.createTag("Architecture");
        tagService.createTag("Additions");
        tagService.createTag("Asbestos");
        tagService.createTag("Basement");
        tagService.createTag("Blueprints");
        tagService.createTag("Brickwork");
        tagService.createTag("Cabinetry");
        tagService.createTag("Construction");
        tagService.createTag("CAD");
        tagService.createTag("Demolition");
        tagService.createTag("Drywall");
        tagService.createTag("Decking");
        tagService.createTag("Electrical");
        tagService.createTag("Energy Efficiency");
        tagService.createTag("Excavation");
        tagService.createTag("Flooring");
        tagService.createTag("Framing");
        tagService.createTag("Fixtures");
        tagService.createTag("Gutters");
        tagService.createTag("Garage");
        tagService.createTag("Grouting");
        tagService.createTag("HVAC");
        tagService.createTag("Hardwood");
        tagService.createTag("Historic");
        tagService.createTag("Insulation");
        tagService.createTag("Interior Design");
        tagService.createTag("Installation");
        tagService.createTag("Joists");
        tagService.createTag("Jackhammering");
        tagService.createTag("Jambs");
        tagService.createTag("Kitchen Remodel");
        tagService.createTag("Knockdown Texture");
        tagService.createTag("Knobs");
        tagService.createTag("Landscaping");
        tagService.createTag("LoadBearing");
        tagService.createTag("Lighting");
        tagService.createTag("Masonry");
        tagService.createTag("Modernization");
        tagService.createTag("Materials");
        tagService.createTag("Nailing");
        tagService.createTag("New Construction");
        tagService.createTag("Noise Reduction");
        tagService.createTag("Open Concept");
        tagService.createTag("Overhang");
        tagService.createTag("Outdoor Living");
        tagService.createTag("Plumbing");
        tagService.createTag("Paint");
        tagService.createTag("Permits");
        tagService.createTag("Quartz Countertops");
        tagService.createTag("Quality Control");
        tagService.createTag("Quick Dry");
        tagService.createTag("Renovation");
        tagService.createTag("Roofing");
        tagService.createTag("Restoration");
        tagService.createTag("Siding");
        tagService.createTag("Skylights");
        tagService.createTag("Subflooring");
        tagService.createTag("Tiling");
        tagService.createTag("Trim Work");
        tagService.createTag("Texture");
        tagService.createTag("Upgrades");
        tagService.createTag("Underlayment");
        tagService.createTag("Utility Room");
        tagService.createTag("Vinyl Flooring");
        tagService.createTag("Ventilation");
        tagService.createTag("Value");
        tagService.createTag("Windows");
        tagService.createTag("Waterproofing");
        tagService.createTag("Wall Removal");
        tagService.createTag("Xeriscaping");
        tagService.createTag("X-Bracing");
        tagService.createTag("XPS Insulation");
        tagService.createTag("Yard Renovation");
        tagService.createTag("Yellow Paint");
        tagService.createTag("Yield Improvements");
        tagService.createTag("Zoning");
        tagService.createTag("Z-Flashing");
        tagService.createTag("Zero Energy");
    }
}
