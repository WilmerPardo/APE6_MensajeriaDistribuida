package ec.unl.flota.repository;

import ec.unl.flota.entity.GpsTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GpsTelemetryRepository extends JpaRepository<GpsTelemetry, Long> {

    List<GpsTelemetry> findTop50ByVehicleIdOrderByIdDesc(String vehicleId);
}
