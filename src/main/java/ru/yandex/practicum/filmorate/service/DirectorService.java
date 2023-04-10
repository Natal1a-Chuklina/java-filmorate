package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Slf4j
@Service
public class DirectorService {

    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Director getDirectorById(long id) {
        return directorStorage.getDirectorById(id);
    }

    public List<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director addDirector(Director director) {
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        checkDirectorExists(director.getId());
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(long id) {
        checkDirectorExists(id);
        directorStorage.deleteDirector(id);
    }

    private void checkDirectorExists(long id) {
        if (!directorStorage.isDirectorExists(id)) {
            log.warn("Выполнена попытка получить режиссера по несуществующему id = {}", id);
            throw new NotFoundException(String.format(Constants.DIRECTOR_NOT_FOUND, id));
        }
    }
}
