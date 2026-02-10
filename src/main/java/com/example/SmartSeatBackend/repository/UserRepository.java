package com.example.SmartSeatBackend.repository;


import com.example.SmartSeatBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByMail(String mail);
    boolean existsByMail(String mail);
}
