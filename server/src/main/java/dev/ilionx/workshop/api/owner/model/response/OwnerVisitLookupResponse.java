package dev.ilionx.workshop.api.owner.model.response;

import dev.ilionx.workshop.api.pet.model.response.PetVisitLookupResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Response for the public, unauthenticated visit-history lookup.
 *
 * <p>Deliberately narrower than {@code OwnerResponse}: no address, telephone, email, or internal
 * IDs are echoed back, since this endpoint is reachable without authentication.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Response containing an owner's pets and their visit history, for public lookup")
public class OwnerVisitLookupResponse {

    @Schema(description = "The owner's pets and their visit history")
    private List<PetVisitLookupResponse> pets;
}
