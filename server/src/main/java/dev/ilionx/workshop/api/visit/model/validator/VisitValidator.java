package dev.ilionx.workshop.api.visit.model.validator;

import dev.ilionx.workshop.api.visit.model.request.CreateVisitRequest;
import dev.ilionx.workshop.api.visit.model.request.UpdateVisitRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for visit requests.
 */
@Component
public class VisitValidator implements Validator<Object> {

    // Error messages
    public static final String BODY_IS_MISSING = "Request body is missing";
    public static final String VET_ID_REQUIRED = "A vet must be selected for this visit";

    // Fields
    public static final String VET_ID = "vetId";

    /**
     * Validates a CreateVisitRequest.
     *
     * @param request the create request
     * @param result  the validation result
     */
    public void validate(final CreateVisitRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(VET_ID, request.getVetId())
            .whenNull(VET_ID_REQUIRED);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * Validates an UpdateVisitRequest.
     *
     * @param request the update request
     * @param result  the validation result
     */
    public void validate(final UpdateVisitRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(VET_ID, request.getVetId())
            .whenNull(VET_ID_REQUIRED);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object request, final ValidationResult result) {
        if (request instanceof CreateVisitRequest) {
            validate((CreateVisitRequest) request, result);
        } else if (request instanceof UpdateVisitRequest) {
            validate((UpdateVisitRequest) request, result);
        } else {
            result.reject("Unsupported request type");
            throw new ValidationException(result);
        }
    }
}
