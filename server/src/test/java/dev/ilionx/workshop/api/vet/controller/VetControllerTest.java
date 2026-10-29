package dev.ilionx.workshop.api.vet.controller;

import dev.ilionx.workshop.api.vet.model.request.CreateVetRequest;
import dev.ilionx.workshop.api.vet.model.request.UpdateVetRequest;
import dev.ilionx.workshop.api.vet.model.response.VetResponse;
import dev.ilionx.workshop.support.IntegrationTest;
import io.github.jframe.exception.resource.ErrorResponseResource;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static dev.ilionx.workshop.api.Paths.VETS;
import static dev.ilionx.workshop.api.Paths.VET_BY_ID;
import static dev.ilionx.workshop.common.exception.ApiErrorCode.VET_HAS_VISITS;
import static dev.ilionx.workshop.common.exception.ApiErrorCode.VET_NOT_FOUND;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Vet Controller")
class VetControllerTest extends IntegrationTest {

    private static final int EXPECTED_VET_COUNT = 6;
    private static final int NON_EXISTENT_VET_ID = 999;

    private static final String VET_FIRST_NAME = "Alice";
    private static final String VET_LAST_NAME = "Brown";
    private static final String UPDATED_VET_FIRST_NAME = "Updated";
    private static final String UPDATED_VET_LAST_NAME = "Name";

