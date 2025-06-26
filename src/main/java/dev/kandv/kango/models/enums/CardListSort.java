package dev.kandv.kango.models.enums;

import dev.kandv.kango.models.Card;
import lombok.Getter;

import java.util.Comparator;

@Getter
public enum CardListSort {
    BY_ID(Comparator.comparing(Card::getId)),
    BY_ID_REVERSE(Comparator.comparing(Card::getId).reversed()),
    BY_TITLE(Comparator.comparing(Card::getTitle)),
    BY_TITLE_REVERSE(Comparator.comparing(Card::getTitle).reversed());

    private final Comparator<Card> comparator;

    CardListSort(Comparator<Card> comparator) {
        this.comparator = comparator;
    }
}