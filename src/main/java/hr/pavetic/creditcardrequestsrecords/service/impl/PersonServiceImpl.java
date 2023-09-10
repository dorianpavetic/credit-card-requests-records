package hr.pavetic.creditcardrequestsrecords.service.impl;

import hr.pavetic.creditcardrequestsrecords.dto.PersonDto;
import hr.pavetic.creditcardrequestsrecords.model.Person;
import hr.pavetic.creditcardrequestsrecords.model.Status;
import hr.pavetic.creditcardrequestsrecords.repository.PersonRepository;
import hr.pavetic.creditcardrequestsrecords.service.CreditCardRequestService;
import hr.pavetic.creditcardrequestsrecords.service.PersonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
@Log4j2
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    private final CreditCardRequestService creditCardRequestService;

    /**
     * Creates new {@link Person} with {@link Status#INACTIVE}.
     *
     * @implSpec {@link Status#INACTIVE} is set, as {@link Person}
     * record is not active until a document has been created
     * for him.
     *
     * @param personDto dto data by which to create {@link Person}.
     * @return created {@link Person} entity from DB.
     */
    @Override
    public Person createPerson(@NonNull PersonDto personDto) {
        Person person = personDto.toDomain(Status.INACTIVE);
        try {
            Person savedPerson = personRepository.saveAndFlush(person);
            log.info("New person added");
            return savedPerson;
        } catch (DataIntegrityViolationException e) {
            // If unique PRIMARY_KEY constraint violated, means Person already
            // exists, return appropriate message, otherwise rethrow unknown exception
            if (e.getCause() instanceof ConstraintViolationException cve &&
                    cve.getConstraintName().contains("PRIMARY_KEY")) {
                // Would normally go Constraint name instead of "PRIMARY_KEY",
                // but H2 does not seem to respect my constraint name from schema.sql
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Person with given OIB already exists in record");
            }
            throw e;
        }
    }

    /**
     * Searches {@link Person} by OIB.
     *
     * @param oib oib by which to search {@link Person}.
     * @return matching {@link Person} mapped into {@link PersonDto}
     * if exists, otherwise {@code null}.
     */
    @Override
    public PersonDto searchPersonByOIB(@NonNull String oib) {
        Person matchingPerson = personRepository.findByOibOrderByIdDesc(oib);
        log.info("Person search by OIB [{}] found: {}", oib, matchingPerson != null);
        if (matchingPerson == null)
            return null;

        return new PersonDto(matchingPerson);
    }

    /**
     * Deletes {@link Person} from the record and deactivate his documents.
     *
     * @implSpec Uses {@link Transactional} to ensure person is deleted only
     * if everything run successfully - prevents that person record gets
     * deleted in DB even when documents could not be disabled.
     *
     * @param oib person's oib to delete.
     * @return stringed result of the deletion. If any people were deleted,
     * returns number of deleted people, otherwise returns message that no
     * people were deleted.
     */
    @Transactional
    @NonNull
    @Override
    public String deletePersonByOIB(@NonNull String oib) {
        int deleteCount = personRepository.deleteByOib(oib);
        log.info(deleteCount + " person(s) deleted for OIB: {}", oib);
        if (deleteCount > 0) {
            int deactivateCount = creditCardRequestService.deactivateCreditCardRequest(oib);
            log.info(deactivateCount + " credit card requests deactivated for OIB: {}", oib);
            return "Person deleted: " + deleteCount;
        }
        return "There are no people with the requested OIB to delete";
    }
}
