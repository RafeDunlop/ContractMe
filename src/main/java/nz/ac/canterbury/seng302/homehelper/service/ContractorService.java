package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.ContractorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * Service for the Contractor entity type.
 */
@Service
public class ContractorService {

    private final ContractorRepository contractorRepository;

    private final RegisterService registerService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor for the service and links the repository and validator to the
     * service.
     * @param contractorRepository ContractorRepository for getting and updating contractor details
     * @param registerService RegisterService for registering Contractors
     */
    @Autowired
    public ContractorService(ContractorRepository contractorRepository, RegisterService registerService) {
        this.registerService = registerService;
        this.contractorRepository = contractorRepository;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }



    /**
     * Create a contractor and save it to the database
     * @param userRegisterDTO Data transfer object for contractor registration
     * @param addressDTO Data transfer object for contractor registration
     * @return the contractor if it was saved successfully
     * @throws IllegalArgumentException if the invalid fields
     */
    public Contractor registerContractor(UserRegisterDTO userRegisterDTO, AddressDTO addressDTO) {
        //validation here (throw error with map for specific errors)
        Contractor contractor = new Contractor(
                userRegisterDTO.getFirstName(),
                userRegisterDTO.getLastName(),
                userRegisterDTO.getEmail(),
                passwordEncoder.encode(userRegisterDTO.getPassword())
        );
        contractor.setHourlyRate(userRegisterDTO.getHourlyRate());
        contractor.setPhoneNumber(userRegisterDTO.getPhoneNumber());
        userRegisterDTO.getSkills().forEach(contractor::addSkill);
        //address validation here
        Location location = new Location(
                addressDTO.getAddress_line1(),
                addressDTO.getCountry(),
                addressDTO.getPostcode(),
                addressDTO.getCity(),
                addressDTO.getRegion()
        );
        contractor.setLocation(location);
        contractor = contractorRepository.save(contractor);
        return contractor;
    }
}
