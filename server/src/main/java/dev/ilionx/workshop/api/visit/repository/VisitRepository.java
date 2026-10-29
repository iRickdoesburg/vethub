package dev.ilionx.workshop.api.visit.repository;

import dev.ilionx.workshop.api.visit.model.Visit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Visit} entities.
 */
@Repository
public interface VisitRepository extends JpaRepository<Visit, Integer> {

    /**
     * Finds all visits for the specified pet.
     *
     * @param petId the pet's ID
     * @return list of visits for the given pet
     */
    List<Visit> findByPetId(Integer petId);

    /**
     * Checks whether any visit records reference the given vet, used to guard against deleting a
     * vet who has recorded visit history.
     *
     * @param vetId the vet's ID
     * @return {@code true} if at least one visit references this vet
     */
    boolean existsByVetId(Integer vetId);
}
