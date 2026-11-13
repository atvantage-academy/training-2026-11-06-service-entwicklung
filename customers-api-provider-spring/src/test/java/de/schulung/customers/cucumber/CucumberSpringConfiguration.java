package de.schulung.customers.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class CucumberSpringConfiguration {
}
