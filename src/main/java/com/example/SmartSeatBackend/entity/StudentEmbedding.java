package com.example.SmartSeatBackend.entity;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "student_embeddings")
public class StudentEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", unique = true, nullable = false)
    private Long studentId;


    @Column(name = "face_embedding", columnDefinition = "vector(512)")
    private float[] faceEmbedding;
}
