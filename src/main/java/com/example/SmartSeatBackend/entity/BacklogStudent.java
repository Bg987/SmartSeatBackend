package com.example.SmartSeatBackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "back_subjects")
@NoArgsConstructor
@Data
public class BacklogStudent {

    // Your screenshot shows "ID" in uppercase
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"ID\"")
    private Long id;

    @Column(name = "enrollment_no", nullable = false)
    private String enrollmentNo;

    // Your screenshot shows "backlog_subjects"
    @Column(name = "backlog_subjects")
    private String subjectCode;
}