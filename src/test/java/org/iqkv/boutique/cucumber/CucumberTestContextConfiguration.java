package org.iqkv.boutique.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.iqkv.boutique.IntegrationTest;
import org.springframework.test.context.web.WebAppConfiguration;

@CucumberContextConfiguration
@IntegrationTest
@WebAppConfiguration
public class CucumberTestContextConfiguration {}
