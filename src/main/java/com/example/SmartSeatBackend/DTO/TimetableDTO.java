package com.example.SmartSeatBackend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableDTO {

    @NotBlank(message = "Subject name is required")
    private String subjectName;

    @NotBlank(message = "Subject ID is required")
    private String subjectId;

    @NotNull(message = "Exam date is required")
    private LocalDate examDate;

    private boolean completed = false;

    private String batchId;//for conserving single time table...

    private String branch;

    @NotNull(message = "Exam semester is required")
    private Integer semester;

    @AssertTrue(message = "Exam date must be at least 1 month from today and not in the past")
    public boolean isExamDateValid() {

        if (examDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate minimumAllowedDate = today.plusMonths(1);

        return !examDate.isBefore(minimumAllowedDate);
    }
}
