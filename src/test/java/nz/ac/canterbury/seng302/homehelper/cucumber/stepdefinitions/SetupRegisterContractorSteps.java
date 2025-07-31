package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.Before;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.ContractorContext;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
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
        Location location = new Location("20 Kirkwood Avenue", "New Zealand", "8041", "Christchuch", "Upper Riccarton");
        contractor.setLocation(location);

        contractor.activate();
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
