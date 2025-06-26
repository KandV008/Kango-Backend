package dev.kandv.kango.repositories;

import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.enums.Visibility;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findById(@NonNull Long id);

    List<Tag> findAllByVisibilityEquals(Visibility visibility);
}
