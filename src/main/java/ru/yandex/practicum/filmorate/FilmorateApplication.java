package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/* Привет, Кирилл!
Я не стала делать абстрактный контроллер и наследовать от него два других, мне показалось, что это не сильно хорошая идея.
 */
@SpringBootApplication
public class FilmorateApplication {

	public static void main(String[] args) {
		SpringApplication.run(FilmorateApplication.class, args);
	}

}
