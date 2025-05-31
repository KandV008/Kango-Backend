package dev.kandv.kango.models;

import dev.kandv.kango.models.utils.AttachedFile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard {
    private String name;
    private List<Table> tableList;
    private List<AttachedFile> attachedAttachedFiles = new LinkedList<>();
    private List<Card> templateCardList = new LinkedList<>();
    private List<Tag> tagList = new LinkedList<>();
    private List<Automation> automationList = new LinkedList<>();

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

    public void addAutomationToAutomationList(Automation automation) {
        this.automationList.add(automation);
    }

    public void removeAutomationFromAutomation(Automation automation) {
        this.automationList.remove(automation);
    }
}
