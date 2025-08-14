package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.persistence.EntityNotFoundException;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.util.MapUtil;
import nz.ac.canterbury.seng302.homehelper.validation.ContractorValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Service for the Contractor entity type.
 */
@Service
public class ContractorService {

    private final ContractorRepository contractorRepository;

    private final PasswordEncoder passwordEncoder;
    private final ContractorValidation contractorValidation;
    private final LocationService locationService;

    /**
     * Constructor for the service and links the repository and validator to the
     * service.
     * @param contractorRepository ContractorRepository for getting and updating contractor details
     */
    @Autowired
    public ContractorService(ContractorRepository contractorRepository, ContractorValidation contractorValidation, LocationService locationService) {
        this.contractorRepository = contractorRepository;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        this.contractorValidation = contractorValidation;
        this.locationService = locationService;
    }



    /**
     * Create a contractor and save it to the database
     * @param userRegisterDTO Data transfer object for contractor registration
     * @param addressDTO Data transfer object for contractor registration
     * @return the contractor if it was saved successfully
     * @throws IllegalArgumentException if the invalid fields
     */
    public Contractor registerContractor(UserRegisterDTO userRegisterDTO, AddressDTO addressDTO) {
        Contractor contractor = new Contractor(
                userRegisterDTO.getFirstName(),
                userRegisterDTO.getLastName(),
                userRegisterDTO.getEmail(),
                passwordEncoder.encode(userRegisterDTO.getPassword())
        );
        BigDecimal hourlyRateDecimal = BigDecimal.valueOf(userRegisterDTO.getHourlyRate());
        BigDecimal roundedHourlyRate = hourlyRateDecimal.setScale(2, RoundingMode.HALF_UP);

        contractor.setHourlyRate(roundedHourlyRate.floatValue());
        contractor.setPhoneNumber(userRegisterDTO.getPhoneNumber());
        contractor.setCountryCode(userRegisterDTO.getCountryCode());
        userRegisterDTO.getSkills().forEach(contractor::addSkill);
        contractor.setLocation(locationService.locate(addressDTO));
        contractor = contractorRepository.save(contractor);
        return contractor;
    }

    /**
     * Validates the contractor specific registration fields provided in the {@link UserRegisterDTO}
     *
     * @param userRegisterDTO the user registration data transfer object containing user input fields
     * @return a map of field name and error lists. If no errors exist for a field, it is not included
     */
    public Map<String, List<String>> validateContractor(UserRegisterDTO userRegisterDTO, boolean locationProvided) {
        Map<String, List<String>> errors = new HashMap<>();

        MapUtil.putIfNotEmpty(errors, "phoneNumberError", contractorValidation.validatePhoneNumber(
                userRegisterDTO.getPhoneNumber(), userRegisterDTO.getCountryCode()));
        MapUtil.putIfNotEmpty(errors, "hourlyRateError", contractorValidation.validateHourlyRate(userRegisterDTO.getHourlyRate()));
        MapUtil.putIfNotEmpty(errors, "locationError", contractorValidation.validateContractorLocation(locationProvided));
        MapUtil.putIfNotEmpty(errors, "skillsError", contractorValidation.validateContractorSkillsField(userRegisterDTO.getSkills()));
        return errors;
    }

    public Contractor getContractorById(long userId) {
        return contractorRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Contractor: " + userId + " not found"));
    }
}
