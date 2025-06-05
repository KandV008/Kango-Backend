package dev.kandv.kango.repositories;

import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.enums.CardType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findById(@NonNull Long id);
    List<Card> findAllByCardTypeEquals(@NonNull CardType cardType);
}
