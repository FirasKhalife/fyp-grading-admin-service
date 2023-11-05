package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.dto.RubricDTO;
import com.fypgrading.adminservice.service.RubricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<RubricDTO> deleteRubric(@PathVariable Integer id) {
        RubricDTO rubrics = rubricService.deleteRubric(id);
        return ResponseEntity.ok().body(rubrics);
    }
}
