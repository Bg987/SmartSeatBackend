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
    @Column(name = "subjectid")
    private String subjectId;

    @Column(name = "subject_name", nullable = false, length = 50)
    private String subjectName;
}