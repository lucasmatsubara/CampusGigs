package br.com.fiap.campusgigs.domain.hiring.dto;

import br.com.fiap.campusgigs.domain.hiring.Hiring;
import br.com.fiap.campusgigs.domain.hiring.HiringStatus;

import java.time.LocalDateTime;

public record HiringResponse(
        Long id,
        Long gigId,
        String gigTitle,
        Long requesterId,
        String requesterName,
        HiringStatus status,
        LocalDateTime createdAt
) {
    public static HiringResponse from(Hiring hiring) {
        return new HiringResponse(
                hiring.getId(),
                hiring.getGig().getId(),
                hiring.getGig().getTitle(),
                hiring.getRequester().getId(),
                hiring.getRequester().getName(),
                hiring.getStatus(),
                hiring.getCreatedAt()
        );
    }
}
