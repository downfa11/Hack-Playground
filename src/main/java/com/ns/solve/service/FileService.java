package com.ns.solve.service;

import com.ns.solve.domain.vo.FileInfo;
import com.ns.solve.utils.exception.ErrorCode.FileErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;

import java.util.List;

@Service
public class FileService {
    private static final List<String> ALLOWED_EXTENSIONS = List.of("pdf", "txt", "md", "png", "jpg","zip", "gzip", "gz", "tar", "tgz", "yaml", "yml", "c", "cpp", "go", "java", "h", "py", "sh", "js"); // 허용 확장자

    @Value("${file.upload.dir}")
    private String uploadDir;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException e) {
            throw new SolvedException(FileErrorCode.FILE_UPLOAD_ERROR, uploadDir);
        }
    }

    public FileInfo uploadFile(Long problemId, MultipartFile file) {
        try {
            Files.createDirectories(fileStorageLocation);

            String originalFileName = sanitizeFilename(file.getOriginalFilename());
            String extension = FilenameUtils.getExtension(originalFileName).toLowerCase();

            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new SolvedException(FileErrorCode.INVALID_FILE_TYPE);
            }

            String fileName = problemId + "_" + originalFileName;
            Path targetLocation = fileStorageLocation.resolve(fileName).normalize();

            if (!targetLocation.startsWith(fileStorageLocation)) {
                throw new SolvedException(FileErrorCode.FILE_UPLOAD_ERROR);
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            long fileSize = Files.size(targetLocation);

            return new FileInfo(fileName, fileSize);
        } catch (IOException e) {
            throw new SolvedException(FileErrorCode.FILE_UPLOAD_ERROR, "failed " + e);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path targetPath = validatePath(filePath);
            Files.deleteIfExists(targetPath);
        } catch (Exception e) {
            throw new SolvedException(FileErrorCode.FILE_DELETE_ERROR, filePath);
        }
    }

    public Resource downloadFile(String filePath) {
        try {
            Path fileLocation = validatePath(filePath);
            Resource resource = new UrlResource(fileLocation.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new SolvedException(FileErrorCode.FILE_NOT_FOUND, "failed " + filePath);
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new SolvedException(FileErrorCode.FILE_DOWNLOAD_ERROR, "잘못된 파일 경로");
        } catch (IOException e) {
            throw new SolvedException(FileErrorCode.FILE_DOWNLOAD_ERROR, "파일 다운로드 실패");
        }
    }

    private Path validatePath(String fileName) throws IOException {
        Path path = fileStorageLocation.resolve(sanitizeFilename(fileName)).normalize();
        if (!path.startsWith(fileStorageLocation)) {
            throw new SolvedException(FileErrorCode.FILE_UPLOAD_ERROR, "허용되지 않은 파일 경로 접근");
        }
        return path;
    }

    private String sanitizeFilename(String filename) {
        return Paths.get(filename).getFileName().toString().replaceAll("[\\s\\\\/:*?\"<>|]+", "_");
    }

    private Long getFileSize(String filePath) {
        try {
            Path targetPath = validatePath(filePath);
            return Files.size(targetPath);
        } catch (IOException e) {
            throw new SolvedException(FileErrorCode.FILE_READ_ERROR);
        }
    }
}
