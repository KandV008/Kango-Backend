package dev.kandv.kango.repositories;

import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
}
