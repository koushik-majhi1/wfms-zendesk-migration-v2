package com.byjus.zendeskMigration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ZendeskMigration {

	public static void main(String[] args) {
		SpringApplication.run(ZendeskMigration.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return JsonMapper.builder()
						 .addModule(new JavaTimeModule())
						 .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
						 .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
						 .build();
	}

}
