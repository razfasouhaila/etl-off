package com.etl.off;

import com.etl.off.service.ProductETLService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EtlOffApplication implements CommandLineRunner {

	private final ProductETLService etlService;

	public EtlOffApplication(ProductETLService etlService) {
		this.etlService = etlService;
	}

	public static void main(String[] args) {
		SpringApplication.run(EtlOffApplication.class, args);
	}

	@Override
	public void run(String... args) {
		etlService.runETL("src/main/resources/open-food-facts.csv");
	}
}
