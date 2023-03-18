package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
Привет, Кирилл!

Не увидела смысла в поддержании старых классов для работы с хранилищами, поэтому просто их удалила, кроме того наставник
сказал,что это допускается.
 */
@SpringBootApplication
public class FilmorateApplication {

	public static void main(String[] args) {
		SpringApplication.run(FilmorateApplication.class, args);
	}

}
