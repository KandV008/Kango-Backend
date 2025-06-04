package dev.kandv.kango.units.models;

import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TableTest {

    private Table table;
    private List<Card> cardList;
    private Card card1;
    private Card card2;

    @BeforeEach
    void beforeEach() {
        String name = "Example";
        this.table = new Table(name);
        this.cardList = new ArrayList<>();

        this.card1 = new Card("Example 1");
        this.card1.setPosition(0);
        this.cardList.add(this.card1);

        this.card2 = new Card("Example 2");
        this.card2.setPosition(1);
        this.cardList.add(this.card2);
    }

    @Test
    void testSetName(){
        String expectedName = "example";

        this.table.setName(expectedName);

        String result = this.table.getName();
        assertThat(result).isEqualTo(expectedName);
    }

    @Test
    void testGetName(){
        String expectedName = "example";
        Table exampleTable = new Table(expectedName);

        String result = exampleTable.getName();
        assertThat(result).isEqualTo(expectedName);
    }

    @Test
    void testSetPosition(){
        int expectedPosition = 1;

        this.table.setPosition(expectedPosition);
        int result = this.table.getPosition();

        assertThat(result).isEqualTo(expectedPosition);
    }

    @Test
    void testGetPosition(){
        String name = "example";
        int expectedPosition = 1;
        Table newTable = new Table(name, expectedPosition, new ArrayList<>());

        int result = newTable.getPosition();

        assertThat(result).isEqualTo(expectedPosition);
    }

    @Test
    void testSetCardList(){
        this.table.setCardList(this.cardList);

        List<Card> result = this.table.getCardList();

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(this.card1);
        assertThat(result.get(1)).isEqualTo(this.card2);
    }

    @Test
    void testSortCardList(){
        this.table.setCardList(this.cardList);

        this.table.sortCardList(Table.SortType.REVERSE);
        List<Card> result = this.table.getCardList();

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(this.card2);
        assertThat(result.get(1)).isEqualTo(this.card1);
    }

    @Test
    void testCleanCardList(){
        this.table.setCardList(this.cardList);

        this.table.cleanCardList();
        List<Card> result = this.table.getCardList();

        assertThat(result).isEmpty();
    }

    @Test
    void testAddCardToCardList(){
        this.table.setCardList(new ArrayList<>());
        Card newCard = new Card("New Card");

        this.table.addCardToCardList(newCard);

        List<Card> result = this.table.getCardList();
        assertThat(result).hasSize(1);
        String resultCardTitle = result.getFirst().getTitle();
        assertThat(resultCardTitle).isEqualTo(newCard.getTitle());
    }

    @Test
    void testRemoveCardFromCardList(){
        this.table.setCardList(this.cardList);

        this.table.removeCardFromCardList(this.card1);

        List<Card> result = this.table.getCardList();
        assertThat(result).hasSize(1);
        Card remainingCard = result.getFirst();
        assertThat(remainingCard).isEqualTo(this.card2);
        assertThat(remainingCard.getPosition()).isZero();
    }
}
