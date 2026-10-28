package dev.ilionx.workshop.api.pet.service;

import dev.ilionx.workshop.api.owner.model.Owner;
import dev.ilionx.workshop.api.owner.repository.OwnerRepository;
import dev.ilionx.workshop.api.pet.model.Pet;
import dev.ilionx.workshop.api.pet.model.PetType;
import dev.ilionx.workshop.api.pet.model.request.CreatePetRequest;
import dev.ilionx.workshop.api.pet.model.request.UpdatePetRequest;
import dev.ilionx.workshop.api.pet.repository.PetRepository;
import dev.ilionx.workshop.api.pet.repository.PetTypeRepository;
import dev.ilionx.workshop.support.UnitTest;
import io.github.jframe.exception.core.DataNotFoundException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Unit Test - Pet Service")
class PetServiceTest extends UnitTest {

    private static final Integer VALID_OWNER_ID = 1;
    private static final Integer VALID_PET_ID = 1;
    private static final String VALID_PET_NAME = "Leo";
    private static final LocalDate VALID_BIRTH_DATE = LocalDate.of(2020, 9, 7);
    private static final Integer VALID_PET_TYPE_ID = 1;
    private static final String VALID_PET_TYPE_NAME = "Cat";
    private static final Integer NON_EXISTENT_PET_ID = 999;
    private static final Integer NON_EXISTENT_OWNER_ID = 999;
    private static final Integer NON_EXISTENT_PET_TYPE_ID = 999;

    private PetRepository petRepository;
    private PetTypeRepository petTypeRepository;
    private OwnerRepository ownerRepository;
    private PetService petService;

    @BeforeEach
    void setUp() {
        petRepository = mock(PetRepository.class);
        petTypeRepository = mock(PetTypeRepository.class);
        ownerRepository = mock(OwnerRepository.class);
        petService = new PetService(petRepository, petTypeRepository, ownerRepository);
    }

    @Test
    @DisplayName("Should return pets when owner has pets")
    void shouldReturnPetsWhenOwnerHasPets() {
        // Given: An owner exists with 2 pets in the repository
        final Owner owner = aValidOwner();
        final Pet firstPet = aValidPet();
        final Pet secondPet = aValidPet();
        secondPet.setId(2);
        secondPet.setName("Bella");
        final List<Pet> expectedPets = List.of(firstPet, secondPet);
        given(ownerRepository.findById(VALID_OWNER_ID)).willReturn(Optional.of(owner));
        given(petRepository.findByOwnerId(VALID_OWNER_ID)).willReturn(expectedPets);

        // When: Finding pets by owner ID
        final List<Pet> actualPets = petService.findByOwnerId(VALID_OWNER_ID);

        // Then: Both pets should be returned
        assertThat(actualPets, is(notNullValue()));
        assertThat(actualPets, hasSize(2));
        assertThat(actualPets.get(0).getId(), is(equalTo(VALID_PET_ID)));
        assertThat(actualPets.get(0).getName(), is(equalTo(VALID_PET_NAME)));
        assertThat(actualPets.get(1).getId(), is(equalTo(2)));
        assertThat(actualPets.get(1).getName(), is(equalTo("Bella")));
    }

