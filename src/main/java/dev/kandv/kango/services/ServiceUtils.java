package dev.kandv.kango.services;

import dev.kandv.kango.models.Card;

import java.util.NoSuchElementException;

import static dev.kandv.kango.services.ErrorMessagesServices.*;

public class ServiceUtils {

    public static final String CHECK_ELEMENT = "check";
    public static final String TAG_ELEMENT = "tag";
    public static final String FILE_ELEMENT = "attachFile";
    public static final String TABLE_ID_ELEMENT = "table_id";
    public static final String DESTINY_TABLE_ID_ELEMENT = "destiny_table_id";
    public static final String CARD_LIST_SORT_ELEMENT = "card_list_sort";
    public static final String CARD_ID_ELEMENT = "card_id";

    public static final String NAME_ELEMENT = "name";

    private ServiceUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void checkId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(INVALID_ID_ERROR + null);
        }
    }

    public static void checkElementToUpdate(Object newObject, String element) {
        if (newObject == null) {
            throw new IllegalArgumentException(INVALID_ELEMENT_ERROR + element);
        }
    }

    public static Card obtainCard(Long cardId, CardService cardService) {
        checkElementToUpdate(cardId, CARD_ID_ELEMENT);

        Card currentCard = cardService.getSpecificCardById(cardId);

        if (currentCard == null) {
            throw new NoSuchElementException(NOT_FOUND_CARD_WITH_ID_ERROR + cardId);
        }

        return currentCard;
    }
}
