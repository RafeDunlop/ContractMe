package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.service.ContractorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class ContractorServiceIntegrationTest {

    @Autowired
    private ContractorService toTest;

    @Autowired
    private ContractorRepository contractorRepository;

    private UserRegisterDTO userRegisterDTO;

    private AddressDTO addressDTO;

    private List<Skill> userRegisterDtoSkills;

    @Autowired
    private ContractorService contractorService;

    @BeforeEach
    void setUp() {
        userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setFirstName("John");
        userRegisterDTO.setLastName("Doe");
        userRegisterDTO.setEmail("john.doe@gmail.com");
        userRegisterDTO.setPassword("P4$$word");
        userRegisterDTO.setConfirmPassword("P4$$word");
        userRegisterDTO.setHourlyRate(30.0f);
        userRegisterDtoSkills = new ArrayList<>();
        userRegisterDTO.setSkills(userRegisterDtoSkills);
        userRegisterDTO.setPhoneNumber("0800111111l");

        addressDTO = new AddressDTO();
        addressDTO.setAddress_line1("123 Main St");
        addressDTO.setCity("Christchurch");
        addressDTO.setCountry("New Zealand");
        addressDTO.setPostcode("8042");
        addressDTO.setRegion("Avonhead");
    }

    @Test
    public void saveContractor_allValid_contractorSavedAndAllFieldsCorrect() {
        contractorService.registerContractor(userRegisterDTO,addressDTO);
        assertTrue(contractorRepository.findByEmailIgnoreCase("john.doe@gmail.com").isPresent());
    }


}
