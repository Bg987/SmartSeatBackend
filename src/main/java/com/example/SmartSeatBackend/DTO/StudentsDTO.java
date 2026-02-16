package com.example.SmartSeatBackend.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentsDTO {

    @NotBlank(message = "Enrollment number is mandatory")
    private String enrollmentNo;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    private String mobileNumber;

    @Email(message = "Please provide a valid email")
    @NotBlank(message = "Email is mandatory")
    private String email;

    private String branch;

    private String specialization;

    @NotNull(message = "Semester is required")
    private Integer semester;

    @NotEmpty(message = "At least one subject must be provided")
    private List<String> subjects;

    private boolean hasBacklog;

    private String imgUrl;

    @NotNull(message = "College ID is required")
    private Integer collegeId;
}
