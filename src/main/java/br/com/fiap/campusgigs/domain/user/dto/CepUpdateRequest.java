package br.com.fiap.campusgigs.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record CepUpdateRequest(
        @NotBlank(message = "O CEP é obrigatório")
        String cep
) {
}
