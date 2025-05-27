package dev.kandv.kango.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Table {
    private String name;
    private int position;
    private List<Card> cardList = new ArrayList<>();

    public Table(String name) {
        this.name = name;
        this.position = 0;
    }

    @Getter
    public enum SortType {
        NATURAL(Comparator.comparing(Card::getTitle)),
        REVERSE(Comparator.comparing(Card::getTitle).reversed());

        private final Comparator<Card> comparator;

        SortType(Comparator<Card> comparator) {
            this.comparator = comparator;
        }
    }

    public void sortCardList(SortType sortType){
        this.cardList.sort(sortType.getComparator());
    }

    public void cleanCardList() {
        this.cardList.clear();
    }

    public void addCardToCardList(Card newCard) {
        newCard.setPosition(this.cardList.size());
        this.cardList.add(newCard);
    }

    public void removeCardFromCardList(Card card) {
        this.cardList.remove(card);
        for (int i = 0; i < this.cardList.size(); i++) {
            this.cardList.get(i).setPosition(i);
        }
    }
}
