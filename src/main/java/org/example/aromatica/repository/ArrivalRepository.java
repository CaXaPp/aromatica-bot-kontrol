package org.example.aromatica.repository;

import org.example.aromatica.model.Arrival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArrivalRepository extends JpaRepository<Arrival, Long> {
    boolean existsByUsernameAndArrivalTimeBetween(String username, LocalDateTime start, LocalDateTime end);
    List<Arrival> findAllByUsernameOrderByArrivalTimeAsc(String username);

}
