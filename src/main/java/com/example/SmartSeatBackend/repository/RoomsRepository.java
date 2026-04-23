package com.example.SmartSeatBackend.repository;


import com.example.SmartSeatBackend.entity.Rooms;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface RoomsRepository extends JpaRepository<Rooms,Integer> {




    List<Rooms> findByCollegeCollegeId(Long collegeId);
    Page<Rooms> findByCollegeCollegeId(Long collegeId, Pageable pageble);

    Long countByCollege_CollegeId(Long collegeId);


    @Query("""
       SELECT r.roomNumber as roomNumber,
              r.capacity as capacity,
              r.block as block
       FROM Rooms r
       WHERE r.id = :roomId
       AND r.college.id = :collegeId
       """)
    Map<String, Object> findCapacityAndRoomnumber(
            @Param("collegeId") Long collegeId,
            @Param("roomId") Long roomId
    );

    //fetch room details for particuler data
    @Query("SELECT r FROM Rooms r WHERE r.college.collegeId = :collegeId")
    List<Rooms> findByCollegeId(@Param("collegeId") Long collegeId);


    //fetch room details for particuler date and time for college
    @Query(value = """
    SELECT DISTINCT 
        r.block AS block, 
        r.room_number AS roomNumber 
    FROM seat_allocation s 
    JOIN rooms r ON s.room_id = r.id 
    JOIN time_table t ON s.timetable_id = t.timetable_id 
    WHERE t.exam_date = :date 
      AND t.start_time = :time 
      AND s.college_id = :collegeId
    """, nativeQuery = true)
    List<Map<String, Object>> findOccupiedRoomsBySlot(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time,
            @Param("collegeId") Long collegeId
    );

}
