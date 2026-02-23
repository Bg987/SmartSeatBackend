package com.example.SmartSeatBackend.repository;


import com.example.SmartSeatBackend.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface  CollegeRepository  extends JpaRepository<College,Long> {

    Optional<College> findByUser_userId(Long userId);

}
