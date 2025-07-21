package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.ContractorService;
import nz.ac.canterbury.seng302.homehelper.service.RegisterService;
import nz.ac.canterbury.seng302.homehelper.validation.ContractorValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class ContractorServiceIntegrationTest {

    private ContractorService toTest;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private RegisterService registerService;
    @Autowired
    private ContractorValidation contractorValidation;

    private UserRegisterDTO userRegisterDTO;

    private UserRegisterDTO invalidUserRegisterDTO;

    private AddressDTO addressDTO;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        contractorRepository.deleteAll();
        userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setFirstName("John");
        userRegisterDTO.setLastName("Doe");
        userRegisterDTO.setEmail("john.doe@gmail.com");
        userRegisterDTO.setPassword("P4$$word");
        userRegisterDTO.setConfirmPassword("P4$$word");
        userRegisterDTO.setHourlyRate(30.0f);
        List<Skill> userRegisterDtoSkills = new ArrayList<>();
        userRegisterDTO.setSkills(userRegisterDtoSkills);
        userRegisterDTO.setPhoneNumber("0800111111");
        userRegisterDTO.setCountryCode(64);

        invalidUserRegisterDTO = new UserRegisterDTO();
        invalidUserRegisterDTO.setFirstName("John");
        invalidUserRegisterDTO.setLastName("Doe");
        invalidUserRegisterDTO.setEmail("john.doe@gmail.com");
        invalidUserRegisterDTO.setPassword("P4$$word");
        invalidUserRegisterDTO.setConfirmPassword("P4$$word");
        invalidUserRegisterDTO.setHourlyRate(-3.00f);
        invalidUserRegisterDTO.setSkills(userRegisterDtoSkills);
        invalidUserRegisterDTO.setPhoneNumber("0");

        addressDTO = new AddressDTO();
        addressDTO.setAddress_line1("123 Main St");
        addressDTO.setCity("Christchurch");
        addressDTO.setCountry("New Zealand");
        addressDTO.setPostcode("8042");
        addressDTO.setRegion("Avonhead");

        toTest = new ContractorService(contractorRepository, contractorValidation);


    }

    @Test
    public void saveContractor_allValid_contractorSavedAndAllFieldsCorrect() {
        toTest.registerContractor(userRegisterDTO, addressDTO);
        assertTrue(contractorRepository.findByEmailIgnoreCase("john.doe@gmail.com").isPresent());
    }

    @Test
    public void validateContractor_invalidContractorDetails_throwsError() {

        Map<String, List<String>> errors = toTest.validateContractor(invalidUserRegisterDTO);
        assertTrue(errors.containsKey("phoneNumberError"));
        assertEquals("Your phone number is invalid", errors.get("phoneNumberError").get(0));

        assertTrue(errors.containsKey("hourlyRateError"));
        assertEquals("Invalid hourly rate", errors.get("hourlyRateError").get(0));

    }

    @Test
    public void validateContractor_validContractorDetails_noErrors() {
        Map<String, List<String>> errors = toTest.validateContractor(userRegisterDTO);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void saveContractor_allValid_contractorSavedInUserRepository() {
        toTest.registerContractor(userRegisterDTO, addressDTO);
        assertTrue(userRepository.findByEmailIgnoreCase("john.doe@gmail.com").isPresent());
    }

}
