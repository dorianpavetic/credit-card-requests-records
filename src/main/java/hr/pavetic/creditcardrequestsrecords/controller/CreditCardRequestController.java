package hr.pavetic.creditcardrequestsrecords.controller;

import hr.pavetic.creditcardrequestsrecords.dto.PersonDto;
import hr.pavetic.creditcardrequestsrecords.service.CreditCardRequestService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/credit-card-request")
public class CreditCardRequestController {

    private final CreditCardRequestService creditCardRequestService;

    @PostMapping
    public ResponseEntity<Boolean> createCreditCardRequest(
            @RequestBody @Valid @NonNull PersonDto personDto) {
        return new ResponseEntity<>(
                creditCardRequestService.createCreditCardRequest(personDto),
                HttpStatus.CREATED);
    }

    @PostMapping("/complete")
    public ResponseEntity<String> completeCreditCardRequest(
            @RequestBody @Valid @NonNull PersonDto personDto) {
        return ResponseEntity.ok(creditCardRequestService
                .completeCreditCardRequest(personDto));
    }
}
