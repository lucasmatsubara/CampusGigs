package br.com.fiap.campusgigs.domain.gig.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record GigRequest(
        @NotBlank(message = "O titulo e obrigatorio")
        String title,

        @NotBlank(message = "A descricao e obrigatoria")
        String description,

        @NotBlank(message = "A categoria e obrigatoria")
        String category,

        @NotNull(message = "O preco e obrigatorio")
        @DecimalMin(value = "0.0", message = "O preco nao pode ser negativo")
        BigDecimal price
) {
}
