package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.SeatAllocation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface SeatAllocationRepo extends JpaRepository<SeatAllocation, Long> {

        @Transactional
        void deleteByCollegeId(Long collegeId);
        List<SeatAllocation> findBycollegeId(Long collegeId);

}
