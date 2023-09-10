package hr.pavetic.creditcardrequestsrecords.controller;

import hr.pavetic.creditcardrequestsrecords.dto.PersonDto;
import hr.pavetic.creditcardrequestsrecords.model.Person;
import hr.pavetic.creditcardrequestsrecords.service.PersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/person")
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public ResponseEntity<Person> createPerson(
            @RequestBody @Valid @NonNull PersonDto createPersonDto) {
        return new ResponseEntity<>(
                personService.createPerson(createPersonDto),
                HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PersonDto> searchPersonByOIB(
            @RequestParam @NonNull String oib) {
        return ResponseEntity.ok(personService.searchPersonByOIB(oib));
    }

    @DeleteMapping(path = "/{oib}")
    public ResponseEntity<String> deletePersonByOIB(
            @PathVariable(name = "oib") @NonNull String oib) {
        return ResponseEntity.ok(personService.deletePersonByOIB(oib));
    }
}
