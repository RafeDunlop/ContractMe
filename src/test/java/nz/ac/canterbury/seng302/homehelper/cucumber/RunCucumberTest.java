package nz.ac.canterbury.seng302.homehelper.cucumber;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import io.cucumber.spring.CucumberContextConfiguration;
import nz.ac.canterbury.seng302.homehelper.HomeHelperApplication;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "nz.ac.canterbury.seng302.homehelper.cucumber")
@ContextConfiguration(classes = HomeHelperApplication.class)
@CucumberContextConfiguration
@SpringBootTest(classes = HomeHelperApplication.class)
@ActiveProfiles("cucumber")
public class RunCucumberTest {
}
