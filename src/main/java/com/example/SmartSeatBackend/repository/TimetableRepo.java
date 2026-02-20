package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface  TimetableRepo extends JpaRepository<Timetable, Long> {
    List<Timetable> findByBatchId(String batchId);
}
