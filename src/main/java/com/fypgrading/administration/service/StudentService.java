package com.fypgrading.administration.service;

import com.fypgrading.administration.entity.Student;
import com.fypgrading.administration.repository.StudentRepository;
import com.fypgrading.administration.service.dto.StudentDTO;
import com.fypgrading.administration.service.mapper.StudentMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public StudentService(StudentRepository studentRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }

    public List<StudentDTO> getStudents() {
        List<Student> students = studentRepository.findAll();
        return studentMapper.toDTOList(students);
    }

    public StudentDTO createStudent(StudentDTO studentDTO) {
        Student student = studentMapper.toEntity(studentDTO);
        Student createdEntity = studentRepository.save(student);
        return studentMapper.toDTO(createdEntity);
    }

    public StudentDTO updateStudent(Integer id, StudentDTO studentDTO) {
        getStudentById(id);
        Student student = studentMapper.toEntity(studentDTO);
        Student updatedEntity = studentRepository.save(student);
        return studentMapper.toDTO(updatedEntity);
    }

    public StudentDTO deleteStudent(Integer id) {
        Student student = getStudentById(id);
        studentRepository.delete(student);
        return studentMapper.toDTO(student);
    }

    private Student getStudentById(Integer id) {
        return studentRepository.findById(id).orElseThrow(() ->
                        new EntityNotFoundException("Student not found"));
    }
}
