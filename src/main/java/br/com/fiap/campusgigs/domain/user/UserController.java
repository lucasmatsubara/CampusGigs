package br.com.fiap.campusgigs.domain.user;

import br.com.fiap.campusgigs.cep.CepService;
import br.com.fiap.campusgigs.cep.ViaCepResponse;
import br.com.fiap.campusgigs.domain.user.dto.CepUpdateRequest;
import br.com.fiap.campusgigs.domain.user.dto.RegisterRequest;
import br.com.fiap.campusgigs.domain.user.dto.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CepService cepService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody @Valid RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail");
        }

        User.UserBuilder user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER);

        if (request.cep() != null && !request.cep().isBlank()) {
            aplicarEndereco(user, request.cep());
        }

        return UserResponse.from(userRepository.save(user.build()));
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UserResponse.from(currentUser(jwt));
    }

    @PatchMapping("/me/cep")
    public UserResponse updateCep(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid CepUpdateRequest request) {
        User user = currentUser(jwt);

        ViaCepResponse endereco = cepService.buscarEndereco(request.cep());

        user.setCep(CepService.somenteNumeros(request.cep()));
        user.setStreet(endereco.logradouro());
        user.setCity(endereco.localidade());
        user.setState(endereco.uf());

        return UserResponse.from(userRepository.save(user));
    }

    private void aplicarEndereco(User.UserBuilder builder, String cep) {
        ViaCepResponse endereco = cepService.buscarEndereco(cep);

        builder.cep(CepService.somenteNumeros(cep))
                .street(endereco.logradouro())
                .city(endereco.localidade())
                .state(endereco.uf());
    }

    private User currentUser(Jwt jwt) {
        return userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }
}
