package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.DTO.GetSeatByCollege;
import com.example.SmartSeatBackend.entity.College;
import com.example.SmartSeatBackend.entity.SeatAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface SeatAllocationRepo extends JpaRepository<SeatAllocation, Long> {

         //void deleteByCollegeIdAndTimetableId(Long collegeId, Long timetableId);

        //fetch college details whose studnets appears in particuler exam
        @Query("SELECT DISTINCT s.college FROM SeatAllocation s WHERE s.timetable.id = :timetableId")
        List<College> findCollegesByTimetableId(@Param("timetableId") Long timetableId);

        // 2. If using Method Names: Use the Underscore to specify college.collegeId
        //List<SeatAllocation> findByCollege_CollegeId(Long collegeId);

        @Query("""
       SELECT new com.example.SmartSeatBackend.DTO.GetSeatByCollege(
           s.student.enrollmentNo,
           s.room.id,
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

}
