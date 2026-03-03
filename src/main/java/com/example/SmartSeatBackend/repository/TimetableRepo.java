package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.Timetable;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}