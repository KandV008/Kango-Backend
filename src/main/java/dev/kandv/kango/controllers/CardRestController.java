package dev.kandv.kango.controllers;

import dev.kandv.kango.dtos.CardDTO;
import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.enums.CardType;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.utils.AttachedFile;
import dev.kandv.kango.models.utils.Check;
import dev.kandv.kango.services.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CardRestController {
    public static final String INVALID_TITLE = "ERROR: Invalid Card Title. Value: ";
    public static final String INVALID_DESCRIPTION = "ERROR: Invalid Card Description. Value: ";
    public static final String INVALID_CARD_TYPE = "ERROR: Invalid Card Type. Value: ";
    public static final String INVALID_COLOR = "ERROR: Invalid Card Color. Value: ";
    public static final String INVALID_DEAD_LINE = "ERROR: Invalid Card Color. Value: ";
    public static final String CARD_NOT_FOUND = "ERROR: Card Not Found with that ID. ID: ";
    public static final String INTERNAL_SERVER_ERROR = "ERROR: Something gone wrong at server. It is not you fault.";
    public static final String NULL_ATTACHED_FILE = "ERROR: Attached File is null";
    public static final String INVALID_ATTACHED_FILE = "ERROR: Some or all attributes from Attached File are invalid";
    public static final String NULL_CHECK = "ERROR: Check is null";
    public static final String INVALID_CHECK = "ERROR: Some or all attributes from Check are invalid";

    private final CardService cardService;

    private void checkCardTitle(String title) {
        if (title == null || title.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_TITLE + title);
        }
    }

    private void checkCardDescription(String description) {
        if (description == null || description.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_DESCRIPTION + description);
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

    private void checkCard(Long id, Card currentCard) {
        if (currentCard == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CARD_NOT_FOUND + id);
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

    @GetMapping("/cards/{id}")
    public ResponseEntity<Card> getCard(@PathVariable Long id) {
        Card currentCard = this.cardService.getSpecificCardById(id);

        this.checkCard(id, currentCard);

        return ResponseEntity.status(200).body(currentCard);
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<CardDTO> deleteCard(@PathVariable Long id) {
        Card currentCard = this.cardService.getSpecificCardById(id);

        this.checkCard(id, currentCard);

        this.cardService.removeCardById(id);
        Card nullCard = this.cardService.getSpecificCardById(id);

        if (nullCard != null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR);
        }

        CardDTO cardDTO = new CardDTO(currentCard.getTitle(), currentCard.getCardType());
        return new ResponseEntity<>(cardDTO, HttpStatus.OK);
    }

    @GetMapping("/global-template-cards")
    public ResponseEntity<List<Card>> getGlobalTemplatesCards() {
        List<Card> allGlobalTemplateCards = this.cardService.getAllGlobalTemplateCards();

        return ResponseEntity.status(200).body(allGlobalTemplateCards);
    }

    @PutMapping("/cards/{id}/title")
    public ResponseEntity<Card> updateCardTitle(@PathVariable Long id, @RequestBody CardDTO cardDTO) {
        String title = cardDTO.getTitle();
        this.checkCardTitle(title);

        try{
            this.cardService.updateTitleCard(id, title);
            Card updatedCard = this.cardService.getSpecificCardById(id);

            return ResponseEntity.status(200).body(updatedCard);
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
            Card updatedCard = this.cardService.getSpecificCardById(id);

            return ResponseEntity.status(200).body(updatedCard);
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
            Card updatedCard = this.cardService.getSpecificCardById(id);

            return ResponseEntity.status(200).body(updatedCard);
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
            Card updatedCard = this.cardService.getSpecificCardById(id);

            return ResponseEntity.status(200).body(updatedCard);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CARD_NOT_FOUND + id);
        }
    }

    @PostMapping("/cards/{id}/attached-files")
    public ResponseEntity<Card> attachFileToCard(@PathVariable Long id, @RequestBody AttachedFile attachedFile) {
        this.checkAttachedFile(attachedFile);

        try{
            this.cardService.attachFileToCard(id, attachedFile);
            Card updatedCard = this.cardService.getSpecificCardById(id);

            return ResponseEntity.status(201).body(updatedCard);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CARD_NOT_FOUND + id);
        }
    }

    @DeleteMapping("/cards/{id}/attached-files")
    public ResponseEntity<Card> detachFileToCard(@PathVariable Long id, @RequestBody AttachedFile attachedFile) {
        this.checkAttachedFile(attachedFile);

        try{
            this.cardService.detachFileToCard(id, attachedFile);
            Card updatedCard = this.cardService.getSpecificCardById(id);

            return ResponseEntity.status(200).body(updatedCard);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/cards/{id}/checks")
    public ResponseEntity<Card> addCheckToCard(@PathVariable Long id, @RequestBody Check check) {
        this.checkCheck(check);

        try{
            this.cardService.addCheckToCard(id, check);
            Card updatedCard = this.cardService.getSpecificCardById(id);

            return ResponseEntity.status(201).body(updatedCard);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CARD_NOT_FOUND + id);
        }
    }

    @DeleteMapping("/cards/{id}/checks")
    public ResponseEntity<Card> removeCheckFromCard(@PathVariable Long id, @RequestBody Check check) {
        this.checkCheck(check);

        try{
            this.cardService.removeCheckFromCard(id, check);
            Card updatedCard = this.cardService.getSpecificCardById(id);

            return ResponseEntity.status(200).body(updatedCard);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/cards/{id}/checks")
    public ResponseEntity<Card> updateCheckToCard(@PathVariable Long id, @RequestBody Check check) {
        this.checkCheck(check);

        try {
            this.cardService.updateCheckFromCard(id, check);
            Card updatedCard = this.cardService.getSpecificCardById(id);

            return ResponseEntity.status(200).body(updatedCard);
        }catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
