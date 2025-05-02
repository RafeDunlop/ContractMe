package nz.ac.canterbury.seng302.homehelper.util;

import java.io.IOException;

public class EnvVarUtil {


    public String retrieveEnvironmentVariable(String env) throws IOException {

        String value = System.getenv(env);
        if (value != null) {
            return String.format("%s=%s%n",
                    env, value);
        } else {
            return String.format("%s is"
                    + " not assigned.%n", env);
        }

    }
}
