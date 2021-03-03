package com.app.siget.cucumber;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)

@CucumberOptions(features = "src/test/java/features", // archivo feature de login y register
		glue = "com.app.siget.cucumber.pruebas", // paquete
		plugin = { "pretty", "html:target/cucumber-html-report", "json:target/cucumber.json" }

)

public class ejecutor {

}
