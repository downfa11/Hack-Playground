package com.ns.solve.controller.core;

import com.ns.solve.service.core.GitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
public class GitController {
    private final GitService gitService;

    @GetMapping("/clone")
    public ResponseEntity<String> cloneGitRepository(@RequestParam String repoUrl){
        try {
            gitService.cloneRepository(repoUrl);
            return ResponseEntity.ok("Repository cloned successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to clone repository: " + e.getMessage());
        }
    }

    @GetMapping("/delete")
    public ResponseEntity<String> deleteGitRepository(@RequestParam String repoName){
        try {
            gitService.deleteDirectory(repoName);
            return ResponseEntity.ok("Repository directory deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete repository: " + e.getMessage());
        }
    }
}
