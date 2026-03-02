package com.example.SmartSeatBackend.repository;

import com.example.SmartSeatBackend.entity.StudentEmbedding;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentEmbeddingRepository extends JpaRepository<StudentEmbedding, Long> {

    // This is the Euclidean search query for authentication
    @Query(value = "SELECT e.student_id FROM student_embeddings e " +
            "ORDER BY e.face_embedding <-> cast(:inputVector as vector) " +
            "LIMIT 1", nativeQuery = true)
    Long findNearestStudentId(@Param("inputVector") float[] inputVector);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO student_embeddings (student_id, face_embedding) " +
            "VALUES (:studentId, cast(:embedding as vector)) " +
            "ON CONFLICT (student_id) " +
            "DO UPDATE SET face_embedding = cast(:embedding as vector)",
            nativeQuery = true)
    void upsertEmbedding(@Param("studentId") Long studentId, @Param("embedding") String embedding);
}