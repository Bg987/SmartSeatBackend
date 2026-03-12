package com.example.SmartSeatBackend.DTO;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectDTO {

    @NotBlank(message="Subject code is required")
    @Size(min=5, max=8, message = "subjectCode must be Minimum 5 and maximum 8 characters long")
    private String subjectId;


    @NotBlank(message = "Subject name is required")
    @Size(min = 3, max = 50, message = "subject Name must be between 3 and 100 characters")
    private String subjectName;

    @NotBlank(message = "department can't be empty")
    private String department;

    @NotNull(message = "Semester can't be null")
    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 8, message = "Semester must be at most 8")
    private Integer semester;

    @NotBlank(message = "Branch can't be empty")
    private String branch;
}