package com.ns.solve.repository;

import com.ns.solve.domain.entity.user.Affiliation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AffiliationRepository extends JpaRepository<Affiliation, Long> {
    List<Affiliation> findByNameContaining(String name);
    List<Affiliation> findByNameContainingAndType(String name, String type);
    List<Affiliation> findByNameContainingAndTypeIn(String query, List<String> types);

}
