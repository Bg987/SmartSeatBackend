package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.SeatAllocation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatAllocationRepo extends JpaRepository< SeatAllocation,Long> {
    package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.SeatAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

    public interface SeatAllocationRepo extends JpaRepository<SeatAllocation, Long> {

        @Transactional
        void deleteByCollegeId(Long collegeId);

}
