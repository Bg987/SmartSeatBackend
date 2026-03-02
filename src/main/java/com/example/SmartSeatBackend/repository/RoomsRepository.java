package com.example.SmartSeatBackend.repository;


import com.example.SmartSeatBackend.entity.Rooms;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomsRepository extends JpaRepository<Rooms,Integer> {




    List<Rooms> findByCollegeCollegeId(Long collegeId);
    Page<Rooms> findByCollegeCollegeId(Long collegeId, Pageable pageble);

    Long countByCollege_CollegeId(Long collegeId);

}
