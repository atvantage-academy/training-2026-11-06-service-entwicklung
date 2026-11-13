package de.schulung.cucumber;

import io.quarkiverse.cucumber.CucumberOptions;
import io.quarkiverse.cucumber.CucumberQuarkusTest;

@CucumberOptions(
  features = "classpath:features",
  glue = "de.schulung.cucumber",
  plugin = {
    "pretty",
    "html:target/cucumber-reports/cucumber.html",
    "json:target/cucumber-reports/cucumber.json"
  }
)
public class CustomersApiCucumberTests extends CucumberQuarkusTest {
}
