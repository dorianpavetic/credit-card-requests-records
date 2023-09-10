package hr.pavetic.creditcardrequestsrecords.service.impl;

import hr.pavetic.creditcardrequestsrecords.dto.PersonDto;
import hr.pavetic.creditcardrequestsrecords.model.Status;
import hr.pavetic.creditcardrequestsrecords.repository.CreditCardRequestRepository;
import hr.pavetic.creditcardrequestsrecords.repository.PersonRepository;
import hr.pavetic.creditcardrequestsrecords.service.CreditCardRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
@Service
@Log4j2
public class CreditCardRequestServiceImpl implements CreditCardRequestService {
    private final CreditCardRequestRepository creditCardRequestRepository;
    private final PersonRepository personRepository;

    @Transactional
    @Override
    public boolean createCreditCardRequest(@NonNull PersonDto personDto) {
        personDto.setStatus(Status.ACTIVE);

        int updateCount = personRepository.updateStatusByOib(Status.ACTIVE, personDto.getOib());
        // If no people row are updated - it means person with oib does not exist
        if (updateCount < 1)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "The person you want to create a credit card for does not exist");

        try {
            // Check if active request already exist - only 1 active request is allowed
            boolean isActiveRequestExist = creditCardRequestRepository.isActiveRequestExist(personDto.getOib());
            if (isActiveRequestExist)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Credit card is already being made for given person");

            // No active requests for given person found --> create new request
            Path requestPath = creditCardRequestRepository.writePersonDtoRequest(personDto);
            log.info("New credit card request created: {}", requestPath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "There was a problem creating credit card request", e);
        }
        return true;
    }

    @Override
    public int deactivateCreditCardRequest(@NonNull String oib) {
        List<Pair<Path, PersonDto>> activeRequestFilesForPerson = creditCardRequestRepository
                .findActiveRequestsForPerson(oib);
        for (Pair<Path, PersonDto> pathPersonDtoPair : activeRequestFilesForPerson) {
            pathPersonDtoPair.getSecond().setStatus(Status.INACTIVE);
            try {
                Path requestPath = creditCardRequestRepository.writePersonDtoRequest(
                        pathPersonDtoPair.getFirst(), pathPersonDtoPair.getSecond());
                log.info("Credit card record deactivated: {}", requestPath);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "There was a problem updating credit card request status", e);
            }
        }
        return activeRequestFilesForPerson.size();
    }

    @Transactional
    @Override
    public String completeCreditCardRequest(@NonNull PersonDto personDto) {
        String oib = personDto.getOib();

        int updateCount = personRepository.updateStatusByOib(Status.INACTIVE, oib);
        // If no people row are updated - it means person with oib does not exist
        if (updateCount < 1)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "The person you want to complete a credit card request does not exist");

        int deactivatedRequestsCount = deactivateCreditCardRequest(oib);
        log.info("Credit card request(s) completed: " + deactivatedRequestsCount);
        if (deactivatedRequestsCount > 0)
            return "Credit card request(s) completed: " + deactivatedRequestsCount;
        else
            return "No credit card requests to complete for given person.";
    }
}