    // ========================= LIST =========================
    @Test
    @DisplayName("Should return all vets when vets exist")
    void shouldReturnAllVetsWhenVetsExist() throws Exception {
        // Given: Seed data has 6 vets

        // When: Getting all vets
        // Then: All 6 vets should be returned with correct structure
        mockMvc.perform(get(VETS))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(EXPECTED_VET_COUNT)))
            .andExpect(jsonPath("$[0].id", is(equalTo(1))))
            .andExpect(jsonPath("$[0].firstName", is(equalTo("James"))))
            .andExpect(jsonPath("$[0].lastName", is(equalTo("Carter"))))
            .andExpect(jsonPath("$[0].specialties", is(empty())))
            .andExpect(jsonPath("$[1].id", is(equalTo(2))))
            .andExpect(jsonPath("$[1].firstName", is(equalTo("Helen"))))
            .andExpect(jsonPath("$[1].lastName", is(equalTo("Leary"))))
            .andExpect(jsonPath("$[2].id", is(equalTo(3))))
            .andExpect(jsonPath("$[2].firstName", is(equalTo("Linda"))))
            .andExpect(jsonPath("$[2].lastName", is(equalTo("Douglas"))))
            .andExpect(jsonPath("$[3].id", is(equalTo(4))))
            .andExpect(jsonPath("$[3].firstName", is(equalTo("Rafael"))))
            .andExpect(jsonPath("$[3].lastName", is(equalTo("Ortega"))))
            .andExpect(jsonPath("$[4].id", is(equalTo(5))))
            .andExpect(jsonPath("$[4].firstName", is(equalTo("Henry"))))
            .andExpect(jsonPath("$[4].lastName", is(equalTo("Stevens"))))
            .andExpect(jsonPath("$[5].id", is(equalTo(6))))
            .andExpect(jsonPath("$[5].firstName", is(equalTo("Sharon"))))
            .andExpect(jsonPath("$[5].lastName", is(equalTo("Jenkins"))));
    }

    @Test
    @DisplayName("Should return OK status when getting vets")
    void shouldReturnOkStatusWhenGettingVets() throws Exception {
        // Given: Vets exist in the database

        // When: Getting all vets
        // Then: HTTP 200 OK should be returned
        mockMvc.perform(get(VETS))
            .andExpect(status().isOk());
    }

    // ========================= GET BY ID =========================
    @Test
    @DisplayName("Should return vet when valid ID exists")
    void shouldReturnVetWhenValidIdExists() throws Exception {
        // Given: Vet with ID 2 (Helen Leary) exists with Radiology specialty

        // When: Getting vet by ID 2
        // Then: Vet should be returned with specialty
        mockMvc.perform(get(VET_BY_ID, 2))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(equalTo(2))))
            .andExpect(jsonPath("$.firstName", is(equalTo("Helen"))))
            .andExpect(jsonPath("$.lastName", is(equalTo("Leary"))))
            .andExpect(jsonPath("$.specialties", hasSize(1)))
            .andExpect(jsonPath("$.specialties[0].id", is(equalTo(1))))
            .andExpect(jsonPath("$.specialties[0].name", is(equalTo("Radiology"))));
    }

    @Test
    @DisplayName("Should return not found when vet ID does not exist")
    void shouldReturnNotFoundWhenVetIdDoesNotExist() throws Exception {
        // Given: No vet exists with ID 999

        // When: Getting vet by non-existent ID
        final ResultActions response = mockMvc.perform(get(VET_BY_ID, NON_EXISTENT_VET_ID));

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(VET_NOT_FOUND.getReason())));
    }

    // ========================= CREATE =========================
    @Test
    @DisplayName("Should create vet when valid data provided")
    void shouldCreateVetWhenValidDataProvided() throws Exception {
        // Given: A valid create vet request with specialty IDs 1 and 2
        final CreateVetRequest request = aCreateVetRequest(List.of(1, 2));

        // When: Creating the vet via POST
        // Then: HTTP 201 Created should be returned with vet response including id, firstName, lastName, and 2 specialties
        mockMvc.perform(
            post(VETS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(notNullValue())))
            .andExpect(jsonPath("$.firstName", is(equalTo(VET_FIRST_NAME))))
            .andExpect(jsonPath("$.lastName", is(equalTo(VET_LAST_NAME))))
            .andExpect(jsonPath("$.specialties", hasSize(2)))
            .andExpect(jsonPath("$.specialties[0].id", is(equalTo(1))))
            .andExpect(jsonPath("$.specialties[0].name", is(equalTo("Radiology"))))
            .andExpect(jsonPath("$.specialties[1].id", is(equalTo(2))))
            .andExpect(jsonPath("$.specialties[1].name", is(equalTo("Surgery"))));
    }

    @Test
    @DisplayName("Should create vet without specialties when no specialty IDs provided")
    void shouldCreateVetWithoutSpecialtiesWhenNoSpecialtyIdsProvided() throws Exception {
        // Given: A create request with empty specialty IDs
        final CreateVetRequest request = aCreateVetRequest(List.of());

        // When: Creating the vet via POST
        // Then: HTTP 201 Created should be returned with vet response including empty specialties array
        mockMvc.perform(
            post(VETS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(notNullValue())))
            .andExpect(jsonPath("$.firstName", is(equalTo(VET_FIRST_NAME))))
            .andExpect(jsonPath("$.lastName", is(equalTo(VET_LAST_NAME))))
            .andExpect(jsonPath("$.specialties", is(empty())));
    }

    // ========================= UPDATE =========================
    @Test
    @DisplayName("Should update vet when valid data provided")
    void shouldUpdateVetWhenValidDataProvided() throws Exception {
        // Given: A newly created vet via POST
        final String createResponse = mockMvc.perform(
            post(VETS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(aCreateVetRequest(List.of(1))))
        )
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        final Integer createdVetId = fromJson(createResponse, VetResponse.class).getId();

        // When: Updating the vet via PUT with new name and specialty IDs
        final UpdateVetRequest request = anUpdateVetRequest(List.of(2, 3));

        // Then: HTTP 200 OK should be returned with updated vet response
        mockMvc.perform(
            put(VET_BY_ID, createdVetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(equalTo(createdVetId))))
            .andExpect(jsonPath("$.firstName", is(equalTo(UPDATED_VET_FIRST_NAME))))
            .andExpect(jsonPath("$.lastName", is(equalTo(UPDATED_VET_LAST_NAME))))
            .andExpect(jsonPath("$.specialties", hasSize(2)));
    }

    @Test
    @DisplayName("Should return not found when updating non-existent vet")
    void shouldReturnNotFoundWhenUpdatingNonExistentVet() throws Exception {
        // Given: No vet exists with ID 999
        final UpdateVetRequest request = anUpdateVetRequest(List.of());

        // When: Updating non-existent vet via PUT
        final ResultActions response = mockMvc.perform(
            put(VET_BY_ID, NON_EXISTENT_VET_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        );

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(VET_NOT_FOUND.getReason())));
    }

    // ========================= DELETE =========================
    @Test
    @DisplayName("Should delete vet when valid ID exists")
    void shouldDeleteVetWhenValidIdExists() throws Exception {
        // Given: A newly created vet via POST
        final String createResponse = mockMvc.perform(
            post(VETS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(aCreateVetRequest(List.of())))
        )
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        final Integer createdVetId = fromJson(createResponse, VetResponse.class).getId();

        // When: Deleting the vet via DELETE
        // Then: HTTP 204 No Content should be returned
        mockMvc.perform(delete(VET_BY_ID, createdVetId))
            .andExpect(status().isNoContent());

        // And: Getting the deleted vet should return HTTP 404
        mockMvc.perform(get(VET_BY_ID, createdVetId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found when deleting non-existent vet")
    void shouldReturnNotFoundWhenDeletingNonExistentVet() throws Exception {
        // Given: No vet exists with ID 999

        // When: Deleting non-existent vet via DELETE
        final ResultActions response = mockMvc.perform(delete(VET_BY_ID, NON_EXISTENT_VET_ID));

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(VET_NOT_FOUND.getReason())));
    }

    @Test
    @DisplayName("Should reject deleting a vet who has recorded visits")
    void shouldRejectDeletingVetWhoHasRecordedVisits() throws Exception {
        // Given: A vet who has performed a visit on a pet
        final var owner = aSavedOwner();
        final var pet = aSavedPet(owner);
        final var vet = aSavedVet();
        aSavedVisit(pet, vet);

        // When: Attempting to delete that vet via DELETE
        final ResultActions response = mockMvc.perform(delete(VET_BY_ID, vet.getId()));

        // Then: HTTP 400 Bad Request should be returned with a clear error message
        response.andExpect(status().isBadRequest());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(VET_HAS_VISITS.getReason())));

        // And: The vet should still exist
        mockMvc.perform(get(VET_BY_ID, vet.getId()))
            .andExpect(status().isOk());
    }
}
