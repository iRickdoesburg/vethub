package dev.ilionx.workshop.api.owner.controller;

import dev.ilionx.workshop.api.owner.model.Owner;
import dev.ilionx.workshop.api.pet.model.Pet;
import dev.ilionx.workshop.api.visit.model.Visit;
import dev.ilionx.workshop.support.IntegrationTest;
import io.github.jframe.exception.resource.ErrorResponseResource;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static dev.ilionx.workshop.api.Paths.PUBLIC_VISIT_LOOKUP;
import static dev.ilionx.workshop.common.exception.ApiErrorCode.VISIT_LOOKUP_NOT_FOUND;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Visit Lookup Controller")
class VisitLookupControllerTest extends IntegrationTest {

    private static final String OWNER_LAST_NAME = "Franklin";
    private static final String OWNER_TELEPHONE = "6085551023";

    @Test
    @DisplayName("Should return pets and visit history when last name and telephone match")
    void shouldReturnPetsAndVisitHistoryWhenLastNameAndTelephoneMatch() throws Exception {
        // Given: An owner with a pet and two visits exists
        final Owner owner = aSavedOwner();
        final Pet pet = aSavedPet(owner);
        final Visit olderVisit = aSavedVisitOn(pet, LocalDate.of(2022, 1, 1), "Annual checkup");
        final Visit newerVisit = aSavedVisitOn(pet, LocalDate.of(2023, 6, 15), "Follow-up checkup");

        // When: Looking up by matching last name and telephone
        // Then: Should return 200 with the pet and its visits, most recent first
        mockMvc.perform(
            get(PUBLIC_VISIT_LOOKUP)
                .param("lastName", OWNER_LAST_NAME)
                .param("telephone", OWNER_TELEPHONE)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pets", hasSize(1)))
            .andExpect(jsonPath("$.pets[0].name", is(equalTo(pet.getName()))))
            .andExpect(jsonPath("$.pets[0].type", is(notNullValue())))
            .andExpect(jsonPath("$.pets[0].visits", hasSize(2)))
            .andExpect(jsonPath("$.pets[0].visits[0].description", is(equalTo(newerVisit.getDescription()))))
            .andExpect(jsonPath("$.pets[0].visits[1].description", is(equalTo(olderVisit.getDescription()))));
    }

    @Test
    @DisplayName("Should match last name case-insensitively")
    void shouldMatchLastNameCaseInsensitively() throws Exception {
        // Given: An owner named "Franklin" exists
        final Owner owner = aSavedOwner();
        aSavedPet(owner);

        // When: Looking up with a differently-cased last name
        // Then: Should still return 200 with the matching owner's pets
        mockMvc.perform(
            get(PUBLIC_VISIT_LOOKUP)
                .param("lastName", "FRANKLIN")
                .param("telephone", OWNER_TELEPHONE)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pets", hasSize(1)));
    }

    @Test
    @DisplayName("Should not expose owner or pet identifiers or contact details")
    void shouldNotExposeOwnerOrPetIdentifiersOrContactDetails() throws Exception {
        // Given: An owner with a pet exists
        final Owner owner = aSavedOwner();
        aSavedPet(owner);

        // When: Looking up by matching last name and telephone
        final ResultActions response = mockMvc.perform(
            get(PUBLIC_VISIT_LOOKUP)
                .param("lastName", OWNER_LAST_NAME)
                .param("telephone", OWNER_TELEPHONE)
        );

        // Then: The response should not contain the owner's address, telephone, email, or any IDs
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, not(containsString(owner.getAddress())));
        assertThat(content, not(containsString(owner.getTelephone())));
        assertThat(content, not(containsString("\"id\"")));
        assertThat(content, not(containsString("ownerId")));
    }

    @Test
    @DisplayName("Should return not found with a generic message when telephone does not match")
    void shouldReturnNotFoundWithGenericMessageWhenTelephoneDoesNotMatch() throws Exception {
        // Given: An owner named "Franklin" exists, but with a different telephone
        final Owner owner = aSavedOwner();
        aSavedPet(owner);

        // When: Looking up with the correct last name but a wrong telephone
        final ResultActions response = mockMvc.perform(
            get(PUBLIC_VISIT_LOOKUP)
                .param("lastName", OWNER_LAST_NAME)
                .param("telephone", "0000000000")
        );

        // Then: Should return 404 with the generic, non-leaking error message
        response.andExpect(status().isNotFound());
        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(VISIT_LOOKUP_NOT_FOUND.getReason())));
    }

    @Test
    @DisplayName("Should return not found with a generic message when last name does not match")
    void shouldReturnNotFoundWithGenericMessageWhenLastNameDoesNotMatch() throws Exception {
        // Given: An owner with telephone "6085551023" exists, but under a different last name
        final Owner owner = aSavedOwner();
        aSavedPet(owner);

        // When: Looking up with the correct telephone but a wrong last name
        final ResultActions response = mockMvc.perform(
            get(PUBLIC_VISIT_LOOKUP)
                .param("lastName", "NotARealLastName")
                .param("telephone", OWNER_TELEPHONE)
        );

        // Then: Should return the exact same 404/generic message as a telephone mismatch -
        // the two failure cases must be indistinguishable to the caller
        response.andExpect(status().isNotFound());
        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(VISIT_LOOKUP_NOT_FOUND.getReason())));
    }

    private Visit aSavedVisitOn(final Pet pet, final LocalDate date, final String description) {
        final Visit visit = aSavedVisit(pet);
        visit.setDate(date);
        visit.setDescription(description);
        return visitRepository.save(visit);
    }
}
