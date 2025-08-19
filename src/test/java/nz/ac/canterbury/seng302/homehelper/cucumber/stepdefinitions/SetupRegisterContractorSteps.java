package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.Before;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.ContractorContext;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootTest
public class SetupRegisterContractorSteps {



    private final ContractorContext contractorContext;
    @Autowired
    private ContractorRepository contractorRepository;

    public SetupRegisterContractorSteps(ContractorContext contractorContext) {
        this.contractorContext = contractorContext;
    }

    @Before("@authoriseContractor")
    public void i_am_an_existing_contractor() {

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String uniqueEmail = "test" + System.currentTimeMillis() + "@user.nz";
        Contractor contractor = new Contractor("Test", "User", uniqueEmail, encoder.encode("Test123!"));
        Location location = new Location("20 Kirkwood Avenue", "New Zealand", "8041",
                "Christchurch", "Upper Riccarton", 1D, 1D);
        contractor.setLocation(location);
        contractor.setHourlyRate(27.50F);
        contractor.setCountryCode(64);
        contractor.setPhoneNumber("021 123 4567");
        contractor.addSkill(Skill.CARPENTRY);

        contractor.activate();
        contractor.setAvailable(true);
        contractorRepository.save(contractor);
        contractorContext.setContractor(contractor);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(contractor.getEmail(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

}
