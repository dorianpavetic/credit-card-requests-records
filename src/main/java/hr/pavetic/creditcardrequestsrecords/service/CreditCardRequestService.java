package hr.pavetic.creditcardrequestsrecords.service;

import hr.pavetic.creditcardrequestsrecords.dto.PersonDto;
import org.springframework.lang.NonNull;

public interface CreditCardRequestService {
    boolean createCreditCardRequest(@NonNull PersonDto personDto);
    int deactivateCreditCardRequest(@NonNull String oib);
    String completeCreditCardRequest(@NonNull PersonDto personDto);
}
