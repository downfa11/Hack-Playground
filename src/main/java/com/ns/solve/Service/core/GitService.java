package com.ns.solve.service.core;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitService {

    @Value("${container.image.directory:C:\\Users\\jks83\\OneDrive\\문서\\GitHub\\solve\\/gitRepository}")
    public static String gitDirectory;

    public void cloneRepository(String repoUrl) throws GitAPIException {
        if (!isValidGitRepoUrl(repoUrl)) return;

        String repoName = repoUrl.substring(repoUrl.lastIndexOf('/') + 1, repoUrl.lastIndexOf('.'));
        Path fullLocalPath = Paths.get(gitDirectory, repoName);

        File repoDir = fullLocalPath.toFile();
        if (!repoDir.exists()) {
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(repoDir)
                    .call();

            log.info("Repository '" + repoName + "' cloned successfully. " + fullLocalPath);
        } else {
            log.info("Repository '" + repoName + "' already exists at " + fullLocalPath);
        }
    }

    public void deleteDirectory(String repoName) {
        Path fileDirectory = findDirectoryByRepoName(repoName);
        deleteDirectory(fileDirectory.toFile());
    }

    public void deleteDirectory(File file) {
        if (file == null || !file.exists()) {
            log.info("File or directory not exist: " + file);
            return;
        }

        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    deleteDirectory(subFile);
                }
            }
        }

        if (file.delete()) {
            log.info("Deleted: " + file.getAbsolutePath());
        } else {
            log.info("Failed to delete: " + file.getAbsolutePath());
        }
    }

    public boolean isValidGitRepoUrl(String url) {
        return url.startsWith("https://github.com/") && url.endsWith(".git");
    }

    public Path findDirectoryByRepoName(String repoName) {
        return Paths.get(gitDirectory, repoName);
    }
}
