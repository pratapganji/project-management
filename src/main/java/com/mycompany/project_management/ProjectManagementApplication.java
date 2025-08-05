package com.mycompany.project_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectManagementApplication {

	public static void main(String[] args) {

		SpringApplication.run(ProjectManagementApplication.class, args);
	}

	@Bean
    	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
	        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
	
	        // Path to your external file
	        File externalProps = new File("/opt/configs/application.properties");
	
	        if (externalProps.exists()) {
	            configurer.setLocation(new FileSystemResource(externalProps));
	        } else {
	            throw new FileNotFoundException("External properties file not found: " + externalProps.getAbsolutePath());
	        }
	
	        configurer.setIgnoreResourceNotFound(false);
	        configurer.setIgnoreUnresolvablePlaceholders(false);
	
	        return configurer;
    	}

}
