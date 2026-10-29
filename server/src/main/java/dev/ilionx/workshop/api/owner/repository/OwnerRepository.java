package dev.ilionx.workshop.api.owner.repository;

import dev.ilionx.workshop.api.owner.model.Owner;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Owner} entities.
 */
@Repository
public interface OwnerRepository extends JpaRepository<Owner, Integer> {

    /**
     * Finds all owners with the given last name.
     *
     * @param lastName the last name to search for
     * @return a list of owners matching the last name
     */
    List<Owner> findByLastName(String lastName);

    /**
     * Finds an owner matching both the given last name (case-insensitive) and telephone number
     * exactly. Used for the public, unauthenticated visit-history lookup, where both fields must
     * match together to confirm identity.
     *
     * @param lastName  the owner's last name (case-insensitive)
     * @param telephone the owner's telephone number
     * @return the matching owner, if any
     */
    Optional<Owner> findByLastNameIgnoreCaseAndTelephone(String lastName, String telephone);
}
