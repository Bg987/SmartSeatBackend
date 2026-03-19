package com.example.SmartSeatBackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_table")
@NoArgsConstructor
@Data
public class Timetable {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="timetable_id")
    private Long id;

    @Column(name = "subjectid", nullable = false)
    private String subjectId;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Column(name="branch")
    private String branch;

    @Column(name="batchid")
    private String batchId;

    @Column(name="semester")
    private Integer semester;

    @Column(name="exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name="start_time", nullable = false)
    private LocalTime startTime = LocalTime.of(9, 0); // Default 09:00 AM

    @Column(name="duration_minutes", nullable = false)
    private Integer durationMinutes = 180; // 3 Hours

    @Column(name="completed")
    private boolean completed;

    @Column(name = "is_allocated", nullable = false, columnDefinition = "boolean default false")
    private boolean allocated = false;

    @Column(name = "is_QuestionGenrated", nullable = false, columnDefinition = "boolean default false")
    private boolean QuestionGenrated = false;


    //Allowed to enter 30 mins before startTime
    public boolean isAccessAllowed() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime examStart = LocalDateTime.of(this.examDate, this.startTime);

        // Allowed between (Start - 30 mins) AND (Start + 3 Hours)
        return now.isAfter(examStart.minusMinutes(30)) &&
                now.isBefore(examStart.plusMinutes(durationMinutes));
    }
}