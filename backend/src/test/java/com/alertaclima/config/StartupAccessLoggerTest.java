package com.alertaclima.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;

class StartupAccessLoggerTest {

    @Test
    void logAccessUrlsPrintsWithoutError() {
        StartupAccessLogger logger = new StartupAccessLogger();
        ReflectionTestUtils.setField(logger, "serverPort", 8080);
        ReflectionTestUtils.setField(logger, "activeProfiles", "seed");

        assertThatCode(logger::logAccessUrls).doesNotThrowAnyException();
    }
}
