package dev.kandv.kango.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import dev.kandv.kango.models.utils.AttachedFile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
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
public class Dashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Table> tableList;
    @ElementCollection
    @CollectionTable(name = "card_attached_file", joinColumns = @JoinColumn(name = "card_id"))
    private List<AttachedFile> attachedAttachedFiles = new LinkedList<>();
    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> templateCardList = new LinkedList<>();
    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tag> tagList = new LinkedList<>();
    //private List<Automation> automationList = new LinkedList<>(); TODO Decide what to do

    public Dashboard(String name) {
        this.name = name;
        this.tableList = new LinkedList<>();
    }

    public Dashboard(String name, List<Table> tableList) {
        this.name = name;
        this.tableList = tableList;
    }

    public void attachFile(AttachedFile attachedFile) {
        this.attachedAttachedFiles.add(attachedFile);
    }

    public void detachFile(AttachedFile attachedFile) {
        this.attachedAttachedFiles.remove(attachedFile);
    }

    public void addTable(Table table) {
        this.tableList.add(table);
    }

    public void removeTable(Table table) {
        this.tableList.remove(table);
    }

    public void addTemplateCard(Card card) {
        this.templateCardList.add(card);
    }

    public void removeTemplateCard(Card card) {
        this.templateCardList.remove(card);
    }

    public void addTagToTagList(Tag tag) {
        this.tagList.add(tag);
    }

    public void removeTagFromTagList(Tag tag) {
        this.tagList.remove(tag);
    }

    //public void addAutomationToAutomationList(Automation automation) { TODO Decide what to do
    //    this.automationList.add(automation);
    //}
//
    //public void removeAutomationFromAutomation(Automation automation) {
    //    this.automationList.remove(automation);
    //}
}
