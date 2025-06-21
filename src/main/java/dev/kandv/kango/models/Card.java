package dev.kandv.kango.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import dev.kandv.kango.models.enums.CardType;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.utils.AttachedFile;
import dev.kandv.kango.models.utils.Check;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
@Entity
public class Card {

    public static final String NOT_FOUND_CHECK_ERROR = "ERROR: There is no such Check in this Card. Card: ";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private CardType cardType = CardType.NORMAL;
    private Color color;
    @ElementCollection
    @CollectionTable(name = "card_attached_file", joinColumns = @JoinColumn(name = "card_id"))
    private List<AttachedFile> attachedFiles = new LinkedList<>();
    private Date deadLine;
    @ElementCollection
    @CollectionTable(name = "card_check", joinColumns = @JoinColumn(name = "card_id"))
    private List<Check> checks = new LinkedList<>();
    private int position = -1;
    @ManyToMany
    @JoinTable(
            name = "card_tags",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tagList = new LinkedList<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private Table table;

    public Card(String title){
        this.title = title;
    }

    public Card(String title, CardType cardType){
        this.title = title;
        this.cardType = cardType;
        this.table = null;
    }

    public Card(String title, int position){
        this.title = title;
        this.position = position;
        this.table = null;
    }

    public Card(Card other) {
        this.id = null;
        this.title = other.title;
        this.description = other.description;
        this.cardType = other.cardType;
        this.color = other.color;
        this.attachedFiles = new LinkedList<>(other.attachedFiles); // TODO See about doing a deep copy
        this.deadLine = other.deadLine != null ? new Date(other.deadLine.getTime()) : null;
        this.checks = new LinkedList<>(other.checks); // TODO See about doing a deep copy
        this.position = other.position;
        this.tagList = new LinkedList<>(other.tagList); // TODO See about doing a deep copy
        this.table = null;
    }


    public void attachFile(AttachedFile attachedFile){
        this.attachedFiles.add(attachedFile);
    }

    public boolean detachFile(AttachedFile attachedFile){
        return this.attachedFiles.remove(attachedFile);
    }

    public void addCheckToCheckList(Check check){
        check.setPosition(this.checks.size());
        this.checks.add(check);
    }

    public boolean removeCheckFromCheckList(Check check) {
        boolean result = this.checks.remove(check);

        if (!result){
            return false;
        }

        for (int i = 0; i < this.checks.size(); i++) {
            this.checks.get(i).setPosition(i);
        }

        return true;
    }

    public void updateCheckFromCheckList(Check check) {
        try {
            this.checks.set(check.getPosition(), check);
        } catch (IndexOutOfBoundsException e) {
            throw new NoSuchElementException(NOT_FOUND_CHECK_ERROR + this.id);
        }
    }

    public void addTagToTagList(Tag tag) {
        this.tagList.add(tag);
    }

    public boolean removeTagFromTagList(Tag tag) {
        return this.tagList.remove(tag);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Card card = (Card) obj;
        return this.id != null && this.id.equals(card.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
