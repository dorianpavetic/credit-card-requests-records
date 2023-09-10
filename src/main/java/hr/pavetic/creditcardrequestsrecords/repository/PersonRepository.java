package hr.pavetic.creditcardrequestsrecords.repository;

import hr.pavetic.creditcardrequestsrecords.model.Person;
import hr.pavetic.creditcardrequestsrecords.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PersonRepository extends JpaRepository<Person, String> {
    @Query("select count(p) from Person p where p.oib = ?1 and p.status = 'ACTIVE'")
    long countActiveByOib(@NonNull String oib);

    @Query("select p from Person p where p.oib = ?1 order by p.id DESC")
    @Nullable
    Person findByOibOrderByIdDesc(@NonNull String oib);

    @Transactional
    @Modifying
    @Query("update Person p set p.status = :status where p.oib = :oib")
    int updateStatusByOib(@NonNull @Param("status") Status status, @NonNull @Param("oib") String oib);

    @Transactional
    @Modifying
    @Query("delete from Person p where p.oib = ?1")
    int deleteByOib(@NonNull String oib);
}
