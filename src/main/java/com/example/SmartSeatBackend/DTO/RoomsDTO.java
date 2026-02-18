package com.example.SmartSeatBackend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.internal.bytebuddy.implementation.bind.annotation.Default;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomsDTO {

    @NotNull(message = "Room number is required")
    private Integer roomNumber;

    private String block;
    @NotNull(message = "Room capacity cannot be blank")
    private Integer capacity;

}
