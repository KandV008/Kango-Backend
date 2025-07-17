package dev.kandv.kango.services;

import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.Table;
import dev.kandv.kango.models.enums.CardListSort;
import dev.kandv.kango.repositories.TableRepository;
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
public class TableService {

    public static final String INVALID_TABLE_CREATION_ERROR = "ERROR: Invalid Table. Value: ";
    public static final String NOT_FOUND_CARD_IN_THE_TABLE_ERROR = "ERROR: There is no Card with such ID in the Table. ID: ";

    private final TableRepository tableRepository;
    private final CardService cardService;

    public Table getSpecificTableById(Long id) {
        Optional<Table> cardById = this.tableRepository.findById(id);
        return cardById.orElse(null);
    }

    public Table createTable(Table table) {
        try{
            return this.tableRepository.save(table);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(INVALID_TABLE_CREATION_ERROR + table);
        }
    }

    public void removeAllTables() {
        this.tableRepository.deleteAll();
    }

    public void removeTableById(Long id) {
        this.tableRepository.deleteById(id);
    }

    private Table checkTableDatabaseResult(Long id, Optional<Table> result) {
        if (result.isEmpty()) {
            throw new NoSuchElementException(NOT_FOUND_TABLE_WITH_ID_ERROR + id);
        }

        return result.get();
    }

    private void checkId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(INVALID_ID_ERROR + null);
        }
    }

    private void checkElementToUpdate(Object newObject, String element) {
        if (newObject == null) {
            throw new IllegalArgumentException(INVALID_ELEMENT_ERROR + element);
        }
    }

    public void updateNameTable(Long id, String newName) {
        this.checkId(id);
        this.checkElementToUpdate(newName, NAME_ELEMENT);

        Optional<Table> result = this.tableRepository.findById(id);

        Table currentTable = this.checkTableDatabaseResult(id, result);
        currentTable.setName(newName);
        this.tableRepository.save(currentTable);
    }

    public boolean checkPositions(int newPosition, int oldPosition) {
        return newPosition != oldPosition;
    }

    private boolean checkExistenceInTable(Table currentTable, Card currentCard) {
        List<Card> cardList = currentTable.getCardList();

        return cardList.contains(currentCard);
    }

    @Transactional
    public void addCardToTable(Long tableId, Long cardId) {
        this.checkId(tableId);
        Card currentCard = obtainCard(cardId, cardService);

        Optional<Table> result = this.tableRepository.findById(tableId);
        Table currentTable = this.checkTableDatabaseResult(tableId, result);

        currentTable.addCardToCardList(currentCard);
        this.tableRepository.save(currentTable);
    }

    @Transactional
    public void removeCardFromTable(Long tableId, Long cardId) {
        this.checkId(tableId);
        Card currentCard = obtainCard(cardId, cardService);

        Optional<Table> result = this.tableRepository.findById(tableId);
        Table currentTable = this.checkTableDatabaseResult(tableId, result);

        boolean isSuccess = currentTable.removeCardFromCardList(currentCard);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_CARD_IN_THE_TABLE_ERROR + cardId);
        }

        this.cardService.removeCardById(cardId);
        this.tableRepository.save(currentTable);
    }

    @Transactional
    public void sortCardListFromTable(Long id, CardListSort cardListSort) {
        this.checkId(id);
        this.checkElementToUpdate(cardListSort, CARD_LIST_SORT_ELEMENT);

        Optional<Table> result = this.tableRepository.findById(id);
        Table currentTable = this.checkTableDatabaseResult(id, result);

        currentTable.sortCardList(cardListSort);
        this.tableRepository.save(currentTable);
    }

    @Transactional
    public void updateCardPositionFromTable(Long tableId, Long cardId, int newPosition) {
        this.checkId(tableId);
        Card currentCard = obtainCard(cardId, cardService);

        Optional<Table> result = this.tableRepository.findById(tableId);
        Table currentTable = this.checkTableDatabaseResult(tableId, result);

        boolean existsCardInTable = this.checkExistenceInTable(currentTable, currentCard);

        if (!existsCardInTable) {
            throw new NoSuchElementException(NOT_FOUND_CARD_IN_THE_TABLE_ERROR + cardId);
        }

        boolean hasMovement = this.checkPositions(newPosition, currentCard.getPosition());

        if (!hasMovement) {
            return;
        }

        boolean isSuccess = currentTable.updateCardPosition(currentCard, newPosition);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_CARD_IN_THE_TABLE_ERROR + cardId);
        }

        this.tableRepository.save(currentTable);
    }

    @Transactional
    public void moveCardFromTableToAnotherTable(Long originTableId, Long cardId, Long destinyTableId, int newPosition) {
        this.checkId(originTableId);

        Card currentCard = obtainCard(cardId, this.cardService);

        Table destinyTable = obtainDestinationTable(destinyTableId);

        this.removeCardFromTable(originTableId, cardId);

        Card cloneCard = new Card(currentCard);
        Card newCard = this.cardService.createCard(cloneCard);
        destinyTable.addCardToCardList(newCard);
        destinyTable.updateCardPosition(newCard, newPosition);

        this.tableRepository.save(destinyTable);
    }

    @Transactional
    public void moveCardListFromTableToAnotherTable(Long originTableId, Long destinyTableId) {
        Table originTable = obtainOriginTable(originTableId);
        Table destinyTable = obtainDestinationTable(destinyTableId);

        List<Card> cardListOrigin = originTable.getCardList();

        cardListOrigin.forEach(destinyTable::addCardToCardList);

        cardListOrigin.clear();

        this.tableRepository.save(originTable);
        this.tableRepository.save(destinyTable);
    }

    @Transactional
    public void copyCardListFromTableToAnotherTable(Long originTableId, Long destinyTableId) {
        Table originTable = obtainOriginTable(originTableId);
        Table destinyTable = obtainDestinationTable(destinyTableId);

        List<Card> copyCardList = originTable.copyCardList();

        copyCardList.forEach((Card card) ->{
            Card newCard = this.cardService.createCard(card);
            destinyTable.addCardToCardList(newCard);
        });

        this.tableRepository.save(destinyTable);
    }

    private Table obtainOriginTable(Long originTableId) {
        this.checkId(originTableId);
        Optional<Table> originById = this.tableRepository.findById(originTableId);
        return this.checkTableDatabaseResult(originTableId, originById);
    }

    private Table obtainDestinationTable(Long destinyTableId) {
        this.checkElementToUpdate(destinyTableId, DESTINY_TABLE_ID_ELEMENT);
        Optional<Table> destinyById = this.tableRepository.findById(destinyTableId);
        return this.checkTableDatabaseResult(destinyTableId, destinyById);
    }
}
