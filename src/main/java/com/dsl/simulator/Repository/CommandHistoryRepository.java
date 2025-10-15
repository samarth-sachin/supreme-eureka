package com.dsl.simulator.Repository;

import com.dsl.simulator.Entity.CommandHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommandHistoryRepository extends JpaRepository<CommandHistory, Long> {
    List<CommandHistory> findTop20ByOrderByExecutedAtDesc();
}
