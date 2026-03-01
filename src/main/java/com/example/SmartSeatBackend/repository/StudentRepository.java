package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.Students;
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

//    @Query("SELECT s FROM Students s WHERE s.semester >= :semester AND (" +
//            "EXISTS (SELECT 1 FROM s.subjects sub WHERE sub.subjectCode = :subjectId) OR " +
//            "EXISTS (SELECT 1 FROM s.backlogSubjects back WHERE back.subjectCode = :subjectId))")
//    List<Students> findEligibleStudentsBySemesterAndSubject(
//            @Param("semester") Integer semester,
//            @Param("subjectId") String subjectId
//    );

//    @Query("SELECT DISTINCT s FROM Students s " +
//            "LEFT JOIN s.subjects sub " +
//            "LEFT JOIN s.subjects back " +
//            "WHERE s.semester = :semester " +
//            "AND (sub.subjectCode = :subjectId OR back.subjectCode = :subjectId)")
//    List<Students> findEligibleStudentsBySemesterAndSubject2(
//            @Param("semester") Integer semester,
//            @Param("subjectId") String subjectId
//    );

    @Query(value =
            "SELECT st.enrollment_no FROM students st " +
                    "LEFT JOIN subject_student ss ON st.enrollment_no = ss.enrollment_no " +
                    "WHERE ss.subject_code = :subCode AND st.semester = :sem " +
                    "UNION " +
                    "SELECT st.enrollment_no FROM students st " +
                    "LEFT JOIN back_subjects bs ON st.enrollment_no = bs.enrollment_no " +
                    "WHERE bs.backlog_subjects = :subCode AND st.semester > :sem",
            nativeQuery = true)
    List<String> findEnrollmentNumbersSpecial(@Param("subCode") String subCode, @Param("sem") int sem);
}
