package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface SubjectRepository  extends JpaRepository<Subject, String> {


    List<Subject> findByDepartmentAndBranchAndSemester(
            String department,
            String branch,
            Integer semester
    );

    //check at the subject insertion time
    boolean existsBySubjectIdAndBranchAndSemester(String subjectId, String branch, Integer semester);
}
