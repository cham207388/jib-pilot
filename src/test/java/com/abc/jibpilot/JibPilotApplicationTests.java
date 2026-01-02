package com.abc.jibpilot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
class JibPilotApplicationTests {

    @Test
    void contextLoads() {
    }

}
