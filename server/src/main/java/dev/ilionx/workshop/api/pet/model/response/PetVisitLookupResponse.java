package dev.ilionx.workshop.api.pet.model.response;

import dev.ilionx.workshop.api.visit.model.response.VisitLookupResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Minimal pet summary (name, type, visit history) exposed through the public, unauthenticated
 * visit-history lookup.
 *
 * <p>Deliberately excludes the pet ID, birth date, and owner reference.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Summary of a single pet within a public visit-history lookup result")
public class PetVisitLookupResponse {

    @Schema(
        description = "The pet's name",
        example = "Leo",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
        description = "The pet type name",
        example = "Cat",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String type;

    @Schema(description = "The pet's visit history, most recent first")
    private List<VisitLookupResponse> visits;
}
