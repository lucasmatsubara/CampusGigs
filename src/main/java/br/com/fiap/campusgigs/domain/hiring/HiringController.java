package br.com.fiap.campusgigs.domain.hiring;

import br.com.fiap.campusgigs.auth.AuthorizationService;
import br.com.fiap.campusgigs.domain.gig.Gig;
import br.com.fiap.campusgigs.domain.gig.GigRepository;
import br.com.fiap.campusgigs.domain.gig.GigStatus;
import br.com.fiap.campusgigs.domain.hiring.dto.HiringRequest;
import br.com.fiap.campusgigs.domain.hiring.dto.HiringResponse;
import br.com.fiap.campusgigs.domain.user.User;
import br.com.fiap.campusgigs.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hirings")
@RequiredArgsConstructor
public class HiringController {

    private final HiringRepository hiringRepository;
    private final GigRepository gigRepository;
    private final AuthorizationService authorizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HiringResponse hire(@RequestBody @Valid HiringRequest request) {
        Gig gig = gigRepository.findById(request.gigId())
                .orElseThrow(() -> new EntityNotFoundException("Gig não encontrado: " + request.gigId()));

        User requester = authorizationService.currentUser();

        if (gig.getStatus() != GigStatus.ATIVO) {
            throw new BusinessException("Este gig não está ativo e não pode ser contratado");
        }

        if (gig.getProvider().getId().equals(requester.getId())) {
            throw new BusinessException("Você não pode contratar o próprio gig");
        }

        Hiring hiring = Hiring.builder()
                .gig(gig)
                .requester(requester)
                .status(HiringStatus.SOLICITADA)
                .build();

        return HiringResponse.from(hiringRepository.save(hiring));
    }
}
