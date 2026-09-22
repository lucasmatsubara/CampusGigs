package br.com.fiap.campusgigs.auth;

import br.com.fiap.campusgigs.domain.user.Role;
import br.com.fiap.campusgigs.domain.user.User;
import br.com.fiap.campusgigs.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;

    public User currentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado"));
    }

    public boolean isAdmin() {
        return currentUser().getRole() == Role.ADMIN;
    }

    public void assertOwnerOrAdmin(Long ownerId) {
        User current = currentUser();

        boolean isOwner = current.getId().equals(ownerId);
        if (!isOwner && current.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Você não tem permissão para realizar esta ação");
        }
    }
}
