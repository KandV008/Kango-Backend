package dev.kandv.kango.services;

import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.Dashboard;
import dev.kandv.kango.models.Table;
import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.utils.AttachedFile;
import dev.kandv.kango.repositories.DashboardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    public static final String INVALID_DASHBOARD_CREATION_ERROR = "ERROR: Invalid Dashboard. Value: ";
    public static final String INVALID_ID_ERROR = "ERROR: The ID value is invalid. ID: ";
    public static final String NOT_FOUND_ID_ERROR = "ERROR: There is no Dashboard with such ID. ID: ";
    public static final String INVALID_ELEMENT_ERROR = "ERROR: The element value is null. Element: ";
    public static final String NOT_FOUND_ELEMENT_ERROR = "ERROR: There is no such Element in that Dashboard. Element: ";
    public static final String NOT_FOUND_CARD_ERROR = "ERROR: There is no Card with such ID. ID: ";
    public static final String NOT_FOUND_TABLE_ERROR = "ERROR: There is no Table with such ID. ID: ";
    public static final String NOT_FOUND_CARD_IN_THE_DASHBOARD_ERROR = "ERROR: There is no Card with such ID in the Dashboard. ID: ";
    public static final String NOT_FOUND_TABLE_IN_THE_DASHBOARD_ERROR = "ERROR: There is no Table with such ID in the Dashboard. ID: ";

    private final DashboardRepository dashboardRepository;
    private final TableService tableService;
    private final CardService cardService;

    public Dashboard createDashboard(Dashboard dashboard) {
        try{
            return this.dashboardRepository.save(dashboard);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(INVALID_DASHBOARD_CREATION_ERROR + dashboard);
        }
    }

    public Dashboard getSpecificDashboardById(Long id) {
        Optional<Dashboard> dashboardById = this.dashboardRepository.findById(id);
        return dashboardById.orElse(null);
    }

    public void removeAllDashboards() {
        this.dashboardRepository.deleteAll();
    }

    public void removeDashboardById(Long id) {
        this.dashboardRepository.deleteById(id);
    }

    private void checkId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(INVALID_ID_ERROR + null);
        }
    }

    private Dashboard checkDatabaseResult(Long id, Optional<Dashboard> result) {
        if (result.isEmpty()) {
            throw new NoSuchElementException(NOT_FOUND_ID_ERROR + id);
        }

        return result.get();
    }

    private void checkElementToUpdate(Object newObject, String element) {
        if (newObject == null) {
            throw new IllegalArgumentException(INVALID_ELEMENT_ERROR + element);
        }
    }

    public void updateName(Long id, String newName) {
        this.checkId(id);

        Optional<Dashboard> result = this.dashboardRepository.findById(id);

        Dashboard currentDashboard = this.checkDatabaseResult(id, result);
        currentDashboard.setName(newName);
        this.dashboardRepository.save(currentDashboard);
    }

    @Transactional
    public void attachFileToDashboard(Long id, AttachedFile newAttachedFile) {
        this.checkId(id);
        this.checkElementToUpdate(newAttachedFile, "attachFile");

        Optional<Dashboard> result = this.dashboardRepository.findById(id);

        Dashboard currentDashboard = this.checkDatabaseResult(id, result);
        currentDashboard.attachFile(newAttachedFile);
        this.dashboardRepository.save(currentDashboard);
    }

    @Transactional
    public void detachFileFromDashboard(Long id, AttachedFile attachedFile) {
        String element = "attachFile";
        this.checkId(id);
        this.checkElementToUpdate(attachedFile, element);

        Optional<Dashboard> result = this.dashboardRepository.findById(id);

        Dashboard currentDashboard = this.checkDatabaseResult(id, result);
        boolean isSuccess = currentDashboard.detachFile(attachedFile);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_ELEMENT_ERROR + element);
        }

        this.dashboardRepository.save(currentDashboard);
    }

    @Transactional
    public void addTagToDashboard(Long id, Tag tag) {
        String element = "tag";
        this.checkId(id);
        this.checkElementToUpdate(tag, element);

        Optional<Dashboard> result = this.dashboardRepository.findById(id);

        Dashboard currentCard = this.checkDatabaseResult(id, result);
        currentCard.addTagToTagList(tag);

        this.dashboardRepository.save(currentCard);
    }

    @Transactional
    public void removeTagFromDashboard(Long id, Tag tag) {
        String element = "tag";
        this.checkId(id);
        this.checkElementToUpdate(tag, element);

        Optional<Dashboard> result = this.dashboardRepository.findById(id);

        Dashboard currentDashboard = this.checkDatabaseResult(id, result);
        boolean isSuccess = currentDashboard.removeTagFromTagList(tag);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_ELEMENT_ERROR + element);
        }

        this.dashboardRepository.save(currentDashboard);
    }

    public void addTemplateCardToDashboard(Long dashboardId, Long cardId) {
        this.checkId(dashboardId);
        this.checkElementToUpdate(cardId, "card_id");

        Card currentCard = this.cardService.getSpecificCardById(cardId);

        if (currentCard == null) {
            throw new NoSuchElementException(NOT_FOUND_CARD_ERROR + cardId);
        }

        Optional<Dashboard> result = this.dashboardRepository.findById(dashboardId);
        Dashboard currentDashboard = this.checkDatabaseResult(dashboardId, result);

        currentDashboard.addTemplateCard(currentCard);
        this.dashboardRepository.save(currentDashboard);
    }

    public void removeTemplateCardFromDashboard(Long dashboardId, Long cardId) {
        this.checkId(dashboardId);
        this.checkElementToUpdate(cardId, "card_id");

        Card currentCard = this.cardService.getSpecificCardById(cardId);

        if (currentCard == null) {
            throw new NoSuchElementException(NOT_FOUND_CARD_ERROR + cardId);
        }

        Optional<Dashboard> result = this.dashboardRepository.findById(dashboardId);
        Dashboard currentDashboard = this.checkDatabaseResult(dashboardId, result);

        boolean isSuccess = currentDashboard.removeTemplateCard(currentCard);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_CARD_IN_THE_DASHBOARD_ERROR + cardId);
        }

        this.cardService.removeCardById(cardId);
        this.dashboardRepository.save(currentDashboard);
    }

    public void addTableToDashboard(Long dashboardId, Long tableId) {
        this.checkId(dashboardId);
        this.checkElementToUpdate(tableId, "table_id");

        Table currentTable = this.tableService.getSpecificTableById(tableId);

        if (currentTable == null) {
            throw new NoSuchElementException(NOT_FOUND_TABLE_ERROR + tableId);
        }

        Optional<Dashboard> result = this.dashboardRepository.findById(dashboardId);
        Dashboard currentDashboard = this.checkDatabaseResult(dashboardId, result);

        currentDashboard.addTable(currentTable);
        this.dashboardRepository.save(currentDashboard);
    }

    public void removeTableFromDashboard(Long dashboardId, Long tableId) {
        this.checkId(dashboardId);
        this.checkElementToUpdate(tableId, "table_id");

        Table currentTable = this.tableService.getSpecificTableById(tableId);

        if (currentTable == null) {
            throw new NoSuchElementException(NOT_FOUND_TABLE_ERROR + tableId);
        }

        Optional<Dashboard> result = this.dashboardRepository.findById(dashboardId);
        Dashboard currentDashboard = this.checkDatabaseResult(dashboardId, result);

        boolean isSuccess = currentDashboard.removeTable(currentTable);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_TABLE_IN_THE_DASHBOARD_ERROR + tableId);
        }

        this.dashboardRepository.save(currentDashboard);
    }
}
