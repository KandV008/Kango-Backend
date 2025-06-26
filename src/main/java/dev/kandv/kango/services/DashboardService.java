package dev.kandv.kango.services;

import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.Dashboard;
import dev.kandv.kango.models.Table;
import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.utils.AttachedFile;
import dev.kandv.kango.repositories.DashboardRepository;
import dev.kandv.kango.repositories.TableRepository;
import dev.kandv.kango.repositories.TagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static dev.kandv.kango.services.ErrorMessagesServices.*;
import static dev.kandv.kango.services.ServiceUtils.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    public static final String INVALID_DASHBOARD_CREATION_ERROR = "ERROR: Invalid Dashboard. Value: ";
    public static final String NOT_FOUND_ELEMENT_ERROR_IN_DASHBOARD = "ERROR: There is no such Element in that Dashboard. Element: ";
    public static final String NOT_FOUND_CARD_IN_THE_DASHBOARD_ERROR = "ERROR: There is no Card with such ID in the Dashboard. ID: ";
    public static final String NOT_FOUND_TABLE_IN_THE_DASHBOARD_ERROR = "ERROR: There is no Table with such ID in the Dashboard. ID: ";

    private final DashboardRepository dashboardRepository;
    private final TableService tableService;
    private final CardService cardService;
    private final TagRepository tagRepository;
    private final TableRepository tableRepository;

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

    private Dashboard checkDatabaseResult(Long id, Optional<Dashboard> result) {
        if (result.isEmpty()) {
            throw new NoSuchElementException(NOT_FOUND_DASHBOARD_WITH_ID_ERROR + id);
        }

        return result.get();
    }

    public void updateName(Long id, String newName) {
        checkId(id);

        Optional<Dashboard> result = this.dashboardRepository.findById(id);

        Dashboard currentDashboard = this.checkDatabaseResult(id, result);
        currentDashboard.setName(newName);
        this.dashboardRepository.save(currentDashboard);
    }

    @Transactional
    public void attachFileToDashboard(Long id, AttachedFile newAttachedFile) {
        checkId(id);
        checkElementToUpdate(newAttachedFile, FILE_ELEMENT);

        Optional<Dashboard> result = this.dashboardRepository.findById(id);

        Dashboard currentDashboard = this.checkDatabaseResult(id, result);
        currentDashboard.attachFile(newAttachedFile);
        this.dashboardRepository.save(currentDashboard);
    }

    @Transactional
    public void detachFileFromDashboard(Long id, AttachedFile attachedFile) {
        checkId(id);
        checkElementToUpdate(attachedFile, FILE_ELEMENT);

        Optional<Dashboard> result = this.dashboardRepository.findById(id);

        Dashboard currentDashboard = this.checkDatabaseResult(id, result);
        boolean isSuccess = currentDashboard.detachFile(attachedFile);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_ELEMENT_ERROR_IN_DASHBOARD + FILE_ELEMENT);
        }

        this.dashboardRepository.save(currentDashboard);
    }

    @Transactional
    public void addTagToDashboard(Long id, Tag tag) {
        checkId(id);
        checkElementToUpdate(tag, TAG_ELEMENT);

        Optional<Dashboard> result = this.dashboardRepository.findById(id);

        Dashboard currentDashboard = this.checkDatabaseResult(id, result);
        currentDashboard.addTagToTagList(tag);
        tag.setDashboard(currentDashboard);

        this.tagRepository.save(tag);
        this.dashboardRepository.save(currentDashboard);
    }

    @Transactional
    public void removeTagFromDashboard(Long id, Tag tag) {
        checkId(id);
        checkElementToUpdate(tag, TAG_ELEMENT);

        Optional<Dashboard> result = this.dashboardRepository.findById(id);

        Dashboard currentDashboard = this.checkDatabaseResult(id, result);
        boolean isSuccess = currentDashboard.removeTagFromTagList(tag);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_ELEMENT_ERROR_IN_DASHBOARD + TAG_ELEMENT);
        }

        this.tagRepository.delete(tag);
        this.dashboardRepository.save(currentDashboard);
    }

    public void addTemplateCardToDashboard(Long dashboardId, Long cardId) {
        Card currentCard = obtainCard(cardId, this.cardService);
        Dashboard currentDashboard = obtainDashboard(dashboardId);

        currentDashboard.addTemplateCard(currentCard);
        this.dashboardRepository.save(currentDashboard);
    }

    public void removeTemplateCardFromDashboard(Long dashboardId, Long cardId) {
        Card currentCard = obtainCard(cardId, this.cardService);
        Dashboard currentDashboard = obtainDashboard(dashboardId);

        boolean isSuccess = currentDashboard.removeTemplateCard(currentCard);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_CARD_IN_THE_DASHBOARD_ERROR + cardId);
        }

        this.cardService.removeCardById(cardId);
        this.dashboardRepository.save(currentDashboard);
    }

    @Transactional
    public void addTableToDashboard(Long dashboardId, Long tableId) {
        Table currentTable = obtainTable(tableId);
        Dashboard currentDashboard = obtainDashboard(dashboardId);

        currentDashboard.addTable(currentTable);

        currentTable.setDashboard(currentDashboard);
        this.tableRepository.save(currentTable);
        this.dashboardRepository.save(currentDashboard);
    }

    @Transactional
    public void removeTableFromDashboard(Long dashboardId, Long tableId) {
        Table currentTable = obtainTable(tableId);
        Dashboard currentDashboard = obtainDashboard(dashboardId);

        boolean isSuccess = currentDashboard.removeTable(currentTable);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_TABLE_IN_THE_DASHBOARD_ERROR + tableId);
        }

        this.tableRepository.delete(currentTable);
        this.dashboardRepository.save(currentDashboard);
    }

    public List<Dashboard> getAllDashboards() {
        return this.dashboardRepository.findAll();
    }

    public void updateTablePositionFromDashboard(Long dashboardId, Long tableId, int newPosition) {
        Table currentTable = obtainTable(tableId);
        Dashboard currentDashboard = obtainDashboard(dashboardId);

        boolean isSuccess = currentDashboard.updateTablePosition(currentTable, newPosition);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_TABLE_IN_THE_DASHBOARD_ERROR + tableId);
        }

        this.dashboardRepository.save(currentDashboard);
    }

    private Dashboard obtainDashboard(Long dashboardId) {
        checkId(dashboardId);
        Optional<Dashboard> result = this.dashboardRepository.findById(dashboardId);
        return this.checkDatabaseResult(dashboardId, result);
    }

    private Table obtainTable(Long tableId) {
        checkElementToUpdate(tableId, TABLE_ID_ELEMENT);
        Table currentTable = this.tableService.getSpecificTableById(tableId);

        if (currentTable == null) {
            throw new NoSuchElementException(NOT_FOUND_TABLE_WITH_ID_ERROR + tableId);
        }

        return currentTable;
    }

}
