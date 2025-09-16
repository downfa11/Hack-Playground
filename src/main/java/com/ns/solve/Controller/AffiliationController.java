package com.ns.solve.controller;

import com.ns.solve.domain.dto.user.AffiliationDto;
import com.ns.solve.service.AffiliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/affiliations")
public class AffiliationController {

    private final AffiliationService affiliationService;

    @GetMapping("/search")
    public ResponseEntity<List<AffiliationDto>> searchAffiliations(@RequestParam String query, @RequestParam(required = false) Optional<String> type) {
        List<AffiliationDto> affiliations = affiliationService.searchAffiliations(query, type.orElse(null));
        return ResponseEntity.ok(affiliations);
    }
}
