package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.Timetable;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface  TimetableRepo extends JpaRepository<Timetable, Long> {
    List<Timetable> findByBatchId(String batchId);
    @Modifying
    @Transactional
    @Query("UPDATE Timetable t SET t.completed = true WHERE t.branch = :branch AND t.semester = :semester AND t.completed = false")
    void markOldTimetablesCompleted(@Param("branch") String branch,
                                    @Param("semester") Integer semester);

    List<Timetable> findBybranchAndSemesterAndCompleted(String branch,Integer semester,Boolean completed);


    //fetch exams which remaining for seat allocation
    List<Timetable> findByAllocatedFalse();

    //fetch allocated exam for seating view
    List<Timetable> findByAllocatedTrue();

    @Query("SELECT t.semester FROM Timetable t WHERE t.id = :id")
    Integer findSemesterById(@Param("id") Long id);

    @Query("SELECT t.subjectId FROM Timetable t WHERE t.id = :id")
    String findsubjectIdById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Timetable t SET t.allocated = true WHERE t.id = :examId")
    void markAsAllocated(@Param("examId") Long examId);

    @Query("SELECT t.allocated FROM Timetable t WHERE t.id = :id")
    Boolean findAllocationStatusById(@Param("id") Long id);


    //get examName based on examId
    @Query("""
SELECT CONCAT(t.branch, ' - Sem ', t.semester, 
              ' - ', t.subjectId, 
              ' - ', t.examDate)
FROM Timetable t
WHERE t.id = :timeTableId
""")
    String getExamNameByTimetable(@Param("timeTableId") Long timeTableId);

    //get details of exam for particuler college whose students appear in it
    @Query("""
    SELECT DISTINCT t.id as id, 
           CONCAT(t.branch, ' - Sem ', t.semester, ' - ', t.subjectId, ' - ', t.examDate) as examName
    FROM SeatAllocation s 
    JOIN s.timetable t
    WHERE s.college.id = :collegeId 
      AND t.allocated = true 
      AND t.completed = false
    """)
    List<Map<String, Object>> findActiveExamNamesByCollege(@Param("collegeId") Long collegeId);

    // Check if a Branch/Semester group is already busy on a specific date/time
    @Query("SELECT t FROM Timetable t WHERE t.branch = :branch " +
            "AND t.semester = :semester " +
            "AND t.examDate = :date " +
            "AND t.startTime = :time " +
            "AND t.completed = false")
    List<Timetable> findGroupConflicts(String branch, Integer semester, LocalDate date, LocalTime time);

    // Check if the specific subject is already scheduled and incomplete
    boolean existsBySubjectIdAndCompletedFalse(String subjectId);

}