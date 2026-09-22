package br.com.fiap.campusgigs.domain.hiring.dto;

import jakarta.validation.constraints.NotNull;

public record HiringRequest(
        @NotNull(message = "O gigId é obrigatório")
        Long gigId
) {
}
