package br.com.fiap.campusgigs.domain.gig;

import br.com.fiap.campusgigs.auth.AuthorizationService;
import br.com.fiap.campusgigs.domain.gig.dto.GigRequest;
import br.com.fiap.campusgigs.domain.gig.dto.GigResponse;
import br.com.fiap.campusgigs.domain.user.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gigs")
@RequiredArgsConstructor
public class GigController {

    private final GigRepository gigRepository;
    private final AuthorizationService authorizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GigResponse create(@RequestBody @Valid GigRequest request) {
        User provider = authorizationService.currentUser();

        Gig gig = Gig.builder()
                .provider(provider)
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .price(request.price())
                .status(GigStatus.ATIVO)
                .build();

        return GigResponse.from(gigRepository.save(gig));
    }

    @GetMapping
    public List<GigResponse> list() {
        return gigRepository.findAll().stream()
                .map(GigResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public GigResponse findById(@PathVariable Long id) {
        return GigResponse.from(findGigOrThrow(id));
    }

    @PatchMapping("/{id}/close")
    public GigResponse close(@PathVariable Long id) {
        Gig gig = findGigOrThrow(id);

        authorizationService.assertOwnerOrAdmin(gig.getProvider().getId());

        gig.setStatus(GigStatus.ENCERRADO);
        return GigResponse.from(gigRepository.save(gig));
    }

    private Gig findGigOrThrow(Long id) {
        return gigRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Gig nao encontrado: " + id));
    }
}
