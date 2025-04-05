package com.ns.solve.controller.core;

import com.ns.solve.service.core.DockerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/docker")
public class DockerController {
    private final DockerService dockerService;

    @GetMapping("/build")
    public ResponseEntity<String> buildDockerImage(@RequestParam String repoName){
        return ResponseEntity.ok(dockerService.buildImage(repoName));
    }

    @GetMapping("/push")
    public ResponseEntity<String> pushECRImage(@RequestParam String repoName){
        return ResponseEntity.ok(dockerService.pushImage(repoName));
    }

}
