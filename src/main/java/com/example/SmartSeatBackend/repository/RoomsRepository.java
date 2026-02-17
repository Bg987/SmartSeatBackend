package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.College;
import com.example.SmartSeatBackend.entity.Rooms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomsRepository extends JpaRepository<Rooms,Integer> {
    boolean existsByRoomNumberAndCollege(Integer roomNumber, College college);
}
