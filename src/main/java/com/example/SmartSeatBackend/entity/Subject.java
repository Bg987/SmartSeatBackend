package com.example.SmartSeatBackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subjects")
@NoArgsConstructor
@Data
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Uses Postgres BIGSERIAL
    @Column(name = "subjectid")
    private Long subjectId;

    @Column(name = "subject_name", nullable = false, length = 50)
    private String subjectName;
}