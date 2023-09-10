package hr.pavetic.creditcardrequestsrecords.repository;

import hr.pavetic.creditcardrequestsrecords.dto.PersonDto;
import hr.pavetic.creditcardrequestsrecords.model.Status;
import hr.pavetic.creditcardrequestsrecords.repository.impl.CreditCardRequestRepositoryImpl;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


class CreditCardRequestRepositoryTest {

    private static final String REQUESTS_FOLDER = "test_requests_folder";

    private final CreditCardRequestRepository repository =
            new CreditCardRequestRepositoryImpl(REQUESTS_FOLDER);

    @BeforeAll
    @AfterAll
    static void cleanupResources() throws IOException {
        FileUtils.deleteDirectory(Paths.get(REQUESTS_FOLDER).toFile());
    }

    @Test
    void writeTwoInactivePersonRecordsTest() throws IOException {
        PersonDto personDto = PersonDto.builder()
                .oib("12345678902")
                .name("Dorian")
                .lastName("Pavetic")
                .status(Status.INACTIVE)
                .build();
        Path path = repository.writePersonDtoRequest(personDto);
        Assertions.assertTrue(path.toString().startsWith(REQUESTS_FOLDER + "/" + personDto.getOib()));
        Assertions.assertTrue(path.toString().endsWith(".txt"));
        Assertions.assertTrue(Files.exists(path));

        // Add delay, as sometimes it may happen that values have same milliseconds in the
        // name, resulting that second overwrites the first file, resulting in only 1
        // request for given person
        Awaitility.await().pollDelay(Duration.ONE_MILLISECOND).until(() -> true);

        Path path2 = repository.writePersonDtoRequest(personDto);
        Assertions.assertTrue(path2.toString().startsWith(REQUESTS_FOLDER + "/" + personDto.getOib()));
        Assertions.assertTrue(path2.toString().endsWith(".txt"));
        Assertions.assertTrue(Files.exists(path2));

        List<File> requestFilesForPerson = Arrays.stream(repository.findRequestFilesForPerson(personDto.getOib()))
                .sorted((File::compareTo)).toList();
        Assertions.assertEquals(2, requestFilesForPerson.size());
        Assertions.assertEquals(path, requestFilesForPerson.get(0).toPath());
        Assertions.assertEquals(path2, requestFilesForPerson.get(1).toPath());

        List<Pair<Path, PersonDto>> activeRequestsForPerson =
                repository.findActiveRequestsForPerson(personDto.getOib());
        Assertions.assertTrue(activeRequestsForPerson.isEmpty());

        boolean isActiveRequestExist = repository.isActiveRequestExist(personDto.getOib());
        Assertions.assertFalse(isActiveRequestExist);
    }

    @Test
    void writeTwoActivePersonRecordsTest() throws IOException {
        PersonDto personDto = PersonDto.builder()
                .oib("12345678907")
                .name("Dorian")
                .lastName("Pavetic")
                .status(Status.ACTIVE)
                .build();
        Path path = repository.writePersonDtoRequest(personDto);
        Assertions.assertTrue(path.toString().startsWith(REQUESTS_FOLDER + "/" + personDto.getOib()));
        Assertions.assertTrue(path.toString().endsWith(".txt"));
        Assertions.assertTrue(Files.exists(path));

        // Add delay, as sometimes it may happen that values have same milliseconds in the
        // name, resulting that second overwrites the first file, resulting in only 1
        // request for given person
        Awaitility.await().pollDelay(Duration.ONE_MILLISECOND).until(() -> true);

        Path path2 = repository.writePersonDtoRequest(personDto);
        Assertions.assertTrue(path2.toString().startsWith(REQUESTS_FOLDER + "/" + personDto.getOib()));
        Assertions.assertTrue(path2.toString().endsWith(".txt"));
        Assertions.assertTrue(Files.exists(path2));

        List<File> requestFilesForPerson = Arrays.stream(repository.findRequestFilesForPerson(personDto.getOib()))
                .sorted((File::compareTo)).toList();
        Assertions.assertEquals(2, requestFilesForPerson.size());
        Assertions.assertEquals(path, requestFilesForPerson.get(0).toPath());
        Assertions.assertEquals(path2, requestFilesForPerson.get(1).toPath());

        List<Pair<Path, PersonDto>> activeRequestsForPerson =
                repository.findActiveRequestsForPerson(personDto.getOib());
        Assertions.assertFalse(activeRequestsForPerson.isEmpty());
        Assertions.assertEquals(2, activeRequestsForPerson.size());

        boolean isActiveRequestExist = repository.isActiveRequestExist(personDto.getOib());
        Assertions.assertTrue(isActiveRequestExist);
    }

    @Test
    void findRequestFilesForPersonEmptyTest() {
        File[] requestFilesForPerson = repository.findRequestFilesForPerson("90123901890");
        Assertions.assertEquals(0, requestFilesForPerson.length);
    }

    @Test
    void entryNoInformationTest() throws IOException {
        Files.createDirectories(Paths.get(REQUESTS_FOLDER));
        Files.write(Paths.get(REQUESTS_FOLDER, "41237809412.txt"), new byte[]{});
        ResponseStatusException ex = Assertions.assertThrows(ResponseStatusException.class,
                () -> repository.findActiveRequestsForPerson("41237809412"));
        Assertions.assertEquals(
                "Credit card request entry does not contain any information",
                ex.getReason());
    }

    @Test
    void entryMalformedEntryTest() throws IOException {
        Files.createDirectories(Paths.get(REQUESTS_FOLDER));
        String text = "a b c d e"; // 5 instead of expected 5
        Files.write(Paths.get(REQUESTS_FOLDER, "91290390512.txt"), text.getBytes());
        ResponseStatusException ex = Assertions.assertThrows(ResponseStatusException.class,
                () -> repository.findActiveRequestsForPerson("91290390512"));
        Assertions.assertEquals(
                "Request could not be read properly - malformed entry",
                ex.getReason());
    }
}
