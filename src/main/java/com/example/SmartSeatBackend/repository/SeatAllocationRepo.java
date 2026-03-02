package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.DTO.GetSeatByCollege;
import com.example.SmartSeatBackend.entity.SeatAllocation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface SeatAllocationRepo extends JpaRepository<SeatAllocation, Long> {

        @Transactional
//        void deleteByCollegeIdAndTimetableId(Long collegeId, Long timetableId);
        List<SeatAllocation> findBycollegeId(Long collegeId);



        @Query("""
       SELECT new com.example.SmartSeatBackend.DTO.GetSeatByCollege(
           s.student.enrollmentNo,
           s.room.id,
           s.rowNo,
           s.colNo
       )
       FROM SeatAllocation s
       WHERE s.collegeId = :collegeId
       AND s.timetable.id = :examId
       """)
        List<GetSeatByCollege> findSeatData(
                @Param("collegeId") Long collegeId,
                @Param("examId") Long examId
        );

}
