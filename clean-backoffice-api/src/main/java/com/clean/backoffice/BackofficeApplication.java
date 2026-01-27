package com.clean.backoffice;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableAspectJAutoProxy
@ComponentScans({
		// Controllers
		@ComponentScan(basePackages = "${com.clean.controller.packages:com.clean.backoffice}", includeFilters = {
				@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class),
				@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = RestController.class) }, useDefaultFilters = false),

		// Services
		@ComponentScan(basePackages = "${com.clean.service.packages:com.clean.backoffice}", includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Service.class), useDefaultFilters = false),

		// Components + Configurations (facades, mappers, utilities, configs)
		@ComponentScan(basePackages = "${com.clean.component.packages:com.clean.backoffice}", includeFilters = {
				@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Component.class),
				@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class) }, useDefaultFilters = false) })

// Entities & Repositories
// Note: @EntityScan is not needed in Spring Boot 4.x - entities are auto-detected in sub-packages
@EnableJpaRepositories(basePackages = "${com.clean.repo.packages:com.clean.backoffice.dao}")
public class BackofficeApplication extends SpringBootServletInitializer {

	private static final Logger logger = LoggerFactory.getLogger(BackofficeApplication.class);

	public static void main(String[] args) {
		logger.info("Backoffice Application booting up on {}", LocalDateTime.now());
		SpringApplication.run(BackofficeApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(BackofficeApplication.class);
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/** Log all effective environment properties for debugging */
	@Bean
	public ApplicationRunner verifyEnv(org.springframework.core.env.Environment env) {
		return args -> {
			logger.info("-----------------------------------------------------------");
			logger.info("Loaded package scan configuration:");
			logger.info("com.clean.controller.packages = {}", env.getProperty("com.clean.controller.packages"));
			logger.info("com.clean.service.packages    = {}", env.getProperty("com.clean.service.packages"));
			logger.info("com.clean.component.packages  = {}", env.getProperty("com.clean.component.packages"));
			logger.info("com.clean.entity.packages     = {}", env.getProperty("com.clean.entity.packages"));
			logger.info("com.clean.repo.packages       = {}", env.getProperty("com.clean.repo.packages"));
			logger.info("-----------------------------------------------------------");
		};
	}
}
