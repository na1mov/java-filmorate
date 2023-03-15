package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorDbStorage;

    public Director create(Director director) {
        return directorDbStorage.create(director);
    }

    public Director update(Director director) {
        return directorDbStorage.update(director);
    }

    public Director delete(Integer directorId) {
        return directorDbStorage.delete(directorId);
    }

    public Director getDirector(Integer directorId) {
        return directorDbStorage.getDirector(directorId);
    }

    public Collection<Director> getDirectors() {
        return directorDbStorage.getDirectors();
    }
}
