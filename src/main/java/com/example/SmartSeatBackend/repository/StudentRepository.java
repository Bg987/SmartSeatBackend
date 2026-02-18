package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.DTO.StudentsDTO;
import com.example.SmartSeatBackend.entity.Students;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Students, String> {
    List<Students>findByCollegeId(Long collegeId);
}
