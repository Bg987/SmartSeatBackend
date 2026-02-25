package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.DTO.StudentsDTO;
import com.example.SmartSeatBackend.entity.Students;
import com.example.SmartSeatBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;




@Repository
public interface StudentRepository extends JpaRepository<Students, String> {

    List<Students>findByCollegeId(Long collegeId);

    @Query("""
SELECT s FROM Students s
WHERE s.collegeId = :collegeId
AND :subject NOT MEMBER OF s.backlogSubjects
""")
    List<Students> findStudentsWithoutBacklog(
            @Param("collegeId") Long collegeId,
            @Param("subject") String subject
    );
    Optional<Students> findByEmail(String mail);

    @Modifying //for update/delete queries
    @Transactional //allow database modification
    @Query("UPDATE Students s SET s.imgUrl = :url WHERE s.studentId = :id")
    int ProfilePic(@Param("id") Long id, @Param("url") String url);

    // Check if the profilePicUrl is NOT null or empty for a student
    @Query("SELECT CASE WHEN s.imgUrl IS NOT NULL AND s.imgUrl != '' THEN true ELSE false END " +
            "FROM Students s WHERE s.studentId = :id")
    boolean existsProfilePic(@Param("id") Long id);

    @Query("SELECT s.studentId FROM Students s WHERE s.enrollmentNo = :stuEnId AND s.collegeId = :collId")
    Optional<Long> findStudentIdByEnrollmentAndCollege(@Param("stuEnId") String stuEnId, @Param("collId") Long collId);

 Long countByCollegeId(Long collegeId);

}
