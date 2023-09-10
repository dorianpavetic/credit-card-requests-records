package hr.pavetic.creditcardrequestsrecords.dto;

import hr.pavetic.creditcardrequestsrecords.model.Person;
import hr.pavetic.creditcardrequestsrecords.model.Status;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PersonDto implements Serializable {
    @NotBlank(message = "OIB cannot be null or blank")
    @Size(min = 11, max = 11, message = "OIB must contain exactly 11 digits")
    private String oib;

    @NotBlank(message = "Name cannot be null or blank")
    private String name;

    @NotBlank(message = "Last name cannot be null or blank")
    private String lastName;

    @NotNull(message = "Status cannot be null")
    private Status status;

    public PersonDto(@NonNull Person person) {
        this.oib = person.getOib();
        this.name = person.getName();
        this.lastName = person.getLastName();
        this.status = person.getStatus();
    }

    public Person toDomain(@NonNull Status status) {
        Person person = new Person();
        person.setStatus(status);
        person.setOib(this.oib);
        person.setName(this.name);
        person.setLastName(this.lastName);
        return person;
    }
}
