package com.ns.solve.service;

import com.ns.solve.domain.dto.user.AffiliationDto;
import com.ns.solve.domain.entity.user.Affiliation;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.repository.AffiliationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AffiliationService {

    private final AffiliationRepository affiliationRepository;

    @Transactional(readOnly = true)
    public List<AffiliationDto> searchAffiliations(String query, String type) {
        List<Affiliation> affiliations;

        if (type != null && !type.isEmpty()) {
            affiliations = affiliationRepository.findByNameContainingAndType(query, type);
        } else {
            affiliations = affiliationRepository.findByNameContaining(query);
        }

        return affiliations.stream()
                .map(AffiliationDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserAffiliations(User user, List<Long> affiliationIds) {
        user.getAffiliations().clear();

        Set<Affiliation> newAffiliations = affiliationIds.stream()
                .map(id -> affiliationRepository.findById(id).orElse(null))
                .collect(Collectors.toSet());

        if (newAffiliations.contains(null)) {
            newAffiliations.remove(null);
        }

        user.setAffiliations(newAffiliations);
    }
}