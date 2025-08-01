package nz.ac.canterbury.seng302.homehelper.cucumber.context;

import io.cucumber.spring.ScenarioScope;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import org.springframework.stereotype.Component;


@Component
@ScenarioScope
public class ContractorContext {

    private Contractor contractor;

    public Contractor getContractor() {
        return contractor;
    }

    public void setContractor(Contractor contractor) {
        this.contractor = contractor;
    }

}
