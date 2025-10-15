package com.dsl.simulator.Repository;

import com.dsl.simulator.Entity.Satellite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SatelliteRepository extends JpaRepository<Satellite, Long> {
    Optional<Satellite> findBySatelliteId(String satelliteId);
    Optional<Satellite> findByNoradId(Integer noradId);
    List<Satellite> findByStatus(String status);
    boolean existsBySatelliteId(String satelliteId);
    boolean existsByNoradId(Integer noradId);
}
