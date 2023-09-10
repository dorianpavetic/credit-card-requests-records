package hr.pavetic.creditcardrequestsrecords.service;

import hr.pavetic.creditcardrequestsrecords.dto.PersonDto;
import hr.pavetic.creditcardrequestsrecords.model.Person;
import hr.pavetic.creditcardrequestsrecords.model.Status;
import hr.pavetic.creditcardrequestsrecords.repository.PersonRepository;
import hr.pavetic.creditcardrequestsrecords.service.impl.CreditCardRequestServiceImpl;
import hr.pavetic.creditcardrequestsrecords.service.impl.PersonServiceImpl;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;

class PersonServiceTest {
    private final PersonRepository personRepository =
            Mockito.mock(PersonRepository.class);
    private final CreditCardRequestService creditCardRequestService =
            Mockito.mock(CreditCardRequestServiceImpl.class);
    private final PersonService personService =
            new PersonServiceImpl(personRepository, creditCardRequestService);

    private static PersonDto getPersonDto() {
        return PersonDto.builder()
                .oib("12345678907")
                .name("Dorian")
                .lastName("Pavetic")
                .status(Status.ACTIVE)
                .build();
    }

    @Test
    void createPersonSuccessTest() {
        PersonDto personDto = getPersonDto();

        Person personMocked = new Person();
        personMocked.setOib(personDto.getOib());
        personMocked.setName(personDto.getName());
        personMocked.setLastName(personDto.getLastName());
        personMocked.setStatus(Status.INACTIVE);
        Mockito
                .when(personRepository.saveAndFlush(ArgumentMatchers.any(Person.class)))
                .thenReturn(personMocked);

        Person person = personService.createPerson(personDto);

        Assertions.assertEquals(personDto.getOib(), person.getOib());
        Assertions.assertEquals(personDto.getOib(), person.getId());
        Assertions.assertEquals(personDto.getName(), person.getName());
        Assertions.assertEquals(personDto.getLastName(), person.getLastName());
        Assertions.assertEquals(Status.INACTIVE, person.getStatus());
        Assertions.assertTrue(person.isNew());
    }

    @Test
    void createPersonAlreadyExistsTest() {
        PersonDto personDto = getPersonDto();

        Person personMocked = new Person();
        personMocked.setOib(personDto.getOib());
        personMocked.setName(personDto.getName());
        personMocked.setLastName(personDto.getLastName());
        personMocked.setStatus(Status.INACTIVE);

        Mockito
                .when(personRepository.saveAndFlush(ArgumentMatchers.any(Person.class)))
                .thenThrow(new DataIntegrityViolationException("err2",
                        new ConstraintViolationException("err", new SQLException(), "PRIMARY_KEY")));

        ResponseStatusException e = Assertions.assertThrows(ResponseStatusException.class,
                () -> personService.createPerson(personDto));
        Assertions.assertEquals("Person with given OIB already exists in record", e.getReason());
    }

    @Test
    void createPersonOtherDataIntegrityExceptionTest() {
        PersonDto personDto = getPersonDto();

        Person personMocked = new Person();
        personMocked.setOib(personDto.getOib());
        personMocked.setName(personDto.getName());
        personMocked.setLastName(personDto.getLastName());
        personMocked.setStatus(Status.INACTIVE);

        Mockito
                .when(personRepository.saveAndFlush(ArgumentMatchers.any(Person.class)))
                .thenThrow(new DataIntegrityViolationException("err2",
                        new IllegalArgumentException("illegal")));


        DataIntegrityViolationException e = Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> personService.createPerson(personDto));
        Assertions.assertTrue(e.getCause() instanceof IllegalArgumentException);
        Assertions.assertEquals("illegal", e.getCause().getMessage());

    }

    @Test
    void searchPersonByOIBExistsTest() {
        Person personMocked = new Person();
        personMocked.setOib("12345678907");
        personMocked.setName("Dorian");
        personMocked.setLastName("Pavetic");
        personMocked.setStatus(Status.INACTIVE);

        Mockito
                .when(personRepository.findByOibOrderByIdDesc(personMocked.getOib()))
                .thenReturn(personMocked);

        PersonDto personDto = personService.searchPersonByOIB(personMocked.getOib());
        Assertions.assertNotNull(personDto);
        Assertions.assertEquals(personMocked.getOib(), personDto.getOib());
        Assertions.assertEquals(personMocked.getName(), personDto.getName());
        Assertions.assertEquals(personMocked.getLastName(), personDto.getLastName());
        Assertions.assertEquals(personMocked.getStatus(), personDto.getStatus());
    }

    @Test
    void searchPersonByOIBDoNoExistTest() {
        Mockito
                .when(personRepository.findByOibOrderByIdDesc("123"))
                .thenReturn(null);

        PersonDto personDto = personService.searchPersonByOIB("123");
        Assertions.assertNull(personDto);
    }

    @Test
    void deletePersonByOIBExistsTest() {
        Mockito
                .when(personRepository.deleteByOib("123"))
                .thenReturn(1);

        // Response should still return number of people deleted - 1
        Mockito.when(creditCardRequestService.deactivateCreditCardRequest("123"))
                .thenReturn(120);

        String result = personService.deletePersonByOIB("123");
        Assertions.assertEquals("Person deleted: 1", result);
    }

    @Test
    void deletePersonByOIBDoNoExistTest() {
        Mockito
                .when(personRepository.deleteByOib("123"))
                .thenReturn(0);

        String result = personService.deletePersonByOIB("123");
        Assertions.assertEquals("There are no people with the requested OIB to delete", result);
    }
}
