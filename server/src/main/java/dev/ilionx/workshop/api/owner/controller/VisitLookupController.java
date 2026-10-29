package dev.ilionx.workshop.api.owner.controller;

import dev.ilionx.workshop.api.owner.model.Owner;
import dev.ilionx.workshop.api.owner.model.mapper.OwnerMapper;
import dev.ilionx.workshop.api.owner.model.response.OwnerVisitLookupResponse;
import dev.ilionx.workshop.api.owner.service.OwnerService;
import dev.ilionx.workshop.api.visit.model.response.VisitLookupResponse;
import dev.ilionx.workshop.common.ratelimit.SimpleRateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static dev.ilionx.workshop.api.Paths.PUBLIC_VISIT_LOOKUP;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Public, unauthenticated controller letting pet owners look up their own pet's visit history
 * without needing an account, by confirming their last name and telephone number.
 */
@Tag(
    name = "Visit Lookup",
    description = "Public visit-history lookup for pet owners"
)
@RestController
@RequiredArgsConstructor
public class VisitLookupController {

    private final OwnerService ownerService;
    private final OwnerMapper ownerMapper;
    private final SimpleRateLimiter rateLimiter;

    @ResponseStatus(OK)
    @Operation(
        summary = "Look up visit history",
        description = "Returns the pets and visit history for the owner matching the given last name and "
            + "telephone number. No authentication required; both fields must match an existing owner."
    )
    @GetMapping(
        path = PUBLIC_VISIT_LOOKUP,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OwnerVisitLookupResponse> lookupVisitHistory(
        @RequestParam final String lastName,
        @RequestParam final String telephone,
        final HttpServletRequest request
    ) {
        rateLimiter.checkLimit(clientKey(request));

        final Owner owner = ownerService.findForVisitLookup(lastName, telephone);
        final OwnerVisitLookupResponse response = ownerMapper.toVisitLookupResponse(owner);
        response.getPets().forEach(
            pet -> pet.getVisits()
                .sort(Comparator.comparing(VisitLookupResponse::getDate).reversed())
        );

        return ResponseEntity.status(OK).body(response);
    }

    private String clientKey(final HttpServletRequest request) {
        final String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
