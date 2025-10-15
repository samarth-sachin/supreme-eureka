package com.dsl.simulator.Repository;

import com.dsl.simulator.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByFromEntity(String fromEntity);
    List<Message> findByToEntity(String toEntity);
    List<Message> findByFromEntityOrToEntity(String fromEntity, String toEntity);
}
