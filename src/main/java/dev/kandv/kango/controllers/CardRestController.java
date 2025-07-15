package dev.kandv.kango.controllers;

import dev.kandv.kango.dtos.CardDTO;
import dev.kandv.kango.dtos.TemplateCardDTO;
import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.enums.CardType;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.utils.AttachedFile;
import dev.kandv.kango.models.utils.Check;
import dev.kandv.kango.services.CardService;
import dev.kandv.kango.services.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import static dev.kandv.kango.controllers.ErrorMessagesRestControllers.*;
import static dev.kandv.kango.controllers.RestControllerUtils.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CardRestController {
    public static final String INVALID_TITLE = "ERROR: Invalid Card Title. Value: ";
    public static final String INVALID_DESCRIPTION = "ERROR: Invalid Card Description. Value: ";
    public static final String INVALID_CARD_TYPE = "ERROR: Invalid Card Type. Value: ";
    public static final String INVALID_COLOR = "ERROR: Invalid Card Color. Value: ";
    public static final String INVALID_DEAD_LINE = "ERROR: Invalid Card Color. Value: ";

    private final CardService cardService;
    private final TagService tagService;

    private void checkCardTitle(String title) {
        if (title == null || title.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_TITLE + title);
        }
    }

    private void checkCardDescription(String description) {
        if (description == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_DESCRIPTION + null);
        }
    }

    private void checkCardType(CardType cardType) {
        if (cardType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_CARD_TYPE + null);
        }
    }

    private void checkColor(Color color) {
        if (color == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_COLOR + null);
        }
    }

    private void checkDeadLine(Date deadLine) {
        if (deadLine == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_DEAD_LINE + null);
        }
    }

    private void checkCheck(Check check) {
        if (check == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, NULL_CHECK);
        }

        String label = check.getLabel();

        if (label.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_CHECK);
        }
    }

    @PostMapping("/cards")
    public ResponseEntity<Card> createCard(@RequestBody CardDTO cardDTO) {
        String title = cardDTO.getTitle();
        this.checkCardTitle(title);
        CardType cardType = cardDTO.getCardType();
        this.checkCardType(cardType);

        Card newCard = new Card(title, cardType);
        Card createdCard = this.cardService.createCard(newCard);

        return ResponseEntity.status(201).body(createdCard);
    }

    @PostMapping("/cards/{id}/copy")
    public ResponseEntity<Card> createCardUsingATemplate(@PathVariable Long id) {
        try{
            Card copyCard = this.cardService.createCardUsingATemplate(id);

            return ResponseEntity.status(201).body(copyCard);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CARD_NOT_FOUND + id);
        }
    }

    @GetMapping("/cards/{id}")
    public ResponseEntity<Card> getCard(@PathVariable Long id) {
        Card currentCard = this.cardService.getSpecificCardById(id);

        checkCard(id, currentCard);

        return ResponseEntity.status(200).body(currentCard);
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<CardDTO> deleteCard(@PathVariable Long id) {
        Card currentCard = this.cardService.getSpecificCardById(id);

        checkCard(id, currentCard);

        this.cardService.removeCardById(id);
        Card nullCard = this.cardService.getSpecificCardById(id);

        if (nullCard != null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR);
        }

        CardDTO cardDTO = new CardDTO(currentCard.getTitle(), currentCard.getCardType());
        return new ResponseEntity<>(cardDTO, HttpStatus.OK);
    }

    @GetMapping("/global-template-cards")
    public ResponseEntity<List<TemplateCardDTO>> getGlobalTemplatesCards() {
        List<Card> allGlobalTemplateCards = this.cardService.getAllGlobalTemplateCards();

        List<TemplateCardDTO> templateCardDTOs = allGlobalTemplateCards.stream()
                .map(CardRestController::mapToTemplateCardDTO)
                .toList();

        return ResponseEntity.status(200).body(templateCardDTOs);
    }

    @PutMapping("/cards/{id}/title")
    public ResponseEntity<Card> updateCardTitle(@PathVariable Long id, @RequestBody CardDTO cardDTO) {
        String title = cardDTO.getTitle();
        this.checkCardTitle(title);

        try{
            this.cardService.updateTitleCard(id, title);

            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CARD_NOT_FOUND + id);
        }
    }

    @PutMapping("/cards/{id}/description")
    public ResponseEntity<Card> updateCardDescription(@PathVariable Long id, @RequestBody CardDTO cardDTO) {
        String description = cardDTO.getDescription();
        this.checkCardDescription(description);

        try{
            this.cardService.updateDescriptionCard(id, description);

            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CARD_NOT_FOUND + id);
        }
    }

    @PutMapping("/cards/{id}/color")
    public ResponseEntity<Card> updateCardColor(@PathVariable Long id, @RequestBody CardDTO cardDTO) {
        Color color = cardDTO.getColor();
        this.checkColor(color);

        try{
            this.cardService.updateColorCard(id, color);

            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CARD_NOT_FOUND + id);
        }
    }

    @PutMapping("/cards/{id}/deadline")
    public ResponseEntity<Card> updateCardDeadLine(@PathVariable Long id, @RequestBody CardDTO cardDTO) {
        Date deadLine = cardDTO.getDeadLine();
        this.checkDeadLine(deadLine);

        try{
            this.cardService.updateDeadLineCard(id, deadLine);

            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CARD_NOT_FOUND + id);
        }
    }

    @PostMapping("/cards/{id}/attached-files")
    public ResponseEntity<Card> attachFileToCard(@PathVariable Long id, @RequestBody AttachedFile attachedFile) {
        checkAttachedFile(attachedFile);

        try{
            this.cardService.attachFileToCard(id, attachedFile);

            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CARD_NOT_FOUND + id);
        }
    }

    @DeleteMapping("/cards/{id}/attached-files")
    public ResponseEntity<Card> detachFileFromCard(@PathVariable Long id, @RequestBody AttachedFile attachedFile) {
        checkAttachedFile(attachedFile);

        try{
            this.cardService.detachFileToCard(id, attachedFile);

            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/cards/{id}/checks")
    public ResponseEntity<Card> addCheckToCard(@PathVariable Long id, @RequestBody Check check) {
        this.checkCheck(check);

        try{
            this.cardService.addCheckToCard(id, check);

            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CARD_NOT_FOUND + id);
        }
    }

    @DeleteMapping("/cards/{id}/checks")
    public ResponseEntity<Card> removeCheckFromCard(@PathVariable Long id, @RequestBody Check check) {
        this.checkCheck(check);

        try{
            this.cardService.removeCheckFromCard(id, check);

            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/cards/{id}/checks")
    public ResponseEntity<Card> updateCheckToCard(@PathVariable Long id, @RequestBody Check check) {
        this.checkCheck(check);

        try {
            this.cardService.updateCheckFromCard(id, check);

            return ResponseEntity.noContent().build();
        }catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/cards/{cardId}/tags")
    public ResponseEntity<Card> addTagToCard(@PathVariable Long cardId, @RequestBody Long tagId) {
        Tag tagById = this.tagService.getSpecificTagById(tagId);
        checkTag(tagId, tagById);

        try{
            this.cardService.addTagToCard(cardId, tagById);

            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CARD_NOT_FOUND + cardId);
        }
    }

    @DeleteMapping("/cards/{cardId}/tags")
    public ResponseEntity<Card> removeTagFromCard(@PathVariable Long cardId, @RequestBody Long tagId) {
        Tag tagById = this.tagService.getSpecificTagById(tagId);
        checkTag(tagId, tagById);

        try{
            this.cardService.removeTagFromCard(cardId, tagById);

            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    static TemplateCardDTO mapToTemplateCardDTO(Card currentCard)  {
        Long id = currentCard.getId();
        String title = currentCard.getTitle();
        String description = currentCard.getDescription();
        CardType cardType = currentCard.getCardType();
        Color color = currentCard.getColor();
        List<AttachedFile> attachedFiles = currentCard.getAttachedFiles();
        Date deadLine = currentCard.getDeadLine();
        List<Check> checks = currentCard.getChecks();
        int position = currentCard.getPosition();
        List<Tag> tagList = currentCard.getTagList();

        return new TemplateCardDTO(
                id, title, description, cardType, color, attachedFiles, deadLine, checks, position, tagList
        );
    }
}
