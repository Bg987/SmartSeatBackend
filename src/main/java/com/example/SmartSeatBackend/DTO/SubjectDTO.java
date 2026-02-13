package com.example.SmartSeatBackend.DTO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(min=5, max=8, message = "Minimum 5 characters are required ")
    private String subjectId;


    @NotBlank(message = "Subject name is required")
    @Size(min = 1, max = 50, message = "Name must be between 3 and 100 characters")
    private String subjectName;
}
