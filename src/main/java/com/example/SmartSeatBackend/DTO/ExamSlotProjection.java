package com.example.SmartSeatBackend.DTO;

import java.time.LocalDate;
import java.time.LocalTime;



public interface ExamSlotProjection {
    Long getId();
    String getExamName();
    LocalDate getExamDate();
    LocalTime getStartTime();
}