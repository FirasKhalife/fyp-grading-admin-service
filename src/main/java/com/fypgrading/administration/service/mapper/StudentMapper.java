package com.fypgrading.administration.service.mapper;

import com.fypgrading.administration.entity.Student;
import com.fypgrading.administration.service.dto.StudentDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface StudentMapper {

    StudentDTO toDTO(Student student);

    Student toEntity(StudentDTO studentDTO);

    List<StudentDTO> toDTOList(List<Student> students);

    List<Student> toEntityList(List<StudentDTO> studentDTOs);
}
