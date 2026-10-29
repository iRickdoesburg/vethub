package dev.ilionx.workshop.api.owner.model.mapper;

import dev.ilionx.workshop.api.owner.model.Owner;
import dev.ilionx.workshop.api.owner.model.response.OwnerResponse;
import dev.ilionx.workshop.api.owner.model.response.OwnerVisitLookupResponse;
import dev.ilionx.workshop.api.owner.model.response.PetSummaryResponse;
import dev.ilionx.workshop.api.pet.model.Pet;
import dev.ilionx.workshop.api.pet.model.response.PetVisitLookupResponse;
import dev.ilionx.workshop.api.visit.model.Visit;
import dev.ilionx.workshop.api.visit.model.response.VisitLookupResponse;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting Owner entities to response DTOs.
 */
@Mapper(config = SharedMapperConfig.class)
public abstract class OwnerMapper {

    public abstract OwnerResponse toResponse(Owner owner);

    public abstract List<OwnerResponse> toResponseList(List<Owner> owners);

    @Mapping(
        source = "type.name",
        target = "typeName"
    )
    public abstract PetSummaryResponse toPetSummaryResponse(Pet pet);

    public abstract OwnerVisitLookupResponse toVisitLookupResponse(Owner owner);

    public abstract VisitLookupResponse toVisitLookupResponse(Visit visit);

    @Mapping(
        source = "type.name",
        target = "type"
    )
    public abstract PetVisitLookupResponse toPetVisitLookupResponse(Pet pet);
}