    @Test
    @DisplayName("Should return empty list when owner has no pets")
    void shouldReturnEmptyListWhenOwnerHasNoPets() {
        // Given: An owner exists with no pets
        final Owner owner = aValidOwner();
        given(ownerRepository.findById(VALID_OWNER_ID)).willReturn(Optional.of(owner));
        given(petRepository.findByOwnerId(VALID_OWNER_ID)).willReturn(Collections.emptyList());

        // When: Finding pets by owner ID
        final List<Pet> actualPets = petService.findByOwnerId(VALID_OWNER_ID);

        // Then: An empty list should be returned
        assertThat(actualPets, is(notNullValue()));
        assertThat(actualPets, is(empty()));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when owner does not exist for findByOwnerId")
    void shouldThrowDataNotFoundExceptionWhenOwnerDoesNotExistForFindByOwnerId() {
        // Given: No owner exists with the given ID
        given(ownerRepository.findById(NON_EXISTENT_OWNER_ID)).willReturn(Optional.empty());

        // When & Then: Finding pets for non-existent owner should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> petService.findByOwnerId(NON_EXISTENT_OWNER_ID)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested owner does not exist.")));
    }

    @Test
    @DisplayName("Should return pet when valid pet ID exists")
    void shouldReturnPetWhenValidPetIdExists() {
        // Given: A pet with valid ID exists in the repository
        final Pet expectedPet = aValidPet();
        given(petRepository.findById(VALID_PET_ID)).willReturn(Optional.of(expectedPet));

        // When: Finding pet by ID
        final Pet actualPet = petService.findById(VALID_PET_ID);

        // Then: The pet should be found with all fields
        assertThat(actualPet, is(notNullValue()));
        assertThat(actualPet.getId(), is(equalTo(VALID_PET_ID)));
        assertThat(actualPet.getName(), is(equalTo(VALID_PET_NAME)));
        assertThat(actualPet.getBirthDate(), is(equalTo(VALID_BIRTH_DATE)));
        assertThat(actualPet.getType().getId(), is(equalTo(VALID_PET_TYPE_ID)));
        assertThat(actualPet.getType().getName(), is(equalTo(VALID_PET_TYPE_NAME)));
        assertThat(actualPet.getOwner().getId(), is(equalTo(VALID_OWNER_ID)));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when pet ID does not exist")
    void shouldThrowDataNotFoundExceptionWhenPetIdDoesNotExist() {
        // Given: No pet exists with the given ID
        given(petRepository.findById(NON_EXISTENT_PET_ID)).willReturn(Optional.empty());

        // When & Then: Finding by non-existent ID should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> petService.findById(NON_EXISTENT_PET_ID)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested pet does not exist.")));
    }

    @Test
    @DisplayName("Should create pet when valid data provided")
    void shouldCreatePetWhenValidDataProvided() {
        // Given: A valid create pet request with existing owner and pet type
        final CreatePetRequest request = new CreatePetRequest();
        request.setName(VALID_PET_NAME);
        request.setBirthDate(VALID_BIRTH_DATE);
        request.setTypeId(VALID_PET_TYPE_ID);
        final Owner owner = aValidOwner();
        final PetType petType = aValidPetType();
        final Pet savedPet = aValidPet();
        given(ownerRepository.findById(VALID_OWNER_ID)).willReturn(Optional.of(owner));
        given(petTypeRepository.findById(VALID_PET_TYPE_ID)).willReturn(Optional.of(petType));
        given(petRepository.save(any(Pet.class))).willReturn(savedPet);

        // When: Creating the pet
        final Pet actualPet = petService.create(VALID_OWNER_ID, request);

        // Then: The pet should be saved and returned with correct owner, type, name, and birthDate
        verify(petRepository).save(any(Pet.class));
        assertThat(actualPet, is(notNullValue()));
        assertThat(actualPet.getId(), is(equalTo(VALID_PET_ID)));
        assertThat(actualPet.getName(), is(equalTo(VALID_PET_NAME)));
        assertThat(actualPet.getBirthDate(), is(equalTo(VALID_BIRTH_DATE)));
        assertThat(actualPet.getType().getId(), is(equalTo(VALID_PET_TYPE_ID)));
        assertThat(actualPet.getType().getName(), is(equalTo(VALID_PET_TYPE_NAME)));
        assertThat(actualPet.getOwner().getId(), is(equalTo(VALID_OWNER_ID)));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when owner does not exist for create")
    void shouldThrowDataNotFoundExceptionWhenOwnerDoesNotExistForCreate() {
        // Given: No owner exists with the given ID
        final CreatePetRequest request = new CreatePetRequest();
        request.setName(VALID_PET_NAME);
        request.setBirthDate(VALID_BIRTH_DATE);
        request.setTypeId(VALID_PET_TYPE_ID);
        given(ownerRepository.findById(NON_EXISTENT_OWNER_ID)).willReturn(Optional.empty());

        // When & Then: Creating pet for non-existent owner should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> petService.create(NON_EXISTENT_OWNER_ID, request)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested owner does not exist.")));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when pet type does not exist for create")
    void shouldThrowDataNotFoundExceptionWhenPetTypeDoesNotExistForCreate() {
        // Given: Owner exists but pet type does not exist
        final CreatePetRequest request = new CreatePetRequest();
        request.setName(VALID_PET_NAME);
        request.setBirthDate(VALID_BIRTH_DATE);
        request.setTypeId(NON_EXISTENT_PET_TYPE_ID);
        final Owner owner = aValidOwner();
        given(ownerRepository.findById(VALID_OWNER_ID)).willReturn(Optional.of(owner));
        given(petTypeRepository.findById(NON_EXISTENT_PET_TYPE_ID)).willReturn(Optional.empty());

        // When & Then: Creating pet with non-existent pet type should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> petService.create(VALID_OWNER_ID, request)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested pet type does not exist.")));
    }

    @Test
    @DisplayName("Should update pet when valid data provided")
    void shouldUpdatePetWhenValidDataProvided() {
        // Given: An existing pet and update request with new pet type
        final Pet existingPet = aValidPet();
        final UpdatePetRequest request = new UpdatePetRequest();
        request.setName("Bella");
        request.setBirthDate(VALID_BIRTH_DATE);
        request.setTypeId(2);
        final PetType newPetType = aValidPetType();
        newPetType.setId(2);
        newPetType.setName("Dog");
        final Pet updatedPet = aValidPet();
        updatedPet.setName("Bella");
        updatedPet.setType(newPetType);
        given(petRepository.findById(VALID_PET_ID)).willReturn(Optional.of(existingPet));
        given(petTypeRepository.findById(2)).willReturn(Optional.of(newPetType));
        given(petRepository.save(any(Pet.class))).willReturn(updatedPet);

        // When: Updating the pet
        final Pet actualPet = petService.update(VALID_PET_ID, request);

        // Then: The pet should be updated and saved with new name, birthDate, and type
        verify(petRepository).save(any(Pet.class));
        assertThat(actualPet, is(notNullValue()));
        assertThat(actualPet.getId(), is(equalTo(VALID_PET_ID)));
        assertThat(actualPet.getName(), is(equalTo("Bella")));
        assertThat(actualPet.getBirthDate(), is(equalTo(VALID_BIRTH_DATE)));
        assertThat(actualPet.getType().getId(), is(equalTo(2)));
        assertThat(actualPet.getType().getName(), is(equalTo("Dog")));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when pet does not exist for update")
    void shouldThrowDataNotFoundExceptionWhenPetDoesNotExistForUpdate() {
        // Given: No pet exists with the given ID
        final UpdatePetRequest request = new UpdatePetRequest();
        request.setName("Bella");
        request.setBirthDate(VALID_BIRTH_DATE);
        request.setTypeId(VALID_PET_TYPE_ID);
        given(petRepository.findById(NON_EXISTENT_PET_ID)).willReturn(Optional.empty());

        // When & Then: Updating non-existent pet should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> petService.update(NON_EXISTENT_PET_ID, request)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested pet does not exist.")));
    }

    @Test
    @DisplayName("Should return all pets when pets exist")
    void shouldReturnAllPetsWhenPetsExist() {
        // Given: Multiple pets exist in the repository
        final Pet firstPet = aValidPet();
        final Pet secondPet = aValidPet();
        secondPet.setId(2);
        secondPet.setName("Bella");
        final Pet thirdPet = aValidPet();
        thirdPet.setId(3);
        thirdPet.setName("Max");
        final List<Pet> expectedPets = List.of(firstPet, secondPet, thirdPet);
        final Pageable pageable = PageRequest.of(0, 10);
        given(petRepository.findAll(pageable)).willReturn(new PageImpl<>(expectedPets, pageable, expectedPets.size()));

        // When: Finding all pets
        final Page<Pet> actualPets = petService.findAll(null, pageable);

        // Then: All pets should be returned
        assertThat(actualPets, is(notNullValue()));
        assertThat(actualPets.getContent(), hasSize(3));
        assertThat(actualPets.getContent().get(0).getId(), is(equalTo(VALID_PET_ID)));
        assertThat(actualPets.getContent().get(0).getName(), is(equalTo(VALID_PET_NAME)));
        assertThat(actualPets.getContent().get(1).getId(), is(equalTo(2)));
        assertThat(actualPets.getContent().get(1).getName(), is(equalTo("Bella")));
        assertThat(actualPets.getContent().get(2).getId(), is(equalTo(3)));
        assertThat(actualPets.getContent().get(2).getName(), is(equalTo("Max")));
    }

    @Test
    @DisplayName("Should return empty page when no pets exist")
    void shouldReturnEmptyListWhenNoPetsExist() {
        // Given: No pets exist in the repository
        final Pageable pageable = PageRequest.of(0, 10);
        given(petRepository.findAll(pageable)).willReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        // When: Finding all pets
        final Page<Pet> actualPets = petService.findAll(null, pageable);

        // Then: An empty page should be returned
        assertThat(actualPets, is(notNullValue()));
        assertThat(actualPets.getContent(), is(empty()));
    }

    @Test
    @DisplayName("Should filter pets by name when name is provided")
    void shouldFilterPetsByNameWhenNameIsProvided() {
        // Given: A pet matching the search term exists
        final Pet matchingPet = aValidPet();
        matchingPet.setName("Bella");
        final Pageable pageable = PageRequest.of(0, 10);
        given(petRepository.findByNameContainingIgnoreCase("bell", pageable))
            .willReturn(new PageImpl<>(List.of(matchingPet), pageable, 1));

        // When: Finding pets by name
        final Page<Pet> actualPets = petService.findAll("bell", pageable);

        // Then: Only the matching pet should be returned
        assertThat(actualPets.getContent(), hasSize(1));
        assertThat(actualPets.getContent().get(0).getName(), is(equalTo("Bella")));
    }

    @Test
    @DisplayName("Should delete pet when pet exists")
    void shouldDeletePetWhenPetExists() {
        // Given: A pet exists in the repository
        final Pet existingPet = aValidPet();
        given(petRepository.findById(VALID_PET_ID)).willReturn(Optional.of(existingPet));

        // When: Deleting the pet
        petService.delete(VALID_PET_ID);

        // Then: The pet should be deleted from the repository
        verify(petRepository).delete(existingPet);
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when pet does not exist for delete")
    void shouldThrowDataNotFoundExceptionWhenPetDoesNotExistForDelete() {
        // Given: No pet exists with the given ID
        given(petRepository.findById(NON_EXISTENT_PET_ID)).willReturn(Optional.empty());

        // When & Then: Deleting non-existent pet should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> petService.delete(NON_EXISTENT_PET_ID)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested pet does not exist.")));
    }

}
