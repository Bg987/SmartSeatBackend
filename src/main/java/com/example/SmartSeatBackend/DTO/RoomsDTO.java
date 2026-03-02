package com.example.SmartSeatBackend.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomsDTO {

    @NotNull(message = "Room number is required")
    private Integer roomNumber;

    private String block ="A";
    @NotNull(message = "Room capacity cannot be blank")
    private Integer capacity;

}
