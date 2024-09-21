package com.fypgrading.adminservice.service.client;

import com.fypgrading.adminservice.service.dto.EvaluationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
    name = "${services.evaluation-service-name}",
    path = "/api",
    fallback = EvaluationFallback.class
)
public interface EvaluationClient {

    @GetMapping("/evaluations/{assessment}/{teamId}")
    List<EvaluationDTO> getTeamEvaluationsByAssessment(@PathVariable String assessment, @PathVariable Long teamId);

}
