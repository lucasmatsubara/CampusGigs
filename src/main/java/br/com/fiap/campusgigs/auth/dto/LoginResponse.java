package br.com.fiap.campusgigs.auth.dto;

public record LoginResponse(
        Long id,
        String name,
        String email,
        String role
) {
}
