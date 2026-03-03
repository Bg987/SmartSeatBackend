package com.example.SmartSeatBackend.repository;


import com.example.SmartSeatBackend.entity.Rooms;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface RoomsRepository extends JpaRepository<Rooms,Integer> {




    List<Rooms> findByCollegeCollegeId(Long collegeId);
    Page<Rooms> findByCollegeCollegeId(Long collegeId, Pageable pageble);

    Long countByCollege_CollegeId(Long collegeId);


    @Query("""
       SELECT r.roomNumber as roomNumber,
              r.capacity as capacity
       FROM Rooms r
       WHERE r.id = :roomId
       AND r.college.id = :collegeId
       """)
    Map<String, Object> findCapacityAndRoomnumber(
            @Param("collegeId") Long collegeId,
            @Param("roomId") Long roomId
    );

}
