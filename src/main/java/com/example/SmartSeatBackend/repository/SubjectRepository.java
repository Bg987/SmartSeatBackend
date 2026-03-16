package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface SubjectRepository  extends JpaRepository<Subject, String> {


    List<Subject> findByDepartmentAndBranchAndSemester(
            String department,
            String branch,
            Integer semester
    );

    @Query("SELECT DISTINCT s.branch FROM Subject s")
    List<String> findDistinctBranches();

    //check at the subject insertion time
    boolean existsBySubjectIdAndBranchAndSemester(String subjectId, String branch, Integer semester);
}
