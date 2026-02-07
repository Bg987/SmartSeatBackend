package com.example.SmartSeatBackend.repository;


import com.example.SmartSeatBackend.entity.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface  userRepository  extends JpaRepository<user,Long> {

    Optional<user> findByMail(String mail);
    boolean existsByMail(String mail);
}
