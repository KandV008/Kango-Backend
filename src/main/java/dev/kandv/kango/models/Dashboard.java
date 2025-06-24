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
    @CollectionTable(name = "dashboard_attached_file", joinColumns = @JoinColumn(name = "dashboard_id"))
    private List<AttachedFile> attachedFiles = new LinkedList<>();
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
        this.attachedFiles.add(attachedFile);
    }

    public boolean detachFile(AttachedFile attachedFile) {
        return this.attachedFiles.remove(attachedFile);
    }

    public void addTable(Table table) {
        this.tableList.add(table);
    }

    public boolean removeTable(Table table) {
        boolean isSuccess = this.tableList.remove(table);

        if (!isSuccess){
            return false;
        }

        for (int i = 0; i < this.tableList.size(); i++) {
            this.tableList.get(i).setPosition(i);
        }

        return true;
    }

    public void addTemplateCard(Card card) {
        card.setDashboard(this);
        this.templateCardList.add(card);
    }

    public boolean removeTemplateCard(Card card) {
        return this.templateCardList.remove(card);
    }

    public void addTagToTagList(Tag tag) {
        this.tagList.add(tag);
    }

    public boolean removeTagFromTagList(Tag tag) {
        return this.tagList.remove(tag);
    }

    //public void addAutomationToAutomationList(Automation automation) { TODO Decide what to do
    //    this.automationList.add(automation);
    //}
//
    //public void removeAutomationFromAutomation(Automation automation) {
    //    this.automationList.remove(automation);
    //}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Dashboard tag = (Dashboard) obj;
        return this.id != null && this.id.equals(tag.id);
    }
}
