package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    @Autowired
    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable long id) {
        log.info("Попытка получить информацию о режиссере с id = {}", id);
        return directorService.getDirectorById(id);
    }

    @GetMapping
    public List<Director> getAllDirectors() {
        log.info("Попытка получить список всех режиссеров");
        return directorService.getAllDirectors();
    }

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director director) {
        log.info("Попытка добавить режиссера");
        return directorService.addDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("Попытка обновить режиссера с id = {}", director.getId());
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable long id) {
        log.info("Попытка удалить режиссера с id = {}", id);
        directorService.deleteDirector(id);
    }
}
