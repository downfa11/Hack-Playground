package com.ns.solve.service.problem;

import com.ns.solve.domain.dto.problem.assignment.CaseDto;
import com.ns.solve.repository.problem.testcase.CaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseService {
    private final String NOT_FOUND_ID_ERROR_MESSAGE = "Not Found this Index.";

    private final CaseRepository caseRepository;

    public void registerCase(CaseDto caseDto) {
        log.info("Case 등록: " + caseDto);
    }

    public void updateCase(Long assignmentId, Long caseId, CaseDto caseDto) {
        log.info("Case 업데이트: ID = " + assignmentId);
    }

    public void deleteCase(Long assignmentId, Long caseId) {
        log.info("Case 삭제: ID = " + assignmentId);
    }

    public void findCaseList(Long assignmentId) {
        log.info("Case 검색: ID = " + assignmentId);
    }

}
