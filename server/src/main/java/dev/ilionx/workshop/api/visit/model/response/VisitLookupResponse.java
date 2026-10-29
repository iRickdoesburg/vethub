package dev.ilionx.workshop.api.visit.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * Minimal visit summary exposed through the public, unauthenticated visit-history lookup.
 *
 * <p>Deliberately excludes the visit ID and any other internal identifiers.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Summary of a single visit within a public visit-history lookup result")
public class VisitLookupResponse {

    @Schema(
        description = "The visit date",
        example = "2023-01-01",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate date;

    @Schema(
        description = "The visit description",
        example = "Rabies shot",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String description;
}
