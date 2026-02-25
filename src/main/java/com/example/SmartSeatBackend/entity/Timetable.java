package com.example.SmartSeatBackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;


@Entity
@Table(name = "timeTable")
@NoArgsConstructor
@Data
public class Timetable {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "subjectid")
    private String subjectId;

    @Column(name = "subject_name", nullable = false, length = 50)
    private String subjectName;

    @Column(name="exam_date",nullable = false)
    private LocalDate examDate;

    @Column(name="completed")
    private boolean completed;

    @Column(name="batchid")
    private String batchId;

    @Column(name="branch")
    private String branch;

    @Column(name="semester")
    private Integer semester;
}