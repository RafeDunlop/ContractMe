package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.ContractorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ContractorService {

    private final ContractorRepository contractorRepository;

    private final RegisterService registerService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ContractorService(ContractorRepository contractorRepository, RegisterService registerService) {
        this.registerService = registerService;
        this.contractorRepository = contractorRepository;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    public Contractor registerContractor(UserRegisterDTO userRegisterDTO, AddressDTO addressDTO) {
        //validation here (throw error with map for specific errors)
        Contractor contractor = new Contractor();
        contractor.setFirstName(userRegisterDTO.getFirstName());
        contractor.setLastName(userRegisterDTO.getLastName());
        contractor.setEmail(userRegisterDTO.getEmail());
        contractor.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        contractor.setHourlyRate(userRegisterDTO.getHourlyRate());
        contractor.setPhoneNumber(userRegisterDTO.getPhoneNumber());
        userRegisterDTO.getSkills().forEach(contractor::addSkill);
        contractor = contractorRepository.save(contractor);
        //address validation here
        registerService.registerLocation(contractor, addressDTO);
        return contractor;
    }
}
