package ec.unl.flota.repository;

import ec.unl.flota.entity.VehicleState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleStateRepository extends JpaRepository<VehicleState, String> {

    List<VehicleState> findAllByOrderByVehicleIdAsc();
}
