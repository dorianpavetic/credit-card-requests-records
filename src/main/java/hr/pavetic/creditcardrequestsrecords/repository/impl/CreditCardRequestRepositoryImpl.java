package hr.pavetic.creditcardrequestsrecords.repository.impl;

import hr.pavetic.creditcardrequestsrecords.dto.PersonDto;
import hr.pavetic.creditcardrequestsrecords.model.Status;
import hr.pavetic.creditcardrequestsrecords.repository.CreditCardRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Component
public class CreditCardRequestRepositoryImpl implements CreditCardRequestRepository {

    /**
     * Folder where all credit card requests are persisted.
     */
    @Value("${requests-folder}")
    private final String requestsFolder;

    @NonNull
    @Override
    public Path writePersonDtoRequest(
            @NonNull Path requestPath, @NonNull PersonDto personDto) throws IOException {
        // Ensure folder exists, otherwise IOException would occur if it does not exist
        Files.createDirectories(Paths.get(requestsFolder));

        byte[] requestEntryBytes = personDtoToRequestEntry(personDto).getBytes();
        Files.write(requestPath, requestEntryBytes);
        return requestPath;
    }

    @NonNull
    @Override
    public Path writePersonDtoRequest(@NonNull PersonDto personDto) throws IOException {
        String fileName = personDto.getOib() + "_" +
                Instant.now().toEpochMilli() + ".txt";
        Path requestPath = Paths.get(requestsFolder, fileName);
        writePersonDtoRequest(requestPath, personDto);
        return requestPath;
    }

    @Override
    public boolean isActiveRequestExist(@NonNull String oib) {
        return !findActiveRequestsForPerson(oib).isEmpty();
    }

    @NonNull
    @Override
    public List<Pair<Path, PersonDto>> findActiveRequestsForPerson(@NonNull String oib) {
        File[] matchingFiles = findRequestFilesForPerson(oib);
        return Arrays.stream(matchingFiles)
                .map(file -> {
                    try {
                        Path path = file.toPath();
                        List<String> entry = Files.readAllLines(path);
                        return Pair.of(path, parseRequestEntryToPersonDto(entry));
                    } catch (IOException e) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "There was a problem reading credit card request", e);
                    }
                })
                .filter(pair -> pair.getSecond().getStatus() == Status.ACTIVE)
                .toList();
    }

    @NonNull
    @Override
    public File[] findRequestFilesForPerson(@NonNull String oib) {
        File[] matchingFiles = Paths.get(requestsFolder)
                .toFile()
                .listFiles((dir, name) ->
                        name.startsWith(oib) && name.endsWith(".txt"));
        if (matchingFiles == null)
            return new File[]{};
        return matchingFiles;
    }

    @NonNull
    private PersonDto parseRequestEntryToPersonDto(@NonNull List<String> entryLines) {
        if (entryLines.isEmpty() || !StringUtils.hasText(entryLines.get(0)))
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Credit card request entry does not contain any information");

        String[] values = entryLines.get(0).split(" ");
        if (values.length != 4)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Request could not be read properly - malformed entry");

        return PersonDto.builder()
                .name(values[0])
                .lastName(values[1])
                .oib(values[2])
                .status(Status.parse(values[3]))
                .build();
    }

    @NonNull
    private String personDtoToRequestEntry(@NonNull PersonDto personDto) {
        return String.join(" ",
                personDto.getName(),
                personDto.getLastName(),
                personDto.getOib(),
                personDto.getStatus().name());
    }
}
