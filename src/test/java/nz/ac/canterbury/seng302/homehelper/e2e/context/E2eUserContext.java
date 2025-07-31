package nz.ac.canterbury.seng302.homehelper.e2e.context;

import io.cucumber.spring.ScenarioScope;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class E2eUserContext {

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
