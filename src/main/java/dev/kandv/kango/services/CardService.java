package dev.kandv.kango.services;

import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.enums.CardType;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.utils.AttachedFile;
import dev.kandv.kango.models.utils.Check;
import dev.kandv.kango.repositories.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static dev.kandv.kango.services.ErrorMessagesServices.*;

@Service
@RequiredArgsConstructor
public class CardService {

    public static final String INVALID_CARD_CREATION_ERROR = "ERROR: Invalid Card. Value: ";
    public static final String NOT_FOUND_ELEMENT_IN_CARD_ERROR = "ERROR: There is no such Element in that Card. Element: ";

    private final CardRepository cardRepository;

    public Card getSpecificCardById(Long id) {
        Optional<Card> cardById = this.cardRepository.findById(id);
        return cardById.orElse(null);
    }

    public Card createCard(Card card) {
        try{
            return this.cardRepository.save(card);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(INVALID_CARD_CREATION_ERROR + card);
        }
    }

    public void removeAllCards() {
        List<Card> cards = this.cardRepository.findAll();
        for (Card card : cards) {
            card.setTable(null);
        }
        this.cardRepository.saveAll(cards);
        this.cardRepository.deleteAll();
    }

    public void removeCardById(Long id) {
        this.cardRepository.deleteById(id);
    }

    public List<Card> getAllLocalTemplateCards() {
        return this.cardRepository.findAllByCardTypeEquals(CardType.LOCAL_TEMPLATE);
    } //TODO Check what to do with this method

    public List<Card> getAllGlobalTemplateCards() {
        return this.cardRepository.findAllByCardTypeEquals(CardType.GLOBAL_TEMPLATE);
    }

    private Card checkDatabaseResult(Long id, Optional<Card> result) {
        if (result.isEmpty()) {
            throw new NoSuchElementException(NOT_FOUND_CARD_WITH_ID_ERROR + id);
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

    public void updateTitleCard(Long id, String newTitle) {
        this.checkId(id);

        Optional<Card> result = this.cardRepository.findById(id);

        Card currentCard = this.checkDatabaseResult(id, result);
        currentCard.setTitle(newTitle);
        this.cardRepository.save(currentCard);
    }

    public void updateDescriptionCard(Long id, String newDescription) {
        this.checkId(id);

        Optional<Card> result = this.cardRepository.findById(id);

        Card currentCard = this.checkDatabaseResult(id, result);
        currentCard.setDescription(newDescription);
        this.cardRepository.save(currentCard);
    }

    public void updateColorCard(Long id, Color newColor) {
        this.checkId(id);

        Optional<Card> result = this.cardRepository.findById(id);

        Card currentCard = this.checkDatabaseResult(id, result);
        currentCard.setColor(newColor);
        this.cardRepository.save(currentCard);
    }

    public void updateDeadLineCard(Long id, Date newDeadLine) {
        this.checkId(id);

        Optional<Card> result = this.cardRepository.findById(id);

        Card currentCard = this.checkDatabaseResult(id, result);
        currentCard.setDeadLine(newDeadLine);
        this.cardRepository.save(currentCard);
    }

    public void attachFileToCard(Long id, AttachedFile newAttachedFile) {
        this.checkId(id);
        this.checkElementToUpdate(newAttachedFile, "attachFile");

        Optional<Card> result = this.cardRepository.findById(id);

        Card currentCard = this.checkDatabaseResult(id, result);
        currentCard.attachFile(newAttachedFile);
        this.cardRepository.save(currentCard);
    }

    public void detachFileToCard(Long id, AttachedFile attachedFile) {
        String element = "attachFile";
        this.checkId(id);
        this.checkElementToUpdate(attachedFile, element);

        Optional<Card> result = this.cardRepository.findById(id);

        Card currentCard = this.checkDatabaseResult(id, result);
        boolean isSuccess = currentCard.detachFile(attachedFile);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_ELEMENT_IN_CARD_ERROR + element);
        }

        this.cardRepository.save(currentCard);
    }

    public void addCheckToCard(Long id, Check newCheck) {
        this.checkId(id);
        this.checkElementToUpdate(newCheck, "check");

        Optional<Card> result = this.cardRepository.findById(id);

        Card currentCard = this.checkDatabaseResult(id, result);
        currentCard.addCheckToCheckList(newCheck);
        this.cardRepository.save(currentCard);
    }

    public void removeCheckFromCard(Long id, Check newCheck) {
        String element = "check";
        this.checkId(id);
        this.checkElementToUpdate(newCheck, element);

        Optional<Card> result = this.cardRepository.findById(id);

        Card currentCard = this.checkDatabaseResult(id, result);
        boolean isSuccess = currentCard.removeCheckFromCheckList(newCheck);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_ELEMENT_IN_CARD_ERROR + element);
        }

        this.cardRepository.save(currentCard);
    }

    public void updateCheckFromCard(Long id, Check newCheck) {
        String element = "check";
        this.checkId(id);
        this.checkElementToUpdate(newCheck, element);

        Optional<Card> result = this.cardRepository.findById(id);

        Card currentCard = this.checkDatabaseResult(id, result);
        currentCard.updateCheckFromCheckList(newCheck);

        this.cardRepository.save(currentCard);
    }

    public void addTagToCard(Long id, Tag tag) {
        String element = "tag";
        this.checkId(id);
        this.checkElementToUpdate(tag, element);

        Optional<Card> result = this.cardRepository.findById(id);

        Card currentCard = this.checkDatabaseResult(id, result);
        currentCard.addTagToTagList(tag);

        this.cardRepository.save(currentCard);
    }

    public void removeTagFromCard(Long id, Tag tag) {
        String element = "tag";
        this.checkId(id);
        this.checkElementToUpdate(tag, element);

        Optional<Card> result = this.cardRepository.findById(id);

        Card currentCard = this.checkDatabaseResult(id, result);
        boolean isSuccess = currentCard.removeTagFromTagList(tag);

        if (!isSuccess) {
            throw new NoSuchElementException(NOT_FOUND_ELEMENT_IN_CARD_ERROR + element);
        }

        this.cardRepository.save(currentCard);
    }
}
