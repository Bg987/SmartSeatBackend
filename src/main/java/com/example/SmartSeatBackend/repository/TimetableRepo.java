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
}
