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
public class GetSeatingPlan {


        private String enrollmentNo;
        private String name;
        private String branch;
        private Integer semester;
        private Integer room_id;
        private Integer row;
        private Integer column;



}
