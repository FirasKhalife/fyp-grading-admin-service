package com.fypgrading.administration.service;

import com.fypgrading.administration.service.dto.RubricDTO;
import com.fypgrading.administration.entity.Rubric;
import com.fypgrading.administration.service.mapper.RubricMapper;
import com.fypgrading.administration.repository.RubricRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RubricService {

    private final RubricRepository rubricRepository;
    private final RubricMapper rubricMapper;

    public RubricService(RubricRepository rubricRepository, RubricMapper rubricMapper) {
        this.rubricRepository = rubricRepository;
        this.rubricMapper = rubricMapper;
    }

    public List<RubricDTO> getRubrics() {
        List<Rubric> rubrics = rubricRepository.findAll();
        return rubricMapper.toDTOList(rubrics);
    }

    public RubricDTO createRubric(RubricDTO rubricDTO) {
        Rubric rubric = rubricMapper.toEntity(rubricDTO);
        Rubric createdEntity = rubricRepository.save(rubric);
        return rubricMapper.toDTO(createdEntity);
    }

    public RubricDTO updateRubric(Integer id, RubricDTO rubricDTO) {
        getRubricById(id);
        Rubric rubric = rubricMapper.toEntity(rubricDTO);
        rubric.setId(id);
        Rubric updatedEntity = rubricRepository.save(rubric);
        return rubricMapper.toDTO(updatedEntity);
    }

    public RubricDTO deleteRubric(Integer id) {
        Rubric rubric = getRubricById(id);
        rubricRepository.delete(rubric);
        return rubricMapper.toDTO(rubric);
    }

    private Rubric getRubricById(Integer id) {
        return rubricRepository.findById(id).orElseThrow(() ->
                        new EntityNotFoundException("Rubric not found"));
    }
}
