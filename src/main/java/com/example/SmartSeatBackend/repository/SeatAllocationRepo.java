package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.DTO.GetSeatByCollege;
import com.example.SmartSeatBackend.entity.College;
import com.example.SmartSeatBackend.entity.SeatAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;


public interface SeatAllocationRepo extends JpaRepository<SeatAllocation, Long> {

         //void deleteByCollegeIdAndTimetableId(Long collegeId, Long timetableId);

        //fetch college details whose studnets appears in particuler exam
        @Query("SELECT DISTINCT s.college FROM SeatAllocation s WHERE s.timetable.id = :timetableId")
        List<College> findCollegesByTimetableId(@Param("timetableId") Long timetableId);


        //fetch allocation details based on college
        @Query("""
       SELECT new com.example.SmartSeatBackend.DTO.GetSeatByCollege(
           s.student.enrollmentNo,
           s.room.id,
           s.room.roomNumber,
           s.room.block,
           s.rowNo,
           s.colNo
       )
       FROM SeatAllocation s
       WHERE s.college.collegeId = :collegeId
       AND s.timetable.id = :examId
       """)
        List<GetSeatByCollege> findSeatData(
                @Param("collegeId") Long collegeId,
                @Param("examId") Long examId
        );

        //fetch exam details for stundent complete or not based on flag
        @Query("SELECT DISTINCT s.timetable.id as id, " +
                "CONCAT(s.timetable.branch, ' - Sem ', s.timetable.semester, ' - ', s.timetable.subjectId, ' - ', s.timetable.examDate) as examName, " +
                "s.timetable.examDate as Date " + // Removed trailing comma, ensured space before FROM
                "FROM SeatAllocation s " +
                "WHERE s.student.enrollmentNo = :enrollmentNo " +
                "AND s.timetable.completed = :status")
        List<Map<String, Object>> findAllocatedExamsByStatus(
                @Param("enrollmentNo") String enrollmentNo,
                @Param("status") boolean status);

        @Query("SELECT s FROM SeatAllocation s WHERE s.room.id = :roomId " +
                "AND s.timetable.examDate = :date AND s.timetable.startTime = :time")
        List<SeatAllocation> findExistingInRoom(@Param("roomId") Long roomId,
                                                @Param("date") LocalDate date,
                                                @Param("time") LocalTime time);

}
