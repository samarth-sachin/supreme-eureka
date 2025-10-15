package com.dsl.simulator.Repository;

import com.dsl.simulator.Entity.GroundStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GroundStationRepository extends JpaRepository<GroundStation, Long> {
    Optional<GroundStation> findByStationId(String stationId);
    boolean existsByStationId(String stationId);
}
