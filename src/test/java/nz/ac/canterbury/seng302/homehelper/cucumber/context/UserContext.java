package nz.ac.canterbury.seng302.homehelper.cucumber.context;

import io.cucumber.spring.ScenarioScope;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class UserContext {

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
