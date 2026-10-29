package dev.ilionx.workshop.api.owner.service;

import dev.ilionx.workshop.api.owner.model.Owner;
import dev.ilionx.workshop.api.owner.model.request.CreateOwnerRequest;
import dev.ilionx.workshop.api.owner.model.request.UpdateOwnerRequest;
import dev.ilionx.workshop.api.owner.repository.OwnerRepository;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dev.ilionx.workshop.common.exception.ApiErrorCode.OWNER_NOT_FOUND;
import static dev.ilionx.workshop.common.exception.ApiErrorCode.VISIT_LOOKUP_NOT_FOUND;

/**
 * Service for managing pet owner operations.
 */
@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;

    /**
     * Retrieves all owners.
     *
     * @return list of all owners
     */
    @Transactional(readOnly = true)
    public List<Owner> findAll() {
        return ownerRepository.findAll();
    }

    /**
     * Finds owners by last name, or all if no filter provided.
     *
     * @param lastName optional last name filter
     * @return list of matching owners
     */
    @Transactional(readOnly = true)
    public List<Owner> findByLastName(final String lastName) {
        if (lastName == null) {
            return ownerRepository.findAll();
        }
        return ownerRepository.findByLastName(lastName);
    }

    /**
     * Finds an owner by ID.
     *
     * @param id the owner ID
     * @return the found owner
     */
    @Transactional(readOnly = true)
    public Owner findById(final Integer id) {
        return ownerRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException(OWNER_NOT_FOUND));
    }

    /**
     * Finds an owner matching both last name (case-insensitive) and telephone number exactly, for
     * the public, unauthenticated visit-history lookup. Both fields must match together; the
     * resulting error does not reveal which field (if either) was incorrect.
     *
     * @param lastName  the owner's last name
     * @param telephone the owner's telephone number
     * @return the matching owner
     */
    @Transactional(readOnly = true)
    public Owner findForVisitLookup(final String lastName, final String telephone) {
        return ownerRepository.findByLastNameIgnoreCaseAndTelephone(lastName, telephone)
            .orElseThrow(() -> new DataNotFoundException(VISIT_LOOKUP_NOT_FOUND));
    }

    /**
     * Creates a new owner.
     *
     * @param request the create request
     * @return the created owner
     */
    @Transactional
    public Owner create(final CreateOwnerRequest request) {
        final Owner owner = new Owner();
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setAddress(request.getAddress());
        owner.setCity(request.getCity());
        owner.setTelephone(request.getTelephone());
        return ownerRepository.save(owner);
    }

    /**
     * Updates an existing owner.
     *
     * @param id      the owner ID
     * @param request the update request
     * @return the updated owner
     */
    @Transactional
    public Owner update(final Integer id, final UpdateOwnerRequest request) {
        final Owner owner = findById(id);
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setAddress(request.getAddress());
        owner.setCity(request.getCity());
        owner.setTelephone(request.getTelephone());
        return ownerRepository.save(owner);
    }

    /**
     * Deletes an owner by ID.
     *
     * @param id the owner ID
     */
    @Transactional
    public void delete(final Integer id) {
        findById(id);
        ownerRepository.deleteById(id);
    }
}
