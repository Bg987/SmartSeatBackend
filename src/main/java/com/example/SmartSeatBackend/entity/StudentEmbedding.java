package com.example.SmartSeatBackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(name = "student_embeddings")
public class StudentEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", unique = true, nullable = false)
    private Long studentId;


    @Column(name = "face_embedding", columnDefinition = "vector(128)")
    private float[] faceEmbedding;
}
