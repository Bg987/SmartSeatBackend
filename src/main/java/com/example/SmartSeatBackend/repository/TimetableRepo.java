package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  TimetableRepo extends JpaRepository<Timetable, Long> {
}
