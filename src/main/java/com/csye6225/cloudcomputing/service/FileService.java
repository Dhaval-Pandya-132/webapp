package com.csye6225.cloudcomputing.service;

import com.csye6225.cloudcomputing.DataRepository.FileRepository;
import com.csye6225.cloudcomputing.Models.FileModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FileService {

    @Autowired
    FileRepository fr;

    public void saveFile(FileModel fm) {
        fr.save(fm);
    }

    public FileModel getFileByID(UUID fileId) {
        Optional<FileModel> fm = fr.findById(fileId);
        return fm.get();
    }

    public void deleteFile(FileModel fm) {
        fr.delete(fm);
    }

    public List<FileModel> getFilesByQuestionID(UUID questionId) {
        return fr.findAll()
                .stream()
                .filter(object -> object
                        .getQuestionId()
                        .getQuestionId()
                        .toString().equalsIgnoreCase(questionId.toString())
                )
                .collect(Collectors.toList());
    }

    public List<FileModel> getFilesByQuestionIDAndAnswerId(UUID questionId, UUID answerId) {

        List<FileModel> output = new ArrayList<>();
        for (FileModel fileModel :
                fr.findAll()) {

            if ( fileModel.getAnswerId() != null &&
                    fileModel.getQuestionId()
                    .getQuestionId()
                    .toString()
                    .equalsIgnoreCase(questionId.toString())
                    &&
                    fileModel.getAnswerId()
                            .getAnswerId()
                            .toString()
                            .equalsIgnoreCase(answerId.toString())

            ) {
                output.add(fileModel);
            }

        }

        return output;
    }

}
