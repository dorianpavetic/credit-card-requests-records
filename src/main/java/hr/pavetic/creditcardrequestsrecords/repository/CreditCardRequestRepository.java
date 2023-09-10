package hr.pavetic.creditcardrequestsrecords.repository;

import hr.pavetic.creditcardrequestsrecords.dto.PersonDto;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface CreditCardRequestRepository {
    @NonNull
    Path writePersonDtoRequest(
            @NonNull Path requestPath, @NonNull PersonDto personDto) throws IOException;

    @NonNull
    Path writePersonDtoRequest(@NonNull PersonDto personDto) throws IOException;

    @NonNull
    List<Pair<Path, PersonDto>> findActiveRequestsForPerson(@NonNull String oib);

    @NonNull
    File[] findRequestFilesForPerson(@NonNull String oib);

    boolean isActiveRequestExist(@NonNull String oib);
}
