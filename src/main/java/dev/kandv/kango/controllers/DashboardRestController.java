package dev.kandv.kango.controllers;

import dev.kandv.kango.dtos.CardDTO;
import dev.kandv.kango.dtos.DashboardDTO;
import dev.kandv.kango.dtos.TableDTO;
import dev.kandv.kango.dtos.TagDTO;
import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.Dashboard;
import dev.kandv.kango.models.Table;
import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.utils.AttachedFile;
import dev.kandv.kango.services.CardService;
import dev.kandv.kango.services.DashboardService;
import dev.kandv.kango.services.TableService;
import dev.kandv.kango.services.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

import static dev.kandv.kango.controllers.ErrorMessagesRestControllers.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardRestController {
    public static final String INVALID_DASHBOARD_NAME = "ERROR: Invalid Dashboard Name. Value: ";

    private final DashboardService dashboardService;
    private final TableService tableService;
    private final CardService cardService;
    private final TagService tagService;

    private void checkDashboardName(String name) {
        if (name == null || name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_DASHBOARD_NAME + name);
        }
    }

    private void checkDashboard(Long id, Dashboard currentDashboard) {
        if (currentDashboard == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, DASHBOARD_NOT_FOUND + id);
        }
    }

    private void checkAttachedFile(AttachedFile attachedFile) {
        if (attachedFile == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, NULL_ATTACHED_FILE);
        }

        String fileName = attachedFile.getFileName();
        String fileUrl = attachedFile.getFileUrl();

        if (fileName.isEmpty() || fileUrl.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ATTACHED_FILE);
        }
    }

    private void checkTag(Long id, Tag currentTag) {
        if (currentTag == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TAG_NOT_FOUND + id);
        }
    }

    @PostMapping("/dashboards")
    public ResponseEntity<Dashboard> createDashboard(@RequestBody DashboardDTO dashboardDTO) {
        String name = dashboardDTO.getName();
        this.checkDashboardName(name);

        Dashboard newDashboard = new Dashboard(name);
        Dashboard createdDashboard = this.dashboardService.createDashboard(newDashboard);

        return ResponseEntity.status(201).body(createdDashboard);
    }

    @GetMapping("/dashboards/{id}")
    public ResponseEntity<Dashboard> getDashboard(@PathVariable Long id) {
        Dashboard currentDashboard = this.dashboardService.getSpecificDashboardById(id);

        this.checkDashboard(id, currentDashboard);

        return ResponseEntity.status(200).body(currentDashboard);
    }

    @DeleteMapping("/dashboards/{id}")
    public ResponseEntity<DashboardDTO> deleteDashboard(@PathVariable Long id) {
        Dashboard currentDashboard = this.dashboardService.getSpecificDashboardById(id);

        this.checkDashboard(id, currentDashboard);

        this.dashboardService.removeDashboardById(id);
        Dashboard nullDashboard = this.dashboardService.getSpecificDashboardById(id);

        if (nullDashboard != null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR);
        }

        DashboardDTO dashboardDTO = mapToDashboardDTO(currentDashboard);
        return new ResponseEntity<>(dashboardDTO, HttpStatus.OK);
    }

    @PutMapping("/dashboards/{id}/name")
    public ResponseEntity<Dashboard> updateDashboardName(@PathVariable Long id, @RequestBody DashboardDTO dashboardDTO) {
        String name = dashboardDTO.getName();
        this.checkDashboardName(name);

        try{
            this.dashboardService.updateName(id, name);
            Dashboard updatedCard = this.dashboardService.getSpecificDashboardById(id);

            return ResponseEntity.status(200).body(updatedCard);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, DASHBOARD_NOT_FOUND + id);
        }
    }

    @PostMapping("/dashboards/{id}/attached-files")
    public ResponseEntity<Dashboard> attachFileToDashboard(@PathVariable Long id, @RequestBody AttachedFile attachedFile) {
        this.checkAttachedFile(attachedFile);

        try{
            this.dashboardService.attachFileToDashboard(id, attachedFile);
            Dashboard updatedDashboard = this.dashboardService.getSpecificDashboardById(id);

            return ResponseEntity.status(201).body(updatedDashboard);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, DASHBOARD_NOT_FOUND + id);
        }
    }

    @DeleteMapping("/dashboards/{id}/attached-files")
    public ResponseEntity<Dashboard> detachFileFromDashboard(@PathVariable Long id, @RequestBody AttachedFile attachedFile) {
        this.checkAttachedFile(attachedFile);

        try{
            this.dashboardService.detachFileFromDashboard(id, attachedFile);
            Dashboard updatedDashboard = this.dashboardService.getSpecificDashboardById(id);

            return ResponseEntity.status(200).body(updatedDashboard);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/dashboards/{dashboardId}/tags")
    public ResponseEntity<Dashboard> addTagToDashboard(@PathVariable Long dashboardId, @RequestBody Long tagId) {
        Tag tagById = this.tagService.getSpecificTagById(tagId);
        this.checkTag(tagId, tagById);

        try{
            this.dashboardService.addTagToDashboard(dashboardId, tagById);
            Dashboard updatedDashboard = this.dashboardService.getSpecificDashboardById(dashboardId);

            return ResponseEntity.status(201).body(updatedDashboard);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, DASHBOARD_NOT_FOUND + dashboardId);
        }
    }

    @DeleteMapping("/dashboards/{dashboardId}/tags")
    public ResponseEntity<Dashboard> removeTagFromDashboard(@PathVariable Long dashboardId, @RequestBody Long tagId) {
        Tag tagById = this.tagService.getSpecificTagById(tagId);
        this.checkTag(tagId, tagById);

        try{
            this.dashboardService.removeTagFromDashboard(dashboardId, tagById);
            Dashboard updatedDashboard = this.dashboardService.getSpecificDashboardById(dashboardId);

            return ResponseEntity.status(200).body(updatedDashboard);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/dashboards/{dashboardId}/tables")
    public ResponseEntity<Dashboard> addTableToDashboard(@PathVariable Long dashboardId, @RequestBody Long tableId) {
        try{
            this.dashboardService.addTableToDashboard(dashboardId, tableId);
            Dashboard updatedDashboard = this.dashboardService.getSpecificDashboardById(dashboardId);

            return ResponseEntity.status(201).body(updatedDashboard);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/dashboards/{dashboardId}/tables")
    public ResponseEntity<DashboardDTO> removeTableFromDashboard(@PathVariable Long dashboardId, @RequestBody Long tableId) {
        try{
            this.dashboardService.removeTableFromDashboard(dashboardId, tableId);
            Dashboard updatedDashboard = this.dashboardService.getSpecificDashboardById(dashboardId);

            DashboardDTO dashboardDTO = mapToDashboardDTO(updatedDashboard);

            return ResponseEntity.status(200).body(dashboardDTO);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/dashboards/{dashboardId}/template-cards")
    public ResponseEntity<Dashboard> addTemplateCardToDashboard(@PathVariable Long dashboardId, @RequestBody Long cardId) {
        try{
            this.dashboardService.addTemplateCardToDashboard(dashboardId, cardId);
            Dashboard updatedDashboard = this.dashboardService.getSpecificDashboardById(dashboardId);

            return ResponseEntity.status(201).body(updatedDashboard);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/dashboards/{dashboardId}/template-cards")
    public ResponseEntity<DashboardDTO> removeTemplateCardFromDashboard(@PathVariable Long dashboardId, @RequestBody Long cardId) {
        try{
            this.dashboardService.removeTemplateCardFromDashboard(dashboardId, cardId);
            Dashboard updatedDashboard = this.dashboardService.getSpecificDashboardById(dashboardId);

            DashboardDTO dashboardDTO = mapToDashboardDTO(updatedDashboard);

            return ResponseEntity.status(200).body(dashboardDTO);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/dashboards")
    public ResponseEntity<List<Dashboard>> getAllDashboards(){
        List<Dashboard> dashboardList = this.dashboardService.getAllDashboards();
        return ResponseEntity.status(200).body(dashboardList);
    }

    @PutMapping("/dashboards/{dashboardId}/tables/{tableId}/position")
    public ResponseEntity<DashboardDTO> updateTablePositionFromDashboard(@PathVariable Long dashboardId, @PathVariable Long tableId, @RequestParam int position) {
        try{
            this.dashboardService.updateTablePositionFromDashboard(dashboardId, tableId, position);
            Dashboard updatedDashboard = this.dashboardService.getSpecificDashboardById(dashboardId);

            DashboardDTO dashboardDTO = mapToDashboardDTO(updatedDashboard);
            return ResponseEntity.status(200).body(dashboardDTO);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    static DashboardDTO mapToDashboardDTO(Dashboard currentDashboard) {
        List<Table> tableList = currentDashboard.getTableList();
        List<TableDTO> tableDTOList = tableList.stream().map((TableRestController::mapToTableDTO)).toList();

        List<Card> templateCardList = currentDashboard.getTemplateCardList();
        List<CardDTO> cardDTOList = templateCardList.stream().map((card -> new CardDTO(card.getTitle(), card.getDescription(), card.getCardType(), card.getColor(), card.getDeadLine()))).toList();

        List<Tag> tagList = currentDashboard.getTagList();
        List<TagDTO> tagDTOList = tagList.stream().map((tag -> new TagDTO(tag.getLabel(), tag.getColor(), tag.getVisibility()))).toList();

        return new DashboardDTO(currentDashboard.getName(), tableDTOList, cardDTOList, tagDTOList);
    }
}
