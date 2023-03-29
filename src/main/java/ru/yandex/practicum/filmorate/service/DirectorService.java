package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
public class DirectorService {

    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Director getDirectorById(long id) {
        if (!directorStorage.directorExists(id)) {
            throw new NotFoundException("Director not found");
        }
        return directorStorage.getDirectorById(id);
    }

    public List<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director addDirector(Director director) {
        validate(director);
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        if (!directorStorage.directorExists(director.getId())) {
            throw new NotFoundException("Director not found");
        }
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(long id) {
        if (!directorStorage.directorExists(id)) {
            throw new NotFoundException("Director not found");
        }
        directorStorage.deleteDirector(id);
    }

    private void validate(Director director) {
        if (director.getName().isBlank() || director.getName().isEmpty()) {
            throw new ValidationException("Director name is empty or blank");
        }
    }
}
