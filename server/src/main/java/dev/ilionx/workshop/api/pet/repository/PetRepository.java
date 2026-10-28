package dev.ilionx.workshop.api.pet.repository;

import dev.ilionx.workshop.api.pet.model.Pet;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Pet} entities.
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Integer> {

    /**
     * Finds all pets belonging to the specified owner.
     *
     * @param ownerId the owner's ID
     * @return list of pets for the given owner
     */
    List<Pet> findByOwnerId(Integer ownerId);

    /**
     * Finds a page of pets whose name contains the given value, case-insensitive.
     *
     * @param name     the (partial) pet name to search for
     * @param pageable the pagination information
     * @return a page of matching pets
     */
    Page<Pet> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
