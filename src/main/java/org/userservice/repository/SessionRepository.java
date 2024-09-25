package org.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.userservice.model.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Session save(Session session);
}
