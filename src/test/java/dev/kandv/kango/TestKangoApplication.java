package dev.kandv.kango;

import org.springframework.boot.SpringApplication;

public class TestKangoApplication {

	public static void main(String[] args) {
		SpringApplication.from(KangoApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
