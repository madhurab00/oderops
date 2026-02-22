package com.orderops;

import org.springframework.boot.SpringApplication;

public class TestOrderopsApplication {

	public static void main(String[] args) {
		SpringApplication.from(OrderopsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
