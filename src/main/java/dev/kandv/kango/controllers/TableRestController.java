package dev.kandv.kango.controllers;

import dev.kandv.kango.dtos.CardDTO;
import dev.kandv.kango.dtos.TableDTO;
import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.Table;
import dev.kandv.kango.models.enums.CardListSort;
import dev.kandv.kango.services.CardService;
import dev.kandv.kango.services.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TableRestController {

    public static final String INVALID_NAME = "ERROR: Invalid Table Name. Value: ";
    public static final String INVALID_CARD_LIST = "ERROR: Invalid Table Card List. Value: ";
    public static final String NULL_CARD_IN_CARD_LIST = "ERROR: Null Card in Card List. Index: ";
    public static final String TABLE_NOT_FOUND = "ERROR: Table Not Found with that ID. ID: ";
    public static final String INVALID_CARD_LIST_SORT = "ERROR: Card List Sort is null";
    public static final String INTERNAL_SERVER_ERROR = "ERROR: Something gone wrong at server. It is not you fault.";

    private final TableService tableService;
    private final CardService cardService;

    private void checkTableName(String name) {
        if (name == null || name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_NAME + name);
        }
    }

    private void checkCards(List<CardDTO> cards) {
        if (cards == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_CARD_LIST + null);
        }

        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i) == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, NULL_CARD_IN_CARD_LIST + i);
            }
        }
    }

    private void checkCardListSort(CardListSort cardListSort) {
        if (cardListSort == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_CARD_LIST_SORT);
        }
    }

    private void checkTable(Long id, Table currentTable) {
        if (currentTable == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TABLE_NOT_FOUND + id);
        }
    }

    @PostMapping("/tables")
    public ResponseEntity<Table> createTable(@RequestBody TableDTO tableDTO) {
        String name = tableDTO.getName();
        this.checkTableName(name);
        List<CardDTO> cards = tableDTO.getCardList();
        this.checkCards(cards);

        Table table = new Table(name);

        cards.forEach((card) -> {
            Card newCard = new Card(card.getTitle(), card.getCardType());
            Card createdCard = this.cardService.createCard(newCard);

            table.addCardToCardList(createdCard);
        });

        Table createdTable = this.tableService.createTable(table);

        return ResponseEntity.status(201).body(createdTable);
    }

    @GetMapping("/tables/{id}")
    public ResponseEntity<Table> getCard(@PathVariable Long id) {
        Table currentTable = this.tableService.getSpecificTableById(id);

        this.checkTable(id, currentTable);

        return ResponseEntity.status(200).body(currentTable);
    }

    @PutMapping("/tables/{id}/name")
    public ResponseEntity<Table> updateTableName(@PathVariable Long id, @RequestBody TableDTO tableDTO) {
        String name = tableDTO.getName();
        this.checkTableName(name);

        try{
            this.tableService.updateNameTable(id, name);
            Table updatedTable = this.tableService.getSpecificTableById(id);

            return ResponseEntity.status(200).body(updatedTable);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TABLE_NOT_FOUND + id);
        }
    }

    @DeleteMapping("/tables/{id}")
    public ResponseEntity<TableDTO> deleteTable(@PathVariable Long id) {
        Table currentTable = this.tableService.getSpecificTableById(id);

        this.checkTable(id, currentTable);

        this.tableService.removeTableById(id);
        Table nullCard = this.tableService.getSpecificTableById(id);

        if (nullCard != null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR);
        }

        TableDTO tableDTO = mapToTableDTO(currentTable);
        return new ResponseEntity<>(tableDTO, HttpStatus.OK);
    }

    @PostMapping("/tables/{tableId}/cards")
    public ResponseEntity<Table> addCardToTable(@PathVariable Long tableId, @RequestBody Long cardId) {
        try{
            this.tableService.addCardToTable(tableId, cardId);
            Table updatedTable = this.tableService.getSpecificTableById(tableId);

            return ResponseEntity.status(201).body(updatedTable);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/tables/{tableId}/cards")
    public ResponseEntity<TableDTO> removeCardFromTable(@PathVariable Long tableId, @RequestBody Long cardId) {
        try{
            this.tableService.removeCardFromTable(tableId, cardId);
            Table updatedTable = this.tableService.getSpecificTableById(tableId);

            TableDTO tableDTO = mapToTableDTO(updatedTable);

            return ResponseEntity.status(200).body(tableDTO);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/tables/{id}/sort")
    public ResponseEntity<Table> sortCardListFromTable(@PathVariable Long id, @RequestParam CardListSort sort) {
        this.checkCardListSort(sort);

        try{
            this.tableService.sortCardListFromTable(id, sort);
            Table updatedTable = this.tableService.getSpecificTableById(id);

            return ResponseEntity.status(200).body(updatedTable);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TABLE_NOT_FOUND + id);
        }
    }

    @PutMapping("/tables/{tableId}/cards/{cardId}/position")
    public ResponseEntity<TableDTO> sortCardListFromTable(@PathVariable Long tableId, @PathVariable Long cardId, @RequestParam int position) {
        try{
            this.tableService.updateCardPositionFromTable(tableId, cardId, position);
            Table updatedTable = this.tableService.getSpecificTableById(tableId);

            TableDTO tableDTO = mapToTableDTO(updatedTable);
            return ResponseEntity.status(200).body(tableDTO);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/tables/{tableId}/cards/{cardId}")
    public ResponseEntity<Void> moveCardFromTableToAnotherTable(
            @PathVariable Long tableId,
            @PathVariable Long cardId,
            @RequestParam Long newTable,
            @RequestParam int position) {
        try {
            this.tableService.moveCardFromTableToAnotherTable(tableId, cardId, newTable, position);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/tables/{tableId}/cards")
    public ResponseEntity<Void> moveCardListFromTableToAnotherTable(
            @PathVariable Long tableId,
            @RequestParam Long newTable) {
        try {
            this.tableService.moveCardListFromTableToAnotherTable(tableId, newTable);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/tables/{tableId}/cards/copy")
    public ResponseEntity<Void> copyCardListFromTableToAnotherTable(
            @PathVariable Long tableId,
            @RequestParam Long newTable) {
        try {
            this.tableService.copyCardListFromTableToAnotherTable(tableId, newTable);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    static TableDTO mapToTableDTO(Table currentTable) {
        List<Card> cardList = currentTable.getCardList();

        List<CardDTO> list = cardList.stream().map((card -> new CardDTO(card.getTitle(), card.getDescription(), card.getCardType(), card.getColor(), card.getDeadLine()))).toList();

        return new TableDTO(currentTable.getName(), list);
    }
}
