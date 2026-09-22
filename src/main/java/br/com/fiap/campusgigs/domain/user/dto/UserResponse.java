package br.com.fiap.campusgigs.domain.user.dto;

import br.com.fiap.campusgigs.domain.user.Role;
import br.com.fiap.campusgigs.domain.user.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        String cep,
        String street,
        String city,
        String state
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCep(),
                user.getStreet(),
                user.getCity(),
                user.getState()
        );
    }
}
