package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;



public interface SubjectRepository  extends JpaRepository<Subject, String> {

    List<Subject> findByDepartmentAndBranchAndSemester(
            String department,
            String branch,
            Integer semester
    );
}
