package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.dto.RubricDTO;
import com.fypgrading.adminservice.service.RubricService;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/rubrics")
public class RubricController {

    private final RubricService rubricService;

    public RubricController(RubricService rubricService) {
        this.rubricService = rubricService;
    }

    @GetMapping("/")
    public ResponseEntity<List<RubricDTO>> getRubrics() {
        List<RubricDTO> rubrics = rubricService.getRubrics();
        return ResponseEntity.ok().body(rubrics);
    }

    @GetMapping("/{assessment}")
    public ResponseEntity<List<RubricDTO>> getRubricsByAssessment(@PathVariable String assessment) {
        List<RubricDTO> rubrics = rubricService.getRubricsByAssessment(assessment);
        return ResponseEntity.ok().body(rubrics);
    }

    @PostMapping("/")
    public ResponseEntity<RubricDTO> createRubric(@RequestBody RubricDTO rubricDTO) {
        RubricDTO createdRubric = rubricService.createRubric(rubricDTO);
        return ResponseEntity.ok().body(createdRubric);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RubricDTO> updateRubric(@PathVariable Integer id, @RequestBody RubricDTO rubricDTO) {
        RubricDTO rubrics = rubricService.updateRubric(id, rubricDTO);
        return ResponseEntity.ok().body(rubrics);
    }

    @PutMapping("/{assessment}")
    public ResponseEntity<List<RubricDTO>> updateRubricsByAssessment(@PathVariable AssessmentEnum assessment,
                                                                     @RequestBody List<RubricDTO> rubricDTOList) {
        List<RubricDTO> rubrics = rubricService.updateRubricsByAssessment(assessment, rubricDTOList);
        return ResponseEntity.ok().body(rubrics);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RubricDTO> deleteRubric(@PathVariable Integer id) {
        RubricDTO rubrics = rubricService.deleteRubric(id);
        return ResponseEntity.ok().body(rubrics);
    }
}
