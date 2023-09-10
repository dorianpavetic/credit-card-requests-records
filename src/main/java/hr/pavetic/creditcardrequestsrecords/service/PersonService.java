package hr.pavetic.creditcardrequestsrecords.service;

import hr.pavetic.creditcardrequestsrecords.dto.PersonDto;
import hr.pavetic.creditcardrequestsrecords.model.Person;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface PersonService {
    Person createPerson(@NonNull PersonDto personDto);

    @Nullable
    PersonDto searchPersonByOIB(@NonNull String oib);

    @NonNull
    String deletePersonByOIB(@NonNull String oib);
}
