package com.example.SmartSeatBackend.repository;


import com.example.SmartSeatBackend.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface  CollegeRepository  extends JpaRepository<College,Long> {

    Optional<College> findByUser_userId(Long userId);

    Optional<College> findByCollegeId(Long collegeId);

    Optional<College> findByUserMail(String mail);

    //fetch userid based on collegeId for notification insertion
    @Query("SELECT c.user.userId FROM College c WHERE c.collegeId = :collegeId")
    String findUserIdByCollegeId(@Param("collegeId") Long collegeId);
}