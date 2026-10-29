package dev.ilionx.workshop.api.vet.service;

import dev.ilionx.workshop.api.vet.model.Specialty;
import dev.ilionx.workshop.api.vet.model.Vet;
import dev.ilionx.workshop.api.vet.model.request.CreateVetRequest;
import dev.ilionx.workshop.api.vet.model.request.UpdateVetRequest;
import dev.ilionx.workshop.api.vet.repository.SpecialtyRepository;
import dev.ilionx.workshop.api.vet.repository.VetRepository;
import dev.ilionx.workshop.api.visit.repository.VisitRepository;
import dev.ilionx.workshop.support.UnitTest;
import io.github.jframe.exception.core.BadRequestException;
import io.github.jframe.exception.core.DataNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("Unit Test - Vet Service")
class VetServiceTest extends UnitTest {

    private static final Integer VALID_VET_ID = 1;
    private static final String VALID_VET_FIRST_NAME = "James";
    private static final String VALID_VET_LAST_NAME = "Carter";
    private static final Integer VALID_SPECIALTY_ID = 1;
    private static final String VALID_SPECIALTY_NAME = "Radiology";
    private static final Integer NON_EXISTENT_VET_ID = 999;

    private VetRepository vetRepository;
    private SpecialtyRepository specialtyRepository;
    private VisitRepository visitRepository;
    private VetService vetService;

    private Vet vetWithoutSpecialties;
    private Vet vetWithSpecialties;

    @BeforeEach
    void setUp() {
        vetRepository = mock(VetRepository.class);
        specialtyRepository = mock(SpecialtyRepository.class);
        visitRepository = mock(VisitRepository.class);
        vetService = new VetService(vetRepository, specialtyRepository, visitRepository);

        vetWithoutSpecialties = aValidVet();

        final Specialty radiology = aValidSpecialty();
        final Specialty surgery = aValidSpecialty();
        surgery.setName("Surgery");
        vetWithSpecialties = aValidVet();
        vetWithSpecialties.setId(2);
        vetWithSpecialties.setFirstName("Helen");
        vetWithSpecialties.setLastName("Leary");
        vetWithSpecialties.setSpecialties(Set.of(radiology, surgery));
    }

    @Test
    @DisplayName("Should return all vets when vets exist")
    void shouldReturnAllVetsWhenVetsExist() {
        // Given: Multiple vets exist in the repository
        final List<Vet> expectedVets = List.of(vetWithoutSpecialties, vetWithSpecialties);
        given(vetRepository.findAll()).willReturn(expectedVets);

        // When: Finding all vets
        final List<Vet> actualVets = vetService.findAll();

        // Then: All vets should be returned
        assertThat(actualVets, is(notNullValue()));
        assertThat(actualVets, hasSize(2));
        assertThat(actualVets.get(0).getId(), is(equalTo(VALID_VET_ID)));
        assertThat(actualVets.get(0).getFirstName(), is(equalTo(VALID_VET_FIRST_NAME)));
        assertThat(actualVets.get(1).getId(), is(equalTo(2)));
        assertThat(actualVets.get(1).getFirstName(), is(equalTo("Helen")));
    }

    @Test
    @DisplayName("Should return empty list when no vets exist")
    void shouldReturnEmptyListWhenNoVetsExist() {
        // Given: No vets exist in the repository
        given(vetRepository.findAll()).willReturn(Collections.emptyList());

        // When: Finding all vets
        final List<Vet> actualVets = vetService.findAll();

        // Then: An empty list should be returned
        assertThat(actualVets, is(notNullValue()));
        assertThat(actualVets, is(empty()));
    }

