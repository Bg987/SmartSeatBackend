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
public class PasswordDTO {

    @NotBlank(message = " old Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String OldPassword;

    @NotBlank(message = "new Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String NewPassword;
}
