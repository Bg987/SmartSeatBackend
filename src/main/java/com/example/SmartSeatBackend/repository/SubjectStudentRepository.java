package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.SubjectStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubjectStudentRepository extends JpaRepository<SubjectStudent, Long> {

    // Find all subjects a specific student is enrolled in
    List<SubjectStudent> findByEnrollmentNo(String enrollmentNo);

    // Find all students enrolled in a specific subject
    List<SubjectStudent> findBySubjectCode(String subjectCode);


    // Check if a specific enrollment exists for a student and subject
    boolean existsByEnrollmentNoAndSubjectCode(String enrollmentNo, String subjectCode);

    @Query(value =
            "SELECT enrollment_no FROM subject_student WHERE \"subjectCode\" = :subCode " +
                    "UNION " +
                    "SELECT enrollment_no FROM back_subjects WHERE \"backlog_subjects\" = :subCode",
            nativeQuery = true)
    List<String> findTotalEligibleStudents(@Param("subCode") String subCode);
}