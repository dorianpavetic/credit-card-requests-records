package hr.pavetic.creditcardrequestsrecords.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Person implements Persistable<String> {
    @Id
    private String oib;
    private String name;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Transient
    @Override
    public String getId() {
        return getOib();
    }

    /**
     * Implementation that ensures that on every
     * saveAndFlush() new instances should be created,
     * and if and entity with same id(oib) is sent,
     * it throws exception.
     *
     * @return whether entity is new.
     */
    @Transient
    @Override
    public boolean isNew() {
        return true;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Person person = (Person) o;
        return getOib() != null && Objects.equals(getOib(), person.getOib());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