    @Test
    @DisplayName("Should return vet when valid ID exists")
    void shouldReturnVetWhenValidIdExists() {
        // Given: A vet with valid ID exists in the repository
        given(vetRepository.findById(VALID_VET_ID)).willReturn(Optional.of(vetWithoutSpecialties));

        // When: Finding vet by ID
        final Vet actualVet = vetService.findById(VALID_VET_ID);

        // Then: The vet should be found
        assertThat(actualVet, is(notNullValue()));
        assertThat(actualVet.getId(), is(equalTo(VALID_VET_ID)));
        assertThat(actualVet.getFirstName(), is(equalTo(VALID_VET_FIRST_NAME)));
        assertThat(actualVet.getLastName(), is(equalTo(VALID_VET_LAST_NAME)));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when vet ID does not exist")
    void shouldThrowDataNotFoundExceptionWhenVetIdDoesNotExist() {
        // Given: No vet exists with the given ID
        given(vetRepository.findById(NON_EXISTENT_VET_ID)).willReturn(Optional.empty());

        // When & Then: Finding by non-existent ID should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> vetService.findById(NON_EXISTENT_VET_ID)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested vet does not exist.")));
    }

    @Test
    @DisplayName("Should create vet when valid data provided")
    void shouldCreateVetWhenValidDataProvided() {
        // Given: A valid create vet request with specialty IDs
        final CreateVetRequest request = new CreateVetRequest();
        request.setFirstName("Alice");
        request.setLastName("Brown");
        request.setSpecialtyIds(List.of(1, 2));

        final Specialty radiology = aValidSpecialty();
        radiology.setId(1);
        final Specialty surgery = aValidSpecialty();
        surgery.setId(2);
        surgery.setName("Surgery");

        final Vet savedVet = aValidVet();
        savedVet.setFirstName("Alice");
        savedVet.setLastName("Brown");
        savedVet.setSpecialties(Set.of(radiology, surgery));

        given(specialtyRepository.findAllById(List.of(1, 2)))
            .willReturn(List.of(radiology, surgery));
        given(vetRepository.save(any(Vet.class))).willReturn(savedVet);

        // When: Creating the vet
        final Vet actualVet = vetService.create(request);

        // Then: The vet should be saved and returned with specialties
        verify(specialtyRepository).findAllById(List.of(1, 2));
        verify(vetRepository).save(any(Vet.class));
        assertThat(actualVet, is(notNullValue()));
        assertThat(actualVet.getFirstName(), is(equalTo("Alice")));
        assertThat(actualVet.getLastName(), is(equalTo("Brown")));
        assertThat(actualVet.getSpecialties(), hasSize(2));
    }

    @Test
    @DisplayName("Should create vet without specialties when no specialty IDs provided")
    void shouldCreateVetWithoutSpecialtiesWhenNoSpecialtyIdsProvided() {
        // Given: A create request with empty specialty IDs
        final CreateVetRequest request = new CreateVetRequest();
        request.setFirstName("Alice");
        request.setLastName("Brown");
        request.setSpecialtyIds(Collections.emptyList());

        final Vet savedVet = aValidVet();
        savedVet.setFirstName("Alice");
        savedVet.setLastName("Brown");

        given(specialtyRepository.findAllById(Collections.emptyList()))
            .willReturn(Collections.emptyList());
        given(vetRepository.save(any(Vet.class))).willReturn(savedVet);

        // When: Creating the vet
        final Vet actualVet = vetService.create(request);

        // Then: The vet should be saved without specialties
        verify(specialtyRepository).findAllById(Collections.emptyList());
        verify(vetRepository).save(any(Vet.class));
        assertThat(actualVet, is(notNullValue()));
        assertThat(actualVet.getFirstName(), is(equalTo("Alice")));
        assertThat(actualVet.getLastName(), is(equalTo("Brown")));
        assertThat(actualVet.getSpecialties(), is(empty()));
    }

    @Test
    @DisplayName("Should update vet when valid data provided")
    void shouldUpdateVetWhenValidDataProvided() {
        // Given: An existing vet and update request
        final Vet existingVet = aValidVet();
        final UpdateVetRequest request = new UpdateVetRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setSpecialtyIds(List.of(1, 2));

        final Specialty radiology = aValidSpecialty();
        radiology.setId(1);
        final Specialty surgery = aValidSpecialty();
        surgery.setId(2);
        surgery.setName("Surgery");

        final Vet updatedVet = aValidVet();
        updatedVet.setFirstName("Updated");
        updatedVet.setLastName("Name");
        updatedVet.setSpecialties(Set.of(radiology, surgery));

        given(vetRepository.findById(VALID_VET_ID)).willReturn(Optional.of(existingVet));
        given(specialtyRepository.findAllById(List.of(1, 2)))
            .willReturn(List.of(radiology, surgery));
        given(vetRepository.save(any(Vet.class))).willReturn(updatedVet);

        // When: Updating the vet
        final Vet actualVet = vetService.update(VALID_VET_ID, request);

        // Then: The vet should be updated and saved
        verify(vetRepository).findById(VALID_VET_ID);
        verify(specialtyRepository).findAllById(List.of(1, 2));
        verify(vetRepository).save(any(Vet.class));
        assertThat(actualVet, is(notNullValue()));
        assertThat(actualVet.getFirstName(), is(equalTo("Updated")));
        assertThat(actualVet.getLastName(), is(equalTo("Name")));
        assertThat(actualVet.getSpecialties(), hasSize(2));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when updating non-existent vet")
    void shouldThrowDataNotFoundExceptionWhenUpdatingNonExistentVet() {
        // Given: No vet exists with the given ID
        final UpdateVetRequest request = new UpdateVetRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setSpecialtyIds(Collections.emptyList());

        given(vetRepository.findById(NON_EXISTENT_VET_ID)).willReturn(Optional.empty());

        // When & Then: Updating non-existent vet should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> vetService.update(NON_EXISTENT_VET_ID, request)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested vet does not exist.")));
    }

    @Test
    @DisplayName("Should delete vet when valid ID exists")
    void shouldDeleteVetWhenValidIdExists() {
        // Given: A vet with valid ID exists in the repository and has no recorded visits
        final Vet existingVet = aValidVet();
        given(vetRepository.findById(VALID_VET_ID)).willReturn(Optional.of(existingVet));
        given(visitRepository.existsByVetId(VALID_VET_ID)).willReturn(false);

        // When: Deleting the vet
        vetService.delete(VALID_VET_ID);

        // Then: The vet should be deleted from the repository
        verify(vetRepository).findById(VALID_VET_ID);
        verify(vetRepository).deleteById(VALID_VET_ID);
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when deleting non-existent vet")
    void shouldThrowDataNotFoundExceptionWhenDeletingNonExistentVet() {
        // Given: No vet exists with the given ID
        given(vetRepository.findById(NON_EXISTENT_VET_ID)).willReturn(Optional.empty());

        // When & Then: Deleting non-existent vet should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> vetService.delete(NON_EXISTENT_VET_ID)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested vet does not exist.")));
    }

    @Test
    @DisplayName("Should throw BadRequestException when deleting a vet who has recorded visits")
    void shouldThrowBadRequestExceptionWhenDeletingVetWithRecordedVisits() {
        // Given: A vet with valid ID exists and has recorded visit history
        final Vet existingVet = aValidVet();
        given(vetRepository.findById(VALID_VET_ID)).willReturn(Optional.of(existingVet));
        given(visitRepository.existsByVetId(VALID_VET_ID)).willReturn(true);

        // When & Then: Deleting the vet should be refused, and the vet should not be deleted
        final BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> vetService.delete(VALID_VET_ID)
        );

        assertThat(exception.getMessage(), is(equalTo("This vet has recorded visits and cannot be deleted.")));
        verify(vetRepository, never()).deleteById(VALID_VET_ID);
    }

}
