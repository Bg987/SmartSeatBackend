package com.example.SmartSeatBackend.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectFilterDTO {
    private String department;
    private String branch;
    private Integer semester;

}
