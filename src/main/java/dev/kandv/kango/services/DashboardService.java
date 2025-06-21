package dev.kandv.kango.services;

import dev.kandv.kango.models.Dashboard;
import dev.kandv.kango.repositories.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    public static final String INVALID_DASHBOARD_CREATION_ERROR = "ERROR: Invalid Dashboard. Value: ";


    private final DashboardRepository dashboardRepository;

    public Dashboard createDashboard(Dashboard dashboard) {
        try{
            return this.dashboardRepository.save(dashboard);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(INVALID_DASHBOARD_CREATION_ERROR + dashboard);
        }
    }
}
