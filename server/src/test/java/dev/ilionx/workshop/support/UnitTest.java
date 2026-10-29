package dev.ilionx.workshop.support;

import dev.ilionx.workshop.api.owner.model.Owner;
import dev.ilionx.workshop.api.pet.model.Pet;
import dev.ilionx.workshop.api.pet.model.PetType;
import dev.ilionx.workshop.api.vet.model.Specialty;
import dev.ilionx.workshop.api.vet.model.Vet;
import dev.ilionx.workshop.api.visit.model.Visit;

import java.time.LocalDate;
import java.util.HashSet;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class UnitTest {

    protected static Owner aValidOwner() {
        final Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("George");
        owner.setLastName("Franklin");
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");
        return owner;
    }

    protected static PetType aValidPetType() {
        final PetType petType = new PetType();
        petType.setId(1);
        petType.setName("Cat");
        return petType;
    }

    protected static Pet aValidPet() {
        final Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Leo");
        pet.setBirthDate(LocalDate.of(2020, 9, 7));
        pet.setType(aValidPetType());
        pet.setOwner(aValidOwner());
        return pet;
    }

    protected static Visit aValidVisit() {
        final Visit visit = new Visit();
        visit.setId(1);
        visit.setDate(LocalDate.of(2023, 1, 1));
        visit.setDescription("Rabies shot");
        visit.setPet(aValidPet());
        visit.setVet(aValidVet());
        return visit;
    }

    protected static Vet aValidVet() {
        final Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Carter");
        vet.setSpecialties(new HashSet<>());
        return vet;
    }

    protected static Specialty aValidSpecialty() {
        final Specialty specialty = new Specialty();
        specialty.setId(1);
        specialty.setName("Radiology");
        return specialty;
    }
}
