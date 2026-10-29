package dev.ilionx.workshop.common.exception;


import io.github.jframe.exception.ApiError;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines an error code and reason for any exception handling.
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "ApiErrorCode")
public enum ApiErrorCode implements ApiError {

    INTERNAL_SERVER_ERROR(
        "ERR-0001",
        "Uncaught Exception: You think I know what went wrong here? If I did, I would've caught this exception no?"
    ),
    OWNER_NOT_FOUND(
        "ERR-0002",
        "The requested owner does not exist."
    ),
    PET_NOT_FOUND(
        "ERR-0003",
        "The requested pet does not exist."
    ),
    PET_TYPE_NOT_FOUND(
        "ERR-0004",
        "The requested pet type does not exist."
    ),
    VET_NOT_FOUND(
        "ERR-0005",
        "The requested vet does not exist."
    ),
    SPECIALTY_NOT_FOUND(
        "ERR-0006",
        "The requested specialty does not exist."
    ),
    VISIT_NOT_FOUND(
        "ERR-0007",
        "The requested visit does not exist."
    ),
    VISIT_LOOKUP_NOT_FOUND(
        "ERR-0008",
        "We couldn't find a match. Please check your details, or call the clinic."
    ),
    VET_HAS_VISITS(
        "ERR-0009",
        "This vet has recorded visits and cannot be deleted."
    );

    /* The error code for this reason. */
    private final String errorCode;

    /* The reason why this error occurred. */
    private final String reason;

}
