package com.example.SmartSeatBackend.entity;

import jakarta.persistence.*;


import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "students")
public class Students {



    @Id
    @Column(name = "enrollment_no", unique = true, nullable = false)
    private String enrollmentNo;

    private String name;

    @Column(name = "mobile_number")
    private String mobileNumber;

    private String email;
    private String branch;
    private String specialization;
    private Integer semester;


    @ElementCollection
    @CollectionTable(name = "student_subjects", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "subject_code")
    private List<String> subjects;


    @Column(name = "password")
    private String password;


    @Column(name = "has_backlog")
    private boolean hasBacklog = false;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "college_id")
    private Integer collegeId;

}