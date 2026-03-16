package com.example.SmartSeatBackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "students")
public class Students {

    @Id
    @Column(name = "enrollment_no", nullable = false, unique = true)
    private String enrollmentNo;

    @Column(name = "student_id", insertable = false, updatable = false, unique = true)
    private Long studentId;

    private String name;

    @Column(name = "mobile_number")
    private String mobileNumber;

    private String email;
    private String branch;
    private String specialization;
    private Integer semester;

    // FIXED: Now maps to the SubjectStudent entity
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_no", referencedColumnName = "enrollment_no")
    private List<SubjectStudent> subjects;

    private String password;

    @Column(name = "has_backlog")
    private boolean hasBacklog = false;

    // FIXED: Now maps to the new BacklogStudent entity instead of ElementCollection
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_no", referencedColumnName = "enrollment_no")
    private List<BacklogStudent> backlogSubjects;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "college_id")
    private Long collegeId;


    public void addSubjectsFromCodes(List<String> codes) {
        if (codes == null) return;
        this.subjects = codes.stream().map(code -> {
            SubjectStudent back = new SubjectStudent();
            back.setEnrollmentNo(this.enrollmentNo); // Linking the FK
            back.setSubjectCode(code);
            return back;
        }).collect(Collectors.toList());
    }

    public void addBacklogSubjectsFromCodes(List<String> codes) {
        if (codes == null) return;
        this.backlogSubjects = codes.stream().map(code -> {
            BacklogStudent back = new BacklogStudent();
            back.setEnrollmentNo(this.enrollmentNo); // Linking the FK
            back.setSubjectCode(code);
            return back;
        }).collect(Collectors.toList());
    }
}