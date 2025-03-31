package com.ns.solve.service;


import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileService {
    private final Path fileStorageLocation = Paths.get("uploads/problems").toAbsolutePath().normalize();

    public String uploadFile(Long problemId, MultipartFile file) {
        try {
            Files.createDirectories(fileStorageLocation);
            String fileName = problemId + "_" + file.getOriginalFilename();
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path targetPath = fileStorageLocation.resolve(filePath);
            Files.deleteIfExists(targetPath);
        } catch (Exception e) {
            throw new RuntimeException("File deletion failed: " + filePath, e);
        }
    }

    public Resource downloadFile(String filePath) {
        try {
            Path fileLocation = fileStorageLocation.resolve(filePath);
            Resource resource = new UrlResource(fileLocation.toUri());
            if (!resource.exists()) {
                throw new FileNotFoundException("File not found: " + fileLocation);
            }
            return resource;
        } catch (Exception e) {
            throw new RuntimeException("File download failed", e);
        }
    }
}

