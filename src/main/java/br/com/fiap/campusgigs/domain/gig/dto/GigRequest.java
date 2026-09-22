package br.com.fiap.campusgigs.domain.gig.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record GigRequest(
        @NotBlank(message = "O título é obrigatório")
        String title,

        @NotBlank(message = "A descrição é obrigatória")
        String description,

        @NotBlank(message = "A categoria é obrigatória")
        String category,

        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.0", message = "O preço não pode ser negativo")
        BigDecimal price
) {
}
