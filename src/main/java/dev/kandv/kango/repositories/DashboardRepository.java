package dev.kandv.kango.repositories;

import dev.kandv.kango.models.Dashboard;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

    Optional<Dashboard> findById(@NonNull Long id);
}
