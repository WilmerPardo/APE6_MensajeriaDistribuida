package ec.unl.flota.repository;

import ec.unl.flota.entity.AlertEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertEventRepository extends JpaRepository<AlertEvent, Long> {

    long countByActiveTrue();

    long countByType(String type);

    List<AlertEvent> findTop50ByOrderByIdDesc();
}
