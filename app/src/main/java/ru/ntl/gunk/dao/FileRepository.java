package ru.ntl.gunk.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ntl.gunk.dao.models.File;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Integer> {

    Optional<File> findByName(String fileName);
    Optional<File> deleteByName(String fileName);
}
