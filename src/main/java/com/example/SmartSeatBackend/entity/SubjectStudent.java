package com.example.SmartSeatBackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subject_student")
@NoArgsConstructor
@Data
public class SubjectStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enrollment_no", nullable = false)
    private String enrollmentNo;

    // Based on your screenshot, the data is in "subjectCode"
    @Column(name = "\"subjectCode\"")
    private String subjectCode;
}