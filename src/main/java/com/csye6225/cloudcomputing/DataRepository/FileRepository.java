package com.csye6225.cloudcomputing.DataRepository;

import com.csye6225.cloudcomputing.Models.FileModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileModel, UUID> {
    @Override
    Optional<FileModel> findById(UUID uuid);

    @Override
    List<FileModel> findAll();

    @Override
    void delete(FileModel fileModel);
}
