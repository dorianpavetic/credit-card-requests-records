package hr.pavetic.creditcardrequestsrecords.service;

import hr.pavetic.creditcardrequestsrecords.dto.PersonDto;
import hr.pavetic.creditcardrequestsrecords.model.Status;
import hr.pavetic.creditcardrequestsrecords.repository.CreditCardRequestRepository;
import hr.pavetic.creditcardrequestsrecords.repository.PersonRepository;
import hr.pavetic.creditcardrequestsrecords.repository.impl.CreditCardRequestRepositoryImpl;
import hr.pavetic.creditcardrequestsrecords.service.impl.CreditCardRequestServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.util.Pair;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class CreditCardRequestServiceTest {
    private final CreditCardRequestRepository creditCardRequestRepository =
            Mockito.mock(CreditCardRequestRepositoryImpl.class);
    private final PersonRepository personRepository =
            Mockito.mock(PersonRepository.class);
    private final CreditCardRequestService creditCardRequestService =
            new CreditCardRequestServiceImpl(creditCardRequestRepository, personRepository);

    private static PersonDto getPersonDto(Status status) {
        return PersonDto.builder()
                .oib("12345678907")
                .name("Dorian")
                .lastName("Pavetic")
                .status(status)
                .build();
    }

    @Test
    void createCreditCardRequestSuccessTest() throws IOException {
        PersonDto personDto = getPersonDto(Status.INACTIVE);

        Mockito.when(personRepository.updateStatusByOib(Status.ACTIVE, personDto.getOib()))
                .thenReturn(1);
        Mockito.when(creditCardRequestRepository.isActiveRequestExist(personDto.getOib()))
                .thenReturn(false);
        Mockito.when(creditCardRequestRepository.writePersonDtoRequest(personDto))
                .thenReturn(Paths.get("requests", personDto.getOib() + ".txt"));

        boolean isSuccess = creditCardRequestService.createCreditCardRequest(personDto);
        Assertions.assertTrue(isSuccess);
    }

    @Test
    void createCreditCardRequestPersonDoesNotExistTest() {
        PersonDto personDto = getPersonDto(Status.INACTIVE);

        Mockito.when(personRepository.updateStatusByOib(Status.ACTIVE, personDto.getOib()))
                .thenReturn(0);

        ResponseStatusException e = Assertions.assertThrows(ResponseStatusException.class,
                () -> creditCardRequestService.createCreditCardRequest(personDto));
        Assertions.assertEquals(
                "The person you want to create a credit card for does not exist", e.getReason());
    }

    @Test
    void createCreditCardRequestActiveAlreadyExistTest() {
        PersonDto personDto = getPersonDto(Status.INACTIVE);

        Mockito.when(personRepository.updateStatusByOib(Status.ACTIVE, personDto.getOib()))
                .thenReturn(1);
        Mockito.when(creditCardRequestRepository.isActiveRequestExist(personDto.getOib()))
                .thenReturn(true);

        ResponseStatusException e = Assertions.assertThrows(ResponseStatusException.class,
                () -> creditCardRequestService.createCreditCardRequest(personDto));
        Assertions.assertEquals(
                "Credit card is already being made for given person", e.getReason());
    }

    @Test
    void createCreditCardRequestIOExceptionTest() throws IOException {
        PersonDto personDto = getPersonDto(Status.INACTIVE);

        Mockito.when(personRepository.updateStatusByOib(Status.ACTIVE, personDto.getOib()))
                .thenReturn(1);
        Mockito.when(creditCardRequestRepository.isActiveRequestExist(personDto.getOib()))
                .thenReturn(false);
        Mockito.when(creditCardRequestRepository.writePersonDtoRequest(personDto))
                .thenThrow(new IOException("some error"));

        ResponseStatusException e = Assertions.assertThrows(ResponseStatusException.class,
                () -> creditCardRequestService.createCreditCardRequest(personDto));
        Assertions.assertEquals(
                "There was a problem creating credit card request", e.getReason());
        Assertions.assertEquals("some error", e.getCause().getMessage());
    }

    @Test
    void deactivateCreditCardRequestTest() throws IOException {
        PersonDto personDto = getPersonDto(Status.ACTIVE);

        List<Pair<Path, PersonDto>> activeRequestFilesForPerson = new ArrayList<>();
        Path path = Paths.get("requests/" + personDto.getOib() + ".txt");
        activeRequestFilesForPerson.add(Pair.of(path, personDto));

        Mockito.when(creditCardRequestRepository.findActiveRequestsForPerson(personDto.getOib()))
                .thenReturn(activeRequestFilesForPerson);
        Mockito.when(creditCardRequestRepository.writePersonDtoRequest(path, personDto))
                .thenReturn(path);

        int count = creditCardRequestService.deactivateCreditCardRequest(personDto.getOib());
        Assertions.assertEquals(1, count);
    }

    @Test
    void deactivateCreditCardRequestIOExceptionTest() throws IOException {
        PersonDto personDto = getPersonDto(Status.ACTIVE);
        String oib = personDto.getOib();

        List<Pair<Path, PersonDto>> activeRequestFilesForPerson = new ArrayList<>();
        Path path = Paths.get("requests/" + oib + ".txt");
        activeRequestFilesForPerson.add(Pair.of(path, personDto));

        Mockito.when(creditCardRequestRepository.findActiveRequestsForPerson(oib))
                .thenReturn(activeRequestFilesForPerson);
        Mockito.when(creditCardRequestRepository.writePersonDtoRequest(path, personDto))
                .thenThrow(new IOException("some error"));

        ResponseStatusException e = Assertions.assertThrows(ResponseStatusException.class,
                () -> creditCardRequestService.deactivateCreditCardRequest(oib));
        Assertions.assertEquals(
                "There was a problem updating credit card request status", e.getReason());
        Assertions.assertEquals("some error", e.getCause().getMessage());
    }

    @Test
    void completeCreditCardRequestSuccessTest() throws IOException {
        PersonDto personDto = getPersonDto(Status.INACTIVE);

        Mockito.when(personRepository.updateStatusByOib(Status.INACTIVE, personDto.getOib()))
                .thenReturn(1);
        List<Pair<Path, PersonDto>> activeRequestFilesForPerson = new ArrayList<>();
        Path path = Paths.get("requests/" + personDto.getOib() + ".txt");
        activeRequestFilesForPerson.add(Pair.of(path, personDto));

        Mockito.when(creditCardRequestRepository.findActiveRequestsForPerson(personDto.getOib()))
                .thenReturn(activeRequestFilesForPerson);
        Mockito.when(creditCardRequestRepository.writePersonDtoRequest(personDto))
                .thenReturn(path);

        String result = creditCardRequestService.completeCreditCardRequest(personDto);
        Assertions.assertEquals("Credit card request(s) completed: 1", result);
    }

    @Test
    void completeCreditCardRequestPersonDoesNotExistTest() {
        PersonDto personDto = getPersonDto(Status.INACTIVE);

        Mockito.when(personRepository.updateStatusByOib(Status.INACTIVE, personDto.getOib()))
                .thenReturn(0);
        List<Pair<Path, PersonDto>> activeRequestFilesForPerson = new ArrayList<>();
        Path path = Paths.get("requests/" + personDto.getOib() + ".txt");
        activeRequestFilesForPerson.add(Pair.of(path, personDto));

        Mockito.when(creditCardRequestRepository.findActiveRequestsForPerson(personDto.getOib()))
                .thenReturn(activeRequestFilesForPerson);

        ResponseStatusException e = Assertions.assertThrows(ResponseStatusException.class,
                () -> creditCardRequestService.completeCreditCardRequest(personDto));
        Assertions.assertEquals(
                "The person you want to complete a credit card request does not exist", e.getReason());
    }

    @Test
    void completeCreditCardRequestNoRequestsToCompleteTest() {
        PersonDto personDto = getPersonDto(Status.INACTIVE);

        Mockito.when(personRepository.updateStatusByOib(Status.INACTIVE, personDto.getOib()))
                .thenReturn(1);
        Mockito.when(creditCardRequestRepository.findActiveRequestsForPerson(personDto.getOib()))
                .thenReturn(new ArrayList<>());

        String result = creditCardRequestService.completeCreditCardRequest(personDto);
        Assertions.assertEquals("No credit card requests to complete for given person.", result);
    }
}
