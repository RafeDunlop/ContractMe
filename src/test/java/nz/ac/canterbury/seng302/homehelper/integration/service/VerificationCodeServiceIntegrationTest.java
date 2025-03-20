package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.service.RegisterService;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;

@DataJpaTest
@Import(VerificationCodeService.class)
public class VerificationCodeServiceIntegrationTest {

    @Autowired
    private RegisterService registerService;

    @Autowired
    private VerificationCodeService toTest;

    private static final long seed = 1234;

    private static final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private static final int timeQuantity = 100;
}
