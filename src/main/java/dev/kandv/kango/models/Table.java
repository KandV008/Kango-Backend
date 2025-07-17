package dev.kandv.kango.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import dev.kandv.kango.models.enums.CardListSort;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
@Entity
@jakarta.persistence.Table(name = "tables")
public class Table {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private String name;
    private int position;
    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cardList = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "dashboard_id")
    private Dashboard dashboard;

    public Table(@NonNull String name) {
        this.name = name;
        this.position = 0;
    }

    public Table(@NonNull String name, int position) {
        this.name = name;
        this.position = position;
    }

    public void sortCardList(CardListSort cardListSort){
        this.cardList.sort(cardListSort.getComparator());

        for (int i = 0; i < this.cardList.size(); i++) {
            this.cardList.get(i).setPosition(i);
        }
    }

    public void cleanCardList() {
        this.cardList.clear();
    }

    public void addCardToCardList(Card newCard) {
        newCard.setPosition(this.cardList.size());
        newCard.setTable(this);
        this.cardList.add(newCard);
    }

    public boolean removeCardFromCardList(Card card) {
        boolean isSuccess = this.cardList.remove(card);

        if (!isSuccess) {
            return false;
        }

        for (int i = 0; i < this.cardList.size(); i++) {
            this.cardList.get(i).setPosition(i);
        }

        return true;
    }

    public boolean updateCardPosition(Card currentCard, int newPosition) {
        int oldPosition = currentCard.getPosition();

        if(oldPosition == newPosition){
            return false;
        }

        boolean success = this.cardList.remove(currentCard);

        if (!success) {
            return false;
        }

        currentCard.setPosition(newPosition);

        this.cardList.add(newPosition, currentCard);

        for (int i = 0; i < this.cardList.size(); i++) {
            this.cardList.get(i).setPosition(i);
        }

        return true;
    }

    public List<Card> copyCardList() {
        List<Card> copyList = new ArrayList<>();

        for (Card card : this.cardList) {
            copyList.add(new Card(card));
        }

        return copyList;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Table table = (Table) obj;
        return this.id != null && this.id.equals(table.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
