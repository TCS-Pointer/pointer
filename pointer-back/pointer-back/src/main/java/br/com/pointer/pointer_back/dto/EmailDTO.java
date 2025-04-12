package br.com.pointer.pointer_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailDTO {
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;
} 