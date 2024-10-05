package com.fypgrading.adminservice.service.client;

import com.fypgrading.adminservice.service.dto.EvaluationDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EvaluationFallback implements EvaluationClient {

    @Override
    public List<EvaluationDTO> getTeamEvaluationsByAssessment(String assessment, Long teamId) {
        return List.of();
    }
}
